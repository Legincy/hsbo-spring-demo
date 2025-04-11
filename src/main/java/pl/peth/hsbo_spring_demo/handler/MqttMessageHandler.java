package pl.peth.hsbo_spring_demo.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * This class is responsible for handling incoming MQTT messages.
 * It implements the MessageHandler interface and processes messages received from the MQTT broker.
 */
@Component
public class MqttMessageHandler implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(MqttMessageHandler.class);

    /**
     * This method is called when a message is received from the MQTT broker.
     * It extracts the topic and payload from the message and logs them.
     *
     * @param message the incoming message
     * @throws MessagingException if an error occurs while processing the message
     */
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = Objects.requireNonNull(message.getHeaders().get("mqtt_receivedTopic")).toString();
        String payload = message.getPayload().toString();

        log.debug("Received message on topic {}: {}", topic, payload);
    }
}
