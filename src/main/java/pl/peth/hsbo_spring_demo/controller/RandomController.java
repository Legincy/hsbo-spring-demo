package pl.peth.hsbo_spring_demo.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.peth.hsbo_spring_demo.model.RandomModel;
import pl.peth.hsbo_spring_demo.service.RandomService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/random")
public class RandomController {
    private final RandomService randomService;

    public RandomController(RandomService randomService) {
        this.randomService = randomService;
    }

    @GetMapping
    public ResponseEntity<List<RandomModel>> getAll(@RequestParam Optional<String> key) {
        List<RandomModel> fetchedData = randomService.findAllByKey(key);
        return ResponseEntity.ok(fetchedData);
    }

    @GetMapping("/latest")
    public ResponseEntity<RandomModel> getLatest() {
        RandomModel fetchedData = randomService.findFirstByOrderByTimestampDesc();
        return ResponseEntity.ok(fetchedData);
    }

    @GetMapping("/period")
    public ResponseEntity<List<RandomModel>> getPeriod(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        Instant startInstant = start.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = end.atZone(ZoneId.systemDefault()).toInstant();

        List<RandomModel> fetchedData = randomService.findByTimestampBetween(startInstant, endInstant);
        return ResponseEntity.ok(fetchedData);
    }
}
