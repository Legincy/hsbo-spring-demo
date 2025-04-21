package pl.peth.hsbo_spring_demo.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "random")
public class RandomModel {
    private String id;
    private Map<String, String> payload;
    private Instant timestamp;
    private String key;

    public RandomModel() {
        this.timestamp = Instant.now();
    }

    public RandomModel(Map<String, String> payload) {
        this();
        this.payload = payload;
    }

    public RandomModel(Map<String, String> payload, String key) {
        this(payload);
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, String> payload) {
        this.payload = payload;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
