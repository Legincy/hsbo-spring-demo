package pl.peth.hsbo_spring_demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    /**
     * This method creates a bean of type MqttConfiguration and binds it to the properties
     * prefixed with "mqtt" in the application properties file.
     *
     * @return a new instance of MqttConfiguration
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.data.mqtt")
    public MqttConfiguration mqttConfig() {
        return new MqttConfiguration();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.data.mongodb")
    public DatabaseConfiguration databaseConfig() {
        return new DatabaseConfiguration();
    }

}
