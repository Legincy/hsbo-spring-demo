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

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        initAdapter();
        subscribeAllRegisteredTopics();
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }

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

    public boolean subscribe(String topic) {
       if (adapter == null) {
           log.error("Adapter is not initialized");
           return false;
       }

       if (!isTopicAlreadySubscribed(topic)) {
            try {
                adapter.addTopic(topic, mqttConfiguration.getQualityOfService());
                return true;
            } catch (Exception e) {
                log.error("Failed to subscribe to topic: {}", topic, e);
                return false;
            }
       }

        log.warn("Topic {} is already subscribed", topic);
        return true;
    }

    public boolean isTopicAlreadySubscribed(String topic) {
        if (adapter == null) {
            log.error("Adapter not initialized");
            return false;
        }

        return subscriptions.containsKey(topic);
    }

    @PreDestroy
    public void cleanup() {
        if (adapter != null) {
            try {
                adapter.stop();
            } catch (Exception e) {
                log.error("Failed to stop MQTT adapter", e);
            }
        }
    }
}
