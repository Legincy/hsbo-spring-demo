package pl.peth.hsbo_spring_demo.service;

import org.springframework.stereotype.Service;
import pl.peth.hsbo_spring_demo.model.Wago750Data;
import pl.peth.hsbo_spring_demo.repository.Wago750Repository;

@Service
public class Wago750Service {
    private final Wago750Repository wago750Repository;

    public Wago750Service(Wago750Repository wago750Repository) {
        this.wago750Repository = wago750Repository;
    }

    public void save(Wago750Data data) {
        wago750Repository.save(data);
    }
}
