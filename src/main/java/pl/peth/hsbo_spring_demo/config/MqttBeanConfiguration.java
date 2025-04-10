package pl.peth.hsbo_spring_demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import pl.peth.hsbo_spring_demo.factory.MqttConnectionFactory;
import pl.peth.hsbo_spring_demo.handler.MqttMessageHandler;

@Configuration
public class MqttBeanConfiguration {
    private final MqttConfiguration mqttConfiguration;
    private final MqttConnectionFactory mqttConnectionFactory;

    public MqttBeanConfiguration(MqttConfiguration mqttConfiguration, MqttConnectionFactory mqttConnectionFactory) {
        this.mqttConfiguration = mqttConfiguration;
        this.mqttConnectionFactory = mqttConnectionFactory;
    }

    @Bean
    public MessageChannel mqttInputChannel(){
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                mqttConnectionFactory.getClientId(),
                mqttConnectionFactory.mqttClientFactory(),
                mqttConfiguration.getTopics()
        );

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(mqttConfiguration.getQualityOfService());
        adapter.setOutputChannel(mqttInputChannel());

        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler messageHandler(){
        return new MqttMessageHandler();
    }


}
