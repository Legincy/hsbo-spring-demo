package pl.peth.hsbo_spring_demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "s7_1500")
public class S7_1500Data {
    @Id
    private String id;
    private Map<String, String> payload;
    private Instant timestamp;

    public S7_1500Data() {
        this.timestamp = Instant.now();
    }

    public S7_1500Data(Map<String, String> payload) {
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
