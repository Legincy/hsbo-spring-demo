package pl.peth.hsbo_spring_demo.service.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import pl.peth.hsbo_spring_demo.handler.mqtt.TopicSubscription;

import java.util.List;
import java.util.Objects;

@Component
public class MessageDispatcher implements MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(MessageDispatcher.class);

    private final MessageHandlerRegistry messageHandlerRegistry;

    public MessageDispatcher(MessageHandlerRegistry messageHandlerRegistry) {
        this.messageHandlerRegistry = messageHandlerRegistry;
    }

    /**
     * Overrides the default handler and manages incoming messages by dispatching them to the appropriate message handlers based on the topic.
     *
     * @param message the incoming message
     * @throws MessagingException if an error occurs while handling the message
     */
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = Objects.requireNonNull(message.getHeaders().get("mqtt_receivedTopic")).toString();

        List<TopicSubscription> handlers = messageHandlerRegistry.getHandlersByTopic(topic);

        if (handlers.isEmpty()) return;

        for (TopicSubscription handler : handlers) {
            try {
                handler.handleMessage(message);
            } catch (MessagingException e) {
                log.error("Error handling message with handler {}: {}", handler.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
