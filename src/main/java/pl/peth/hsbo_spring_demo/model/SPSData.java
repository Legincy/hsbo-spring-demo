package pl.peth.hsbo_spring_demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "sps_data")
public class SPSData {
    @Id
    private String id;
    private String source;
    private String topic;
    private String payload;
    private String key;
    private Instant timestamp;

    public SPSData() {
        this.timestamp = Instant.now();
    }

    public SPSData(String source, String topic, String payload) {
        this();
        this.source = source;
        this.topic = topic;
        this.payload = payload;

        String[] topicParts = topic.split("/");
        this.key = topicParts[topicParts.length - 1];
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String toString() {
        return String.format("SPSData{id='%s', source='%s', topic='%s', key='%s', payload='%s', timestamp=%s}", id, source, topic, key, payload, timestamp);
    }
}
