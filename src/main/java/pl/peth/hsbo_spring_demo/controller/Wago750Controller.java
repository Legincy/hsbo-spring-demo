package pl.peth.hsbo_spring_demo.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.peth.hsbo_spring_demo.model.Wago750Model;
import pl.peth.hsbo_spring_demo.service.Wago750Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/wago750")
public class Wago750Controller {
    private final Wago750Service wago750Service;

    public Wago750Controller(Wago750Service wago750Service) {
        this.wago750Service = wago750Service;
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

}
