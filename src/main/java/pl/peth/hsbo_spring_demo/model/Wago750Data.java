package pl.peth.hsbo_spring_demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "wago750")
public class Wago750Data {
    @Id
    private String id;
    private Map<String, String> payload;
    private Instant timestamp;

    public Wago750Data() {
        this.timestamp = Instant.now();
    }

    public Wago750Data(Map<String, String> payload) {
        this();
        this.payload = payload;
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}

