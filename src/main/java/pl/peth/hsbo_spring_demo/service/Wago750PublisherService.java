package pl.peth.hsbo_spring_demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.peth.hsbo_spring_demo.dto.MqttMessage;
import pl.peth.hsbo_spring_demo.service.mqtt.PublisherService;

import java.util.HashMap;
import java.util.Map;

@Service
public class Wago750PublisherService {
    private static final Logger log = LoggerFactory.getLogger(Wago750PublisherService.class);

    private final PublisherService publisherService;
    private final String BASE_TOPIC = "Wago750";


    public Wago750PublisherService(PublisherService publisherService) {
        this.publisherService = publisherService;
    }

    public Map<String, Object> publish(MqttMessage data) {
        if (data.getTopic().startsWith("/")) {
            data.setTopic(data.getTopic().substring(1));
        }
        if (data.getTopic().endsWith("/")) {
            data.setTopic(data.getTopic().substring(0, data.getTopic().length() - 1));
        }

        Map<String, Object> result = new HashMap<>();
        String topic = String.format("%s/%s", BASE_TOPIC, data.getTopic());

        result.put("payload", Map.of("topic", topic, "value", data.getValue()));

        if (data.getValue() == null) {
            result.put("status", "error");
            result.put("message", "Payload is empty");
            return result;
        }

        publisherService.publish(topic, data.getValue().toString());
        result.put("status", "success");
        result.put("message", "Message published successfully");

        return result;
    }
}
