package pl.peth.hsbo_spring_demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.peth.hsbo_spring_demo.model.S7_1500Model;
import pl.peth.hsbo_spring_demo.repository.S7_1500Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class S7_1500Service {
    private static final Logger log = LoggerFactory.getLogger(S7_1500Service.class);

    private final S7_1500Repository s7_1500Repository;

    public S7_1500Service(S7_1500Repository s7_1500Repository) {
        this.s7_1500Repository = s7_1500Repository;
    }

    public void save(S7_1500Model data) {
        Instant now = Instant.now();
        s7_1500Repository.save(data);
    }

    public List<S7_1500Model> findAllByKey(Optional<String> key, Optional<Integer> limit, Optional<String> sort, Optional<String> order) {
        Instant now = Instant.now();
        List<S7_1500Model> result = new ArrayList<>();

        if (key.isEmpty()) {
            result = s7_1500Repository.findAll();
            log.debug("Fetched {} records from S7-1500 repository in {} ms", result.size(), (Instant.now().toEpochMilli() - now.toEpochMilli()));

            return result;
        }

        result = s7_1500Repository.findAllByKey(key.get());
        log.debug("Fetched {} records from S7-1500 repository in {} ms", result.size(), (Instant.now().toEpochMilli() - now.toEpochMilli()));

        return  result;
    }

    public S7_1500Model findLatest(Optional<String> key) {
        Instant now = Instant.now();
        S7_1500Model result;

        if (key.isEmpty()) {
            result = s7_1500Repository.findFirstByOrderByTimestampDesc();
            log.debug("Fetched latest record from S7-1500 repository in {} ms", (Instant.now().toEpochMilli() - now.toEpochMilli()));

            return result;
        }

        result = s7_1500Repository.findFirstByKeyOrderByTimestampDesc(key.get());
        log.debug("Fetched latest record from S7-1500 repository in {} ms", (Instant.now().toEpochMilli() - now.toEpochMilli()));

        return result;
    }

    public List<S7_1500Model> findByTimestampBetween(Instant startInstant, Instant endInstant) {
        Instant now = Instant.now();
        List<S7_1500Model> result = s7_1500Repository.findByTimestampBetween(startInstant, endInstant);

        log.debug("Fetched {} records from S7-1500 repository in {} ms", result.size(), (Instant.now().toEpochMilli() - now.toEpochMilli()));

        return result;
    }

    public List<S7_1500Model> findByKeyAndTimestampBetween(String key, Instant start, Instant end) {
        Instant now = Instant.now();
        List<S7_1500Model> result = s7_1500Repository.findByKeyAndTimestampBetween(key, start, end);

        log.debug("Fetched {} records from S7-1500 repository in {} ms", result.size(), (Instant.now().toEpochMilli() - now.toEpochMilli()));

        return result;
    }
}
