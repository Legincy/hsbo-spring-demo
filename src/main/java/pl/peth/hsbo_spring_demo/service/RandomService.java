package pl.peth.hsbo_spring_demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.peth.hsbo_spring_demo.model.RandomModel;
import pl.peth.hsbo_spring_demo.repository.RandomRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RandomService {
    private static final Logger log = LoggerFactory.getLogger(RandomService.class);

    private final RandomRepository randomRepository;

    public RandomService(RandomRepository randomRepository) {
        this.randomRepository = randomRepository;
    }

    public void save(RandomModel data) {
        randomRepository.save(data);
    }

    public List<RandomModel> findAllByKey(Optional<String> key) {
        if (key.isEmpty()) {
            return randomRepository.findAll();
        }
        return randomRepository.findAllByKey(key.get());
    }

    public RandomModel findFirstByOrderByTimestampDesc() {
        return randomRepository.findFirstByOrderByTimestampDesc();
    }

    public List<RandomModel> findByTimestampBetween(java.time.Instant startInstant, java.time.Instant endInstant) {
        return randomRepository.findByTimestampBetween(startInstant, endInstant);
    }
}
