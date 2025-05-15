package pl.peth.hsbo_spring_demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(S7_1500Controller.class);

    private final S7_1500Service s7_1500Service;

    public S7_1500Controller(S7_1500Service s71500Service) {
        this.s7_1500Service = s71500Service;

        log.debug("S7-1500Controller initialized");
    }

    /**
     * Requests all sets from S7-1500 repository and returns it as a list.
     *
     * @param key
     * @param limit
     * @param sort
     * @param order
     * @return ResponseEntity with a list of S7_1500Model objects
     */
    @GetMapping
    public ResponseEntity<List<S7_1500Model>> getAllAdvanced(@RequestParam Optional<String> key, @RequestParam Optional<Integer> limit, @RequestParam Optional<String> sort, @RequestParam Optional<String> order) {
        log.debug("Received GET-Request on /api/v1/s7-1500 with parameters: key={}, limit={}, sort={}, order={}", key.orElse(""), limit.orElse(0), sort.orElse(""), order.orElse(""));
        List<S7_1500Model> fetchedData = s7_1500Service.findAllByKey(key, limit, sort, order);

        return ResponseEntity.ok(fetchedData);
    }

    /**
     * Requests the latest written set from S7-1500 repository and returns it.
     *
     * @param key
     * @return ResponseEntity with a S7_1500Model object
     */
    @GetMapping("/latest")
    public ResponseEntity<S7_1500Model> getLatest(@RequestParam Optional<String> key) {
        log.debug("Received GET-Request on /api/v1/s7-1500/latest with parameters: key={}", key.orElse(""));
        S7_1500Model fetchedData = s7_1500Service.findLatest(key);

        return ResponseEntity.ok(fetchedData);
    }

    /**
     * Requests all sets from S7-1500 repository in a given time period and returns it as a list.
     *
     * @param start
     * @param end
     * @return ResponseEntity with a list of S7_1500Model objects
     */
    @GetMapping("/period")
    public ResponseEntity<List<S7_1500Model>> getPeriod(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.debug("Received GET-Request on /api/v1/s7-1500/period with parameters: start={}, end={}", start, end);
        Instant startInstant = start.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = end.atZone(ZoneId.systemDefault()).toInstant();
        List<S7_1500Model> fetchedData = s7_1500Service.findByTimestampBetween(startInstant, endInstant);

        return ResponseEntity.ok(fetchedData);
    }
}
