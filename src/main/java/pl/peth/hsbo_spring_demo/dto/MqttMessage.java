package pl.peth.hsbo_spring_demo.dto;

import java.util.Map;

public class MqttMessage {
    private String topic;
    private Object value;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
