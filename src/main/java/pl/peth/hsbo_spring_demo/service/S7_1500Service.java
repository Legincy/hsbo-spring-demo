package pl.peth.hsbo_spring_demo.service;

import org.springframework.stereotype.Service;
import pl.peth.hsbo_spring_demo.model.S7_1500Data;
import pl.peth.hsbo_spring_demo.repository.S7_1500Repository;

@Service
public class S7_1500Service {
    private final S7_1500Repository s7_1500Repository;

    public S7_1500Service(S7_1500Repository s7_1500Repository) {
        this.s7_1500Repository = s7_1500Repository;
    }

    public void save(S7_1500Data data) {
        s7_1500Repository.save(data);
    }
}
