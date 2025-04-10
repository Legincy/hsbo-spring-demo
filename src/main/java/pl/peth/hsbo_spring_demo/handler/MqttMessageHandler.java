package pl.peth.hsbo_spring_demo.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Component
public class MqttMessageHandler implements MessageHandler {

    private static final Logger log = LoggerFactory.getLogger(MqttMessageHandler.class);

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
        String payload = message.getPayload().toString();

        log.info("Received message on topic {}: {}", topic, payload);
    }
}
