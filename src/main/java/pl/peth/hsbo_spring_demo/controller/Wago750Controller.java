package pl.peth.hsbo_spring_demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pl.peth.hsbo_spring_demo.dto.MqttMessage;
import pl.peth.hsbo_spring_demo.model.Wago750Model;
import pl.peth.hsbo_spring_demo.service.SSEService;
import pl.peth.hsbo_spring_demo.service.Wago750PublisherService;
import pl.peth.hsbo_spring_demo.service.Wago750Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/wago750")
public class Wago750Controller {
    private static final Logger log = LoggerFactory.getLogger(Wago750Controller.class);

    private final Wago750Service wago750Service;
    private final SSEService sseService;
    private final Wago750PublisherService wago750PublisherService;

    public Wago750Controller(Wago750Service wago750Service, SSEService sseService, Wago750PublisherService wago750PublisherService) {
        this.wago750Service = wago750Service;
        this.sseService = sseService;
        this.wago750PublisherService = wago750PublisherService;

        log.debug("Wago750Controller initialized");
    }

    /**
     * Requests all sets from Wago750 repository and returns it as a list.
     *
     * @param key
     * @return ResponseEntity with a list of Wago750Model objects
     */
    @GetMapping
    public ResponseEntity<List<Wago750Model>> getAll(@RequestParam Optional<String> key) {
        log.debug("Received GET-Request on /api/v1/wago750 with parameters: key={}", key.orElse(""));
        List<Wago750Model> fetchedData = wago750Service.findAllByKey(key);

        return ResponseEntity.ok(fetchedData);
    }

    /**
     * Requests the latest set from Wago750 repository and returns it as a single object.
     *
     * @return ResponseEntity with a Wago750Model object
     */
    @GetMapping("/latest")
    public ResponseEntity<Wago750Model> getLatest() {
        log.debug("Received GET-Request on /api/v1/wago750/latest");
        Wago750Model fetchedData = wago750Service.findFirstByOrderByTimestampDesc();

        return ResponseEntity.ok(fetchedData);
    }

    /**
     * Requests the latest set from Wago750 repository and returns it as a single object.
     *
     * @param start
     * @param end
     * @return ResponseEntity with a list of Wago750Model objects
     */
    @GetMapping("/period")
    public ResponseEntity<List<Wago750Model>> getPeriod(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.debug("Received GET-Request on /api/v1/wago750/period with parameters: start={}, end={}", start, end);
        Instant startInstant = start.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = end.atZone(ZoneId.systemDefault()).toInstant();
        List<Wago750Model> fetchedData = wago750Service.findByTimestampBetween(startInstant, endInstant);

        return ResponseEntity.ok(fetchedData);
    }

    /**
     * Receives a RequestBody with a MqttMessage object and publishes it to the Wago750 device.
     *
     * @param body
     * @return ResponseEntity with a map containing the result of the publish operation
     */
    @PostMapping("/control")
    public ResponseEntity<Map<String, Object>> postControl(@RequestBody MqttMessage body) {
        log.debug("Received POST-Request on /api/v1/wago750/control with body: {}", body);
        Map<String, Object> result = wago750PublisherService.publish(body);
        log.debug("Published message to Wago750 with result: {}", result);

        return ResponseEntity.ok(result);
    }

    /**
     * Opens a Server-Sent Events (SSE) connection to stream data from the Wago750 device.
     *
     * @return SseEmitter object for streaming data
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamData() {
        log.debug("Received GET-Request on /api/v1/wago750/stream");
        return sseService.createEmitter("wago750");
    }
}
