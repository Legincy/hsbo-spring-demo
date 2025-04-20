package pl.peth.hsbo_spring_demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import pl.peth.hsbo_spring_demo.handler.MqttMessageDispatcher;

@Configuration
public class MqttChannelConfig {

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler messageHandler(MqttMessageDispatcher dispatcher) {
        return dispatcher;
    }
}