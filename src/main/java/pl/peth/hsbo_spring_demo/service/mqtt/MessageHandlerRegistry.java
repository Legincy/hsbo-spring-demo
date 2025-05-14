package pl.peth.hsbo_spring_demo.service.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.peth.hsbo_spring_demo.handler.mqtt.TopicSubscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class MessageHandlerRegistry {
    private static final Logger log = LoggerFactory.getLogger(MessageHandlerRegistry.class);

    private final List<TopicSubscription> messageHandlers = new CopyOnWriteArrayList<>();

    /**
     * Constructor that initializes the registry with a list of TopicSubscriptions beans.
     *
     * @param newMessageHandlers the list of message handlers to register
     */
    public MessageHandlerRegistry(List<TopicSubscription> newMessageHandlers) {
        messageHandlers.addAll(newMessageHandlers);

        for (TopicSubscription handler : messageHandlers) {
            log.debug("Registered handler: {}", handler.getClass().getSimpleName());
        }
    }

    /**
     * Returns all registered message handlers.
     *
     * @return an array of all registered message handlers
     */
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

    /**
     * Returns a list of message handlers responsible for the given topic.
     *
     * @param topic the topic to check
     * @return a list of message handlers responsible for the given topic
     */
    public List<TopicSubscription> getHandlersByTopic(String topic) {
        List<TopicSubscription> handlers = new ArrayList<>();

        for (TopicSubscription handler : messageHandlers) {
            if (handler.isResponsible(topic)) {
                handlers.add(handler);
            }
        }

        return handlers;
    }

    /**
     * Registers a new message handler.
     *
     * @param handler the message handler to register
     */
    public void registerHandler(TopicSubscription handler) {
        if (handler != null && !messageHandlers.contains(handler)) {
            messageHandlers.add(handler);
            log.debug("Registered handler: {}", handler.getClass().getSimpleName());
        } else {
            log.warn("Handler is null or already registered: {}", handler);
        }
    }

    /**
     * Removes a message handler.
     *
     * @param handler the message handler to unregister
     */
    public void removeHandler(TopicSubscription handler) {
        if (handler != null && messageHandlers.remove(handler)) {
            log.debug("Removed handler: {}", handler.getClass().getSimpleName());
        } else {
            log.warn("Handler is null or not registered: {}", handler);
        }
    }
}
