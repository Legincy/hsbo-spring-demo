package pl.peth.hsbo_spring_demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.peth.hsbo_spring_demo.model.Wago750Model;
import pl.peth.hsbo_spring_demo.repository.Wago750Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class Wago750Service {
    private static final Logger log = LoggerFactory.getLogger(Wago750Service.class);

    private final Wago750Repository wago750Repository;
    private final List<Wago750Model> batchBuffer = new ArrayList<>();
    private final int batchSize = 8;

    public Wago750Service(Wago750Repository wago750Repository) {
        this.wago750Repository = wago750Repository;
    }

    public void save(Wago750Model data) {
        synchronized (batchBuffer) {
            Instant now = Instant.now();
            batchBuffer.add(data);

            if (batchBuffer.size() >= batchSize) {
                batchBuffer.clear();
            }

            wago750Repository.save(data);
            log.debug("Saved record to Wago750 repository in {} ms", (Instant.now().toEpochMilli() - now.toEpochMilli()));
        }
    }

    public List<Wago750Model> findAllByKey(Optional<String> key) {
        Instant now = Instant.now();
        List<Wago750Model> result = new ArrayList<>();

        if (key.isEmpty()) {
            result = wago750Repository.findAll();

            log.debug("Fetched {} records from Wago750 repository in {} ms", result.size(), (Instant.now().toEpochMilli() - now.toEpochMilli()));

            return result;
        }

        result = wago750Repository.findAllByKey(key.get());

        log.debug("Fetched {} records from Wago750 repository in {} ms", result.size(), (Instant.now().toEpochMilli() - now.toEpochMilli()));

        return result;
    }

    public Wago750Model findFirstByOrderByTimestampDesc() {
        Wago750Model result;
        Instant now = Instant.now();

        if (!batchBuffer.isEmpty()) {
            result = batchBuffer.getLast();

            log.debug("Fetched latest record from Wago750 buffer in {} ms", (Instant.now().toEpochMilli() - now.toEpochMilli()));
            return result;
        }

        result = wago750Repository.findFirstByOrderByTimestampDesc();

        log.debug("Fetched latest record from Wago750 repository in {} ms", (Instant.now().toEpochMilli() - now.toEpochMilli()));

        return result;
    }

    public List<Wago750Model> findByTimestampBetween(java.time.Instant startInstant, java.time.Instant endInstant) {
        Instant now = Instant.now();
        List<Wago750Model> result = new ArrayList<>();

        result = wago750Repository.findByTimestampBetween(startInstant, endInstant);

        log.debug("Fetched {} records from Wago750 repository in {} ms", result.size(), (Instant.now().toEpochMilli() - now.toEpochMilli()));

        return result;
    }
}
