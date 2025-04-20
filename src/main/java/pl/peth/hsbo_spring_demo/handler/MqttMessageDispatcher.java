package pl.peth.hsbo_spring_demo.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import pl.peth.hsbo_spring_demo.handler.mqtt.TopicSubscription;
import pl.peth.hsbo_spring_demo.service.MessageHandlerRegistry;

import java.util.List;
import java.util.Objects;

@Component
public class MqttMessageDispatcher implements MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(MqttMessageDispatcher.class);

    private final MessageHandlerRegistry messageHandlerRegistry;

    public MqttMessageDispatcher(MessageHandlerRegistry messageHandlerRegistry) {
        this.messageHandlerRegistry = messageHandlerRegistry;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = Objects.requireNonNull(message.getHeaders().get("mqtt_receivedTopic")).toString();
        String payload = message.getPayload().toString();

        List<TopicSubscription> handlers = messageHandlerRegistry.getHandlersByTopic(topic);

        if (!handlers.isEmpty()) {
            for (TopicSubscription handler : handlers) {
                try {
                    handler.handleMessage(topic, payload, message);
                } catch (MessagingException e) {
                    log.error("Error handling message with handler {}: {}", handler.getClass().getSimpleName(), e.getMessage());
                }
            }
        }
    }
}
