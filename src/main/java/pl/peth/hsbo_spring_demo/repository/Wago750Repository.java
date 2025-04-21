package pl.peth.hsbo_spring_demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.peth.hsbo_spring_demo.model.Wago750Model;

import java.time.Instant;
import java.util.List;

public interface Wago750Repository  extends MongoRepository<Wago750Model, String> {
    Wago750Model findFirstByOrderByTimestampDesc();
    List<Wago750Model> findByTimestampBetween(Instant startInstant, Instant endInstant);
    List<Wago750Model> findByTimestampIsAfterAndTimestampIsBefore(Instant timestampIsGreaterThan, Instant timestampIsLessThan);
    List<Wago750Model> findAllByKey(String key);
}
