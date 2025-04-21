package pl.peth.hsbo_spring_demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.peth.hsbo_spring_demo.model.RandomModel;

import java.time.Instant;
import java.util.List;
import java.util.Random;

public interface RandomRepository extends MongoRepository<RandomModel, String> {
    RandomModel findFirstByOrderByTimestampDesc();
    List<RandomModel> findByTimestampBetween(Instant startInstant, Instant endInstant);
    List<RandomModel> findByTimestampGreaterThanEqualAndTimestampLessThanEqual(Instant timestampIsGreaterThan, Instant timestampIsLessThan);
    List<RandomModel> findAllByKey(String key);
}
