package pl.peth.hsbo_spring_demo.controller;

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
import pl.peth.hsbo_spring_demo.service.mqtt.PublisherService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/wago750")
public class Wago750Controller {
    private final Wago750Service wago750Service;
    private final SSEService sseService;
    private final Wago750PublisherService publisherService;
    private final Wago750PublisherService wago750PublisherService;

    public Wago750Controller(Wago750Service wago750Service, SSEService sseService, Wago750PublisherService publisherService, Wago750PublisherService wago750PublisherService) {
        this.wago750Service = wago750Service;
        this.sseService = sseService;
        this.publisherService = publisherService;
        this.wago750PublisherService = wago750PublisherService;
    }

    @GetMapping
    public ResponseEntity<List<Wago750Model>> getAll(@RequestParam Optional<String> key) {
        List<Wago750Model> fetchedData = wago750Service.findAllByKey(key);
        return ResponseEntity.ok(fetchedData);
    }

    @GetMapping("/latest")
    public ResponseEntity<Wago750Model> getLatest() {
        Wago750Model fetchedData = wago750Service.findFirstByOrderByTimestampDesc();
        return ResponseEntity.ok(fetchedData);
    }

    @GetMapping("/period")
    public ResponseEntity<List<Wago750Model>> getPeriod(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Instant startInstant = start.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = end.atZone(ZoneId.systemDefault()).toInstant();

        List<Wago750Model> fetchedData = wago750Service.findByTimestampBetween(startInstant, endInstant);
        return ResponseEntity.ok(fetchedData);
    }

    @PostMapping("/control")
    public ResponseEntity<Map<String, Object>> postControl(@RequestBody MqttMessage body) {
        Map<String, Object> result = wago750PublisherService.publish(body);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamData() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        sseService.addWago750Emitter(emitter);

        emitter.onCompletion(() -> sseService.removeWago750Emitter(emitter));
        emitter.onTimeout(() -> sseService.removeWago750Emitter(emitter));
        emitter.onError((Throwable t) -> sseService.removeWago750Emitter(emitter));

        return emitter;
    }
}
