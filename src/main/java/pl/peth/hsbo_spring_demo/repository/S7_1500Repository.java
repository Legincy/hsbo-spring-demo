package pl.peth.hsbo_spring_demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.peth.hsbo_spring_demo.model.S7_1500Model;

import java.time.Instant;
import java.util.List;

public interface S7_1500Repository extends MongoRepository<S7_1500Model, String> {
    S7_1500Model findFirstByOrderByTimestampDesc();
    List<S7_1500Model> findByTimestampBetween(Instant startInstant, Instant endInstant);
    List<S7_1500Model> findByTimestampGreaterThanEqualAndTimestampLessThanEqual(Instant timestampIsGreaterThan, Instant timestampIsLessThan);
    List<S7_1500Model> findByKeyAndTimestampBetween(String key, Instant start, Instant end);
    List<S7_1500Model> findAllByKey(String key);
}
