package pl.peth.hsbo_spring_demo.handler.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import pl.peth.hsbo_spring_demo.model.SPSData;
import pl.peth.hsbo_spring_demo.service.SPSDataService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SPSMessageHandler implements TopicSubscription {
    private static final Logger log = LoggerFactory.getLogger(SPSMessageHandler.class);
    private static final Pattern SOURCE_PATTERN = Pattern.compile("^([^/]+)/.*");
    private static final String[] SUBSCRIPTIONS = {
            "Wago750/Status",
            "S7_1500/Temperatur/+"
    };

    private final SPSDataService spsDataService;

    public SPSMessageHandler(SPSDataService spsDataService) {
        this.spsDataService = spsDataService;
    }

    @Override
    public void handleMessage(String topic, String payload, Message<?> message) throws MessagingException {
        String source = extractSource(topic);

        spsDataService.createAndSave(source, topic, payload);
    }

    @Override
    public String[] getTargetTopics() {
        return SUBSCRIPTIONS;
    }

    @Override
    public boolean isResponsible(String topic) {
        for (String subscription : SUBSCRIPTIONS) {
            if (mqttTopicMatches(subscription, topic)) {
                return true;
            }
        }
        return false;
    }

    private String extractSource(String topic) {
        Matcher matcher = SOURCE_PATTERN.matcher(topic);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "unknown";
    }

    private boolean mqttTopicMatches(String subscription, String actualTopic) {
        if (subscription.equals(actualTopic)) {
            return true;
        }

        if (subscription.endsWith("#")) {
            String prefix = subscription.substring(0, subscription.length() - 1);
            return actualTopic.startsWith(prefix);
        }

        String[] subscriptionSegments = subscription.split("/");
        String[] topicSegments = actualTopic.split("/");

        if (subscriptionSegments.length > topicSegments.length) {
            return false;
        }

        for (int i = 0; i < subscriptionSegments.length; i++) {
            if (subscriptionSegments[i].equals("+")) {
                continue;
            }

            if (subscriptionSegments[i].equals("#")) {
                return true;
            }

            if (!subscriptionSegments[i].equals(topicSegments[i])) {
                return false;
            }
        }

        return subscriptionSegments.length == topicSegments.length;
    }
}
