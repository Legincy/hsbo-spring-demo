package pl.peth.hsbo_spring_demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "s7_1500")
public class S7_1500Model {
    @Id
    private String id;
    private Map<String, Object> payload;
    private Instant timestamp;
    @JsonIgnore
    private String key;

    public S7_1500Model() {
        this.timestamp = Instant.now();
    }

    public S7_1500Model(Map<String, Object> payload) {
        this();
        this.payload = payload;
    }

    public S7_1500Model(Map<String, Object> payload, String key) {
        this(payload);
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

