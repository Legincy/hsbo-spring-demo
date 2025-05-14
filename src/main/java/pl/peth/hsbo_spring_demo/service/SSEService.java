package pl.peth.hsbo_spring_demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pl.peth.hsbo_spring_demo.model.Wago750Model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@Service
public class SSEService {
    private static final Logger log = LoggerFactory.getLogger(SSEService.class);

    private final List<SseEmitter> wago750Emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;


    public SSEService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Adds a new SSE emitter to the list of emitters.
     *
     * @param emitter the SSE emitter to add
     */
    public void addEmitter(SseEmitter emitter) {
        wago750Emitters.add(emitter);
        log.info("Added Wago750 SSE-Emitter. Current count: {}", wago750Emitters.size());
    }

    /**
     * Removes an SSE emitter from the list of emitters.
     *
     * @param emitter the SSE emitter to remove
     */
    public void removeEmitter(SseEmitter emitter) {
        wago750Emitters.remove(emitter);
        log.info("Removed Wago750 SSE-Emitter. Current count: {}", wago750Emitters.size());
    }

    /**
     * Sends a Model update to all connected SSE emitters.
     *
     * @param wago750Model the Wago750Model to send
     */
    public void sendUpdate(Wago750Model wago750Model) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(wago750Model);
        } catch (JsonProcessingException e) {
            log.error("Error while converting Model to JSON: {}", e.getMessage());
            return;
        }

        wago750Emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("wago750")
                        .data(jsonData));
            } catch (Exception e) {
                log.warn("Error while sending data to emitter: {}", e.getMessage());
                deadEmitters.add(emitter);
                try {
                    emitter.complete();
                } catch (Exception ex) {
                    //
                }
            }
        });

        if (deadEmitters.isEmpty()) return;
        wago750Emitters.removeAll(deadEmitters);
    }
}
