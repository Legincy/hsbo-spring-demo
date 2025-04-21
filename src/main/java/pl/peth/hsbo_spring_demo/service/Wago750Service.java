package pl.peth.hsbo_spring_demo.service;

import org.springframework.stereotype.Service;
import pl.peth.hsbo_spring_demo.model.Wago750Model;
import pl.peth.hsbo_spring_demo.repository.Wago750Repository;

import java.util.List;
import java.util.Optional;

@Service
public class Wago750Service {
    private final Wago750Repository wago750Repository;

    public Wago750Service(Wago750Repository wago750Repository) {
        this.wago750Repository = wago750Repository;
    }

    public void save(Wago750Model data) {
        wago750Repository.save(data);
    }

    public List<Wago750Model> findAllByKey(Optional<String> key) {
        if (key.isEmpty()) {
            return wago750Repository.findAll();
        }

        return wago750Repository.findAllByKey(key.get());
    }

    public Wago750Model findFirstByOrderByTimestampDesc() {
        return wago750Repository.findFirstByOrderByTimestampDesc();
    }

    public List<Wago750Model> findByTimestampBetween(java.time.Instant startInstant, java.time.Instant endInstant) {
        return wago750Repository.findByTimestampBetween(startInstant, endInstant);
    }
}
