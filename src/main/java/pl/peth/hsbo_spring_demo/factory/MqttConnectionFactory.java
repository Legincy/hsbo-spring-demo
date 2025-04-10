package pl.peth.hsbo_spring_demo.factory;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.stereotype.Component;
import pl.peth.hsbo_spring_demo.config.MqttConfiguration;

@Component
public class MqttConnectionFactory {
    private static final Logger log = LoggerFactory.getLogger(MqttConnectionFactory.class);

    private final MqttConfiguration mqttConfiguration;

    public MqttConnectionFactory(MqttConfiguration mqttConfiguration) {
        this.mqttConfiguration = mqttConfiguration;

        log.info("MQTT broker URL configured: {}", mqttConfiguration.getBrokerUrl());
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        String brokerUrl = String.format("tcp://%s:%d", mqttConfiguration.getHost(), mqttConfiguration.getPort());
        options.setServerURIs(new String[]{brokerUrl});
        options.setCleanSession(true);

        options.setAutomaticReconnect(true);
        options.setKeepAliveInterval(30);
        options.setConnectionTimeout(10);

        String username = mqttConfiguration.getUsername();
        String password = mqttConfiguration.getPassword();
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }

        factory.setConnectionOptions(options);
        return factory;
    }

}
