package pl.peth.hsbo_spring_demo.service;

import org.springframework.stereotype.Service;
import pl.peth.hsbo_spring_demo.cache.ModelCache;
import pl.peth.hsbo_spring_demo.model.S7_1500Model;
import pl.peth.hsbo_spring_demo.model.Wago750Model;
import pl.peth.hsbo_spring_demo.repository.S7_1500Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class S7_1500Service {
    private final S7_1500Repository s7_1500Repository;
    private final ModelCache<S7_1500Model> s7_1500ModelCache;

    public S7_1500Service(S7_1500Repository s7_1500Repository, ModelCache<S7_1500Model> s71500ModelCache) {
        this.s7_1500Repository = s7_1500Repository;
        s7_1500ModelCache = s71500ModelCache;
    }

    public void save(S7_1500Model data) {
        s7_1500ModelCache.put(data.getKey(), data);
        s7_1500Repository.save(data);
    }

    public List<S7_1500Model> findAllByKey(Optional<String> key, Optional<Integer> limit, Optional<String> sort, Optional<String> order) {
        if (key.isEmpty()) {
            return s7_1500Repository.findAll();
        }

        return s7_1500Repository.findAllByKey(key.get());
    }

    public S7_1500Model findLatest(Optional<String> key) {
        return s7_1500Repository.findFirstByOrderByTimestampDesc();
    }

    public List<S7_1500Model> findByTimestampBetween(Instant startInstant, Instant endInstant) {
        return s7_1500Repository.findByTimestampBetween(startInstant, endInstant);
    }

    public List<S7_1500Model> findByKeyAndTimestampBetween(String key, Instant start, Instant end) {
        return s7_1500Repository.findByKeyAndTimestampBetween(key, start, end);
    }
}
