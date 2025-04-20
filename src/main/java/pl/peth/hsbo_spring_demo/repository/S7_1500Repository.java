package pl.peth.hsbo_spring_demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.peth.hsbo_spring_demo.model.S7_1500Data;

public interface S7_1500Repository extends MongoRepository<S7_1500Data, String> {
    // Custom query methods can be defined here if needed
    // For example:
    // List<S7_1500Data> findBySomeField(String someField);
}
