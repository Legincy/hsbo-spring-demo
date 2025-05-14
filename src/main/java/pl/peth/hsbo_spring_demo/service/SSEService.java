package pl.peth.hsbo_spring_demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pl.peth.hsbo_spring_demo.model.Wago750Model;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;


@Service
public class SSEService {
    private static final Logger log = LoggerFactory.getLogger(SSEService.class);

    private final List<SseEmitter> wago750Emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;
    private final Map<String, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private static final long DEFAULT_TIMEOUT = 30 * 60 * 1000L; // 30m

    public SSEService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new SSE emitter with a default timeout.
     *
     * @param streamType the type of stream
     * @return a new SseEmitter instance
     */
    public SseEmitter createEmitter(String streamType) {
        return createEmitter(streamType, DEFAULT_TIMEOUT);
    }

    /**
     * Creates a new SSE emitter with a specified timeout.
     *
     * @param streamType the type of stream
     * @param timeout    the timeout in milliseconds
     * @return a new SseEmitter instance
     */
    public SseEmitter createEmitter(String streamType, long timeout) {
        String emitterId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(timeout);

        emitters.putIfAbsent(streamType, new CopyOnWriteArraySet<>());

        emitters.get(streamType).add(emitter);
        log.info("Added emitter {} to stream '{}'. Current count: {}",
                emitterId, streamType, emitters.get(streamType).size());

        configureEmitter(emitter, streamType, emitterId);

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .id(emitterId)
                    .data("Connected to " + streamType + " stream")
                    .reconnectTime(5000));
        } catch (IOException e) {
            log.warn("Failed to send initial event to emitter {}: {}", emitterId, e.getMessage());
            removeEmitter(streamType, emitter);
        }

        return emitter;
    }
    /**
     * Configures the emitter with completion, timeout, and error handlers.
     *
     * @param emitter   the SseEmitter instance
     * @param streamType the type of stream
     * @param emitterId  the ID of the emitter
     */
    private void configureEmitter(SseEmitter emitter, String streamType, String emitterId) {
        emitter.onCompletion(() -> {
            log.debug("Emitter {} completed (stream: {})", emitterId, streamType);
            removeEmitter(streamType, emitter);
        });

        emitter.onTimeout(() -> {
            log.debug("Emitter {} timed out (stream: {})", emitterId, streamType);
            removeEmitter(streamType, emitter);
        });

        emitter.onError((Throwable t) -> {
            if (t != null && t.getMessage() != null && t.getMessage().contains("abgebrochen")) {
                log.debug("Emitter {} connection closed by client (stream: {})", emitterId, streamType);
            } else {
                log.warn("Emitter {} error (stream: {}): {}", emitterId, streamType,
                        t != null ? t.getMessage() : "unknown error");
            }
            removeEmitter(streamType, emitter);
        });
    }

    /**
     * Removes an emitter from the specified stream type.
     *
     * @param streamType the type of stream
     * @param emitter    the SseEmitter instance to remove
     */
    public void removeEmitter(String streamType, SseEmitter emitter) {
        Set<SseEmitter> streamEmitters = emitters.get(streamType);

        if (streamEmitters == null) return;
        boolean removed = streamEmitters.remove(emitter);
        log.debug("Removed emitter from stream '{}': {}. Current count: {}",
                streamType, removed ? "success" : "not found", streamEmitters.size());
    }

    /**
     * Sends an update to all emitters of a specific stream type.
     *
     * @param streamType the type of stream
     * @param eventName  the name of the event
     * @param data       the data to send
     * @param <T>        the type of data
     */
    public <T> void sendUpdate(String streamType, String eventName, T data) {
        Set<SseEmitter> streamEmitters = emitters.get(streamType);
        if (streamEmitters == null || streamEmitters.isEmpty()) {
            return;
        }

        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(data);
        } catch (IOException e) {
            log.error("Failed to serialize data for stream '{}': {}", streamType, e.getMessage());
            return;
        }

        Set<SseEmitter> deadEmitters = new CopyOnWriteArraySet<>();

        for (SseEmitter emitter : streamEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(jsonData));
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("abgebrochen")) {
                    log.debug("Failed to send update to emitter (connection closed): {}", e.getMessage());
                } else {
                    log.warn("Failed to send update to emitter: {}", e.getMessage());
                }

                deadEmitters.add(emitter);
                try {
                    emitter.complete();
                } catch (Exception ignored) {
                    //
                }
            }
        }

        if (!deadEmitters.isEmpty()) {
            streamEmitters.removeAll(deadEmitters);
            log.info("Removed {} dead emitters from stream '{}'. Current count: {}",
                    deadEmitters.size(), streamType, streamEmitters.size());
        }
    }

    /**
     * Returns the number of active emitters for a specific stream type.
     *
     * @param streamType
     * @return the number of active emitters
     */
    public int getEmitterCount(String streamType) {
        Set<SseEmitter> streamEmitters = emitters.get(streamType);
        return streamEmitters != null ? streamEmitters.size() : 0;
    }


    /**
     * Returns the total number of active emitters across all stream types.
     *
     * @return the total number of active emitters
     */
    public int getTotalEmitterCount() {
        return emitters.values().stream()
                .mapToInt(Set::size)
                .sum();
    }


    /**
     * Sends a heartbeat event to all active emitters every 30 seconds to keep the connection alive.
     */
    @Scheduled(fixedRate = 30000) // 30s
    public void sendHeartbeats() {
        emitters.forEach((streamType, streamEmitters) -> {
            if (streamEmitters.isEmpty()) {
                return;
            }

            Set<SseEmitter> deadEmitters = new CopyOnWriteArraySet<>();

            for (SseEmitter emitter : streamEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("heartbeat")
                            .data("ping"));
                } catch (Exception e) {
                    deadEmitters.add(emitter);
                    try {
                        emitter.complete();
                    } catch (Exception ignored) {
                        //
                    }
                }
            }

            if (!deadEmitters.isEmpty()) {
                streamEmitters.removeAll(deadEmitters);
                log.info("Heartbeat removed {} dead emitters from stream '{}'. Current count: {}",
                        deadEmitters.size(), streamType, streamEmitters.size());
            }
        });
    }
}
