package pl.peth.hsbo_spring_demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig implements WebMvcConfigurer {
    /**
     * This method creates a bean of type MqttConfiguration and binds it to the properties
     * prefixed with "spring.data.mqtt" in the application properties file.
     *
     * @return a new instance of MqttConfiguration
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.data.mqtt")
    public MqttConfiguration mqttConfig() {
        return new MqttConfiguration();
    }

    /**
     * This method creates a bean of type DatabaseConfiguration and binds it to the properties
     * prefixed with "spring.data.mongodb" in the application properties file.
     *
     * @return a new instance of DatabaseConfiguration
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.data.mongodb")
    public DatabaseConfiguration databaseConfig() {
        return new DatabaseConfiguration();
    }

    /**
     * This method configures CORS (Cross-Origin Resource Sharing) settings for the application.
     * It allows requests from "http://localhost:3000" and specifies the allowed HTTP methods,
     * headers, and credentials.
     *
     * @param registry the CORS registry to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:3001")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
