package pl.peth.hsbo_spring_demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pl.peth.hsbo_spring_demo.model.SPSDataModel;

import java.time.Instant;
import java.util.List;

@Repository
public interface SPSDataRepository extends MongoRepository<SPSDataModel, String> {
    List<SPSDataModel> findBySource(String source);
    List<SPSDataModel> findByTopic(String topic);
    List<SPSDataModel> findByTimestampBetween(Instant start, Instant end);
    List<SPSDataModel> findBySourceAndTimestampBetween(String source, Instant start, Instant end);
}
