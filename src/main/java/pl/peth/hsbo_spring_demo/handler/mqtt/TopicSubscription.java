package pl.peth.hsbo_spring_demo.handler.mqtt;

import org.springframework.messaging.Message;

public interface TopicSubscription {
    void handleMessage(Message<?> message);
    boolean isResponsible(String topic);
    String[] getTargetTopics();
}