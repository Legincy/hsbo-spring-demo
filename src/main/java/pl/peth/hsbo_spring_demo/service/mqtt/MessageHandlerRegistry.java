package pl.peth.hsbo_spring_demo.service.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.peth.hsbo_spring_demo.handler.mqtt.TopicSubscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MessageHandlerRegistry {
    private static final Logger log = LoggerFactory.getLogger(MessageHandlerRegistry.class);

    private final List<TopicSubscription> messageHandlers = new CopyOnWriteArrayList<>();

    public MessageHandlerRegistry(List<TopicSubscription> newMessageHandlers) {
        if (newMessageHandlers != null) {
            messageHandlers.addAll(newMessageHandlers);

            for (TopicSubscription handler : messageHandlers) {
                log.debug("Registered handler: {}", handler.getClass().getSimpleName());
            }
        }
    }

    public String[] getAllHandlers() {
        List<String> handlers = new ArrayList<>();

        for (TopicSubscription handler : messageHandlers) {
            String[] subscribedTopics = handler.getTargetTopics();
            for (String topic : subscribedTopics) {
                if (!handlers.contains(topic)) {
                    handlers.add(topic);
                }
            }
        }

        return handlers.toArray(new String[0]);
    }

    public List<TopicSubscription> getHandlersByTopic(String topic) {
        List<TopicSubscription> handlers = new ArrayList<>();

        for (TopicSubscription handler : messageHandlers) {
            if (handler.isResponsible(topic)) {
                handlers.add(handler);
            }
        }

        return handlers;
    }

    public void registerHandler(TopicSubscription handler) {
        if (handler != null && !messageHandlers.contains(handler)) {
            messageHandlers.add(handler);
            log.debug("Registered handler: {}", handler.getClass().getSimpleName());
        } else {
            log.warn("Handler is null or already registered: {}", handler);
        }
    }

    public void unregisterHandler(TopicSubscription handler) {
        if (handler != null && messageHandlers.remove(handler)) {
            log.debug("Unregistered handler: {}", handler.getClass().getSimpleName());
        } else {
            log.warn("Handler is null or not registered: {}", handler);
        }
    }
}
