package pl.peth.hsbo_spring_demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Service;
import pl.peth.hsbo_spring_demo.config.MqttConfiguration;
import pl.peth.hsbo_spring_demo.factory.MqttConnectionFactory;
import pl.peth.hsbo_spring_demo.handler.MqttMessageHandler;

/**
 * This Service is responsible for receiving messages from a MQTT broker.
 */
@Service
public class MqttReceiverService {
    static private final Logger log = LoggerFactory.getLogger(MqttReceiverService.class);

    private final MqttConfiguration mqttConfiguration;
    private final MqttConnectionFactory mqttConnectionFactory;

    private final MessageChannel mqttInputChannel;


    public MqttReceiverService(MqttConfiguration mqttConfiguration, MqttConnectionFactory mqttConnectionFactory) {
        this.mqttConfiguration = mqttConfiguration;
        this.mqttConnectionFactory = mqttConnectionFactory;

        this.mqttInputChannel = new DirectChannel();
    }

    /**
     * This method creates a bean of type MessageProducer and binds it to the MQTT input channel.
     *
     * @return a new instance of DirectChannel
     */
    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                mqttConfiguration.getClientId(),
                mqttConnectionFactory.mqttClientFactory(),
                mqttConfiguration.getTopics()
        );

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(mqttConfiguration.getQualityOfService());
        adapter.setOutputChannel(this.mqttInputChannel);

        return adapter;
    }

    /**
     * This method returns a local initialized instance of DirectChannel that is used to
     * receive messages from the MQTT broker.
     *
     * @return local initialized instance of DirectChannel
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return this.mqttInputChannel;
    }

    /**
     * This method handles incoming messages from MQTT input channel and processes them.
     *
     * @return a new instance of MqttMessageHandler
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler messageHandler(){
        return new MqttMessageHandler();
    }
}
