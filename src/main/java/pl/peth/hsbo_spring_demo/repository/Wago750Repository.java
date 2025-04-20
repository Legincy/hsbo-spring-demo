package pl.peth.hsbo_spring_demo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.peth.hsbo_spring_demo.model.Wago750Data;

public interface Wago750Repository  extends MongoRepository<Wago750Data, String> {
}
