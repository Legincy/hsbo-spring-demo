package pl.peth.hsbo_spring_demo.service.mqtt;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;
import pl.peth.hsbo_spring_demo.config.MqttConfiguration;
import pl.peth.hsbo_spring_demo.handler.mqtt.TopicSubscription;

import java.util.HashMap;
import java.util.Map;

@Component
public class SubscriptionService implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final MqttConfiguration mqttConfiguration;
    private final ReceiverService mqttReceiverService;
    private final MessageHandlerRegistry messageHandlerRegistry;
    private final MessageChannel mqttInputChannel;

    private MqttPahoMessageDrivenChannelAdapter adapter;
    private final Map<String, TopicSubscription> subscriptions = new HashMap<>();

    public SubscriptionService(MqttConfiguration mqttConfiguration,
                               ReceiverService mqttReceiverService,
                               MessageHandlerRegistry messageHandlerRegistry,
                               MessageChannel mqttInputChannel) {
        this.mqttConfiguration = mqttConfiguration;
        this.mqttReceiverService = mqttReceiverService;
        this.messageHandlerRegistry = messageHandlerRegistry;
        this.mqttInputChannel = mqttInputChannel;
    }

    /**
     * Initializes the MQTT adapter and subscribes to all registered topics when the application context is refreshed.
     *
     * @param event the context refreshed event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        initAdapter();
        subscribeAllRegisteredTopics();
    }

    /**
     * Checks if the application supports asynchronous execution.
     *
     * @return true if asynchronous execution is supported, false otherwise
     */
    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }

    /**
     * Initializes the MQTT adapter for receiving messages.
     */
    private void initAdapter() {
        String clientId = mqttReceiverService.getClientId();

        adapter = new MqttPahoMessageDrivenChannelAdapter(
                clientId + "receiver",
                mqttReceiverService.mqttClientFactory(),
                new String[0]
        );

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(mqttConfiguration.getQualityOfService());
        adapter.setOutputChannel(mqttInputChannel);

        try {
            adapter.start();
            log.info("Initialized MQTT adapter with client ID: {}", clientId);
        } catch (Exception e) {
            log.error("Failed to initialize MQTT adapter", e);
        }
    }

    /**
     * Subscribes to all known topics registered in the message handler registry.
     */
    public void subscribeAllRegisteredTopics() {
        String[] topics = messageHandlerRegistry.getAllHandlers();

        if (topics.length > 0) {
            for (String topic : topics) {
                subscribe(topic);
            }
        } else {
            log.warn("No topics registered for subscription");
        }
    }

    /**
     * Subscribes to a specific topic with the default QoS.
     *
     * @param topic the topic to subscribe to
     */
    public void subscribe(String topic) {
        subscribe(topic, mqttConfiguration.getQualityOfService());
    }

    /**
     * Subscribes to a specific topic with the specified QoS.
     *
     * @param topic the topic to subscribe to
     * @param qos   the quality of service level
     */
    public void subscribe(String topic, int qos) {
       if (adapter == null) {
           log.error("Adapter is not initialized");
           return;
       }

       if (!isTopicAlreadySubscribed(topic)) {
            try {
                adapter.addTopic(topic, qos);
            } catch (Exception e) {
                log.error("Failed to subscribe to topic: {}", topic, e);
            }
       }

        log.warn("Topic {} is already subscribed", topic);
    }

    /**
     * Checks if the specified topic is already subscribed.
     *
     * @param topic
     * @return true if the topic is already subscribed, false otherwise
     */
    public boolean isTopicAlreadySubscribed(String topic) {
        if (adapter == null) {
            log.error("Adapter not initialized");
            return false;
        }

        return subscriptions.containsKey(topic);
    }

    /**
     * Cleans up the MQTT adapter when the application context is destroyed.
     */
    @PreDestroy
    public void cleanup() {
        if (adapter == null){
            return;
        }

        try {
            adapter.stop();
        } catch (Exception e) {
            log.error("Failed to stop MQTT adapter", e);
        }
    }
}
