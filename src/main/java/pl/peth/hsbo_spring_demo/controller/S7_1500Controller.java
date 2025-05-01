package pl.peth.hsbo_spring_demo.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.peth.hsbo_spring_demo.model.S7_1500Model;
import pl.peth.hsbo_spring_demo.service.S7_1500Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/s7-1500")
public class S7_1500Controller {
    private final S7_1500Service s7_1500Service;

    public S7_1500Controller(S7_1500Service s71500Service) {
        s7_1500Service = s71500Service;
    }

    @GetMapping
    public ResponseEntity<List<S7_1500Model>> getAllAdvanced(@RequestParam Optional<String> key, @RequestParam Optional<Integer> limit, @RequestParam Optional<String> sort, @RequestParam Optional<String> order) {
        List<S7_1500Model> fetchedData = s7_1500Service.findAllByKey(key, limit, sort, order);

        return ResponseEntity.ok(fetchedData);
    }

    @GetMapping("/latest")
    public ResponseEntity<S7_1500Model> getLatest(@RequestParam Optional<String> key) {
        S7_1500Model fetchedData = s7_1500Service.findLatest(key);

        return ResponseEntity.ok(fetchedData);
    }

    @GetMapping("/period")
    public ResponseEntity<List<S7_1500Model>> getPeriod(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Instant startInstant = start.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = end.atZone(ZoneId.systemDefault()).toInstant();

        List<S7_1500Model> fetchedData = s7_1500Service.findByTimestampBetween(startInstant, endInstant);

        return ResponseEntity.ok(fetchedData);
    }
}
