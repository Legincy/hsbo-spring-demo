package pl.peth.hsbo_spring_demo.service.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import pl.peth.hsbo_spring_demo.config.MqttConfiguration;

@Component
public class PublisherService {
    private static final Logger log = LoggerFactory.getLogger(PublisherService.class);

    private final MqttPahoMessageHandler mqttOutboundHandler;

    public PublisherService(MqttConfiguration mqttConfig, ReceiverService receiverService) {
        String clientId = mqttConfig.getClientId() + "-publisher";
        DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();

        this.mqttOutboundHandler = new MqttPahoMessageHandler(clientId, receiverService.mqttClientFactory());
        this.mqttOutboundHandler.setDefaultQos(mqttConfig.getQualityOfService());
        this.mqttOutboundHandler.setAsync(true);
        this.mqttOutboundHandler.setConverter(converter);

        this.mqttOutboundHandler.start();
    }

    /**
     * Publishes a message to the specified MQTT topic.
     * @param topic
     * @param payload
     * @throws RuntimeException if the message could not be published
     */
    public void publish(String topic, String payload) {
        log.debug("Publishing to topic {}: {}", topic, payload);
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(MqttHeaders.TOPIC, topic)
                .build();

        try {
            mqttOutboundHandler.handleMessage(message);
        } catch (Exception e) {
            log.error("Failed to publish message to topic {}: {}", topic, e.getMessage());
            throw new RuntimeException("Failed to publish MQTT message", e);
        }
    }

    /**
     * Publishes a message to the specified MQTT topic with the given QoS level.
     * @param topic
     * @param payload
     * @param qos
     * @throws RuntimeException if the message could not be published
     */
    public void publish(String topic, String payload, int qos) {
        log.debug("Publishing to topic {} with QoS {}: {}", topic, qos, payload);
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(MqttHeaders.TOPIC, topic)
                .setHeader(MqttHeaders.QOS, qos)
                .build();

        try {
            mqttOutboundHandler.handleMessage(message);
        } catch (Exception e) {
            log.error("Failed to publish message to topic {}: {}", topic, e.getMessage());
            throw new RuntimeException("Failed to publish MQTT message", e);
        }
    }
}