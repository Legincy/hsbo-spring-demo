package pl.peth.hsbo_spring_demo.service.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;
import pl.peth.hsbo_spring_demo.config.MqttConfiguration;

import java.util.UUID;

@Component
public class ReceiverService {
    private static final Logger log = LoggerFactory.getLogger(ReceiverService.class);

    private final MqttConfiguration mqttConfiguration;

    public ReceiverService(MqttConfiguration mqttConfiguration) {
        this.mqttConfiguration = mqttConfiguration;
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setServerURIs(new String[]{mqttConfiguration.getBrokerUrl()});
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setMaxInflight(100);
        options.setKeepAliveInterval(30);
        options.setConnectionTimeout(30);

        String username = mqttConfiguration.getUsername();
        String password = mqttConfiguration.getPassword();
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }

        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public String getClientId() {
        String clientId = mqttConfiguration.getClientId();

        if (clientId == null || clientId.isEmpty()) {
            clientId = "mqtt-client-" + UUID.randomUUID();
        } else if (clientId.endsWith("-")) {
            clientId = clientId + UUID.randomUUID();
        }

        return clientId;
    }
}