package pl.peth.hsbo_spring_demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pl.peth.hsbo_spring_demo.model.SPSData;

import java.time.Instant;
import java.util.List;

@Repository
public interface SPSDataRepository extends MongoRepository<SPSData, String> {
    List<SPSData> findBySource(String source);
    List<SPSData> findByTopic(String topic);
    List<SPSData> findByTimestampBetween(Instant start, Instant end);
    List<SPSData> findBySourceAndTimestampBetween(String source, Instant start, Instant end);
}
