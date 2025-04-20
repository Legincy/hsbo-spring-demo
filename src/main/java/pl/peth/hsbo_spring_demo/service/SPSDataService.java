package pl.peth.hsbo_spring_demo.service;

import org.springframework.stereotype.Service;
import pl.peth.hsbo_spring_demo.model.SPSData;
import pl.peth.hsbo_spring_demo.repository.SPSDataRepository;

import java.time.Instant;
import java.util.List;

@Service
public class SPSDataService {
    private final SPSDataRepository spsDataRepository;

    public SPSDataService(SPSDataRepository spsDataRepository) {
        this.spsDataRepository = spsDataRepository;
    }

    public SPSData save(SPSData spsData) {
        return spsDataRepository.save(spsData);
    }
    
    public SPSData findById(String id) {
        return spsDataRepository.findById(id).orElse(null);
    }

    public List<SPSData> findByTimeRange(Instant start, Instant end) {
        return spsDataRepository.findByTimestampBetween(start, end);
    }
}
