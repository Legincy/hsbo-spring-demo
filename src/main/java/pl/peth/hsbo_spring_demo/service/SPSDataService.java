package pl.peth.hsbo_spring_demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.peth.hsbo_spring_demo.model.SPSDataModel;
import pl.peth.hsbo_spring_demo.repository.SPSDataRepository;

import java.time.Instant;
import java.util.List;

@Service
public class SPSDataService {
    private static final Logger log = LoggerFactory.getLogger(SPSDataService.class);

    private final SPSDataRepository spsDataRepository;

    public SPSDataService(SPSDataRepository spsDataRepository) {
        this.spsDataRepository = spsDataRepository;
    }

    public SPSDataModel save(SPSDataModel spsDataModel) {
        return spsDataRepository.save(spsDataModel);
    }
    
    public SPSDataModel findById(String id) {
        return spsDataRepository.findById(id).orElse(null);
    }

    public List<SPSDataModel> findByTimeRange(Instant start, Instant end) {
        return spsDataRepository.findByTimestampBetween(start, end);
    }
}
