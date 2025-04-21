package pl.peth.hsbo_spring_demo.handler.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import pl.peth.hsbo_spring_demo.model.RandomModel;
import pl.peth.hsbo_spring_demo.model.S7_1500Model;
import pl.peth.hsbo_spring_demo.model.SPSDataModel;
import pl.peth.hsbo_spring_demo.model.Wago750Model;
import pl.peth.hsbo_spring_demo.service.RandomService;
import pl.peth.hsbo_spring_demo.service.S7_1500Service;
import pl.peth.hsbo_spring_demo.service.SPSDataService;
import pl.peth.hsbo_spring_demo.service.Wago750Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SPSMessageHandler implements TopicSubscription {
    private static final Logger log = LoggerFactory.getLogger(SPSMessageHandler.class);
    private static final Pattern SOURCE_PATTERN = Pattern.compile("^([^/]+)/.*");
    private static final String[] SUBSCRIPTIONS = {
            "Wago750/Status",
            "S7_1500/Temperatur/+",
            "Random/+"
    };

    private final SPSDataService spsDataService;
    private final Wago750Service wago750Service;
    private final S7_1500Service s7_1500DataService;
    private final RandomService randomService;

    public SPSMessageHandler(SPSDataService spsDataService, Wago750Service wago750Service, S7_1500Service s71500DataService, RandomService randomService) {
        this.spsDataService = spsDataService;
        this.wago750Service = wago750Service;
        s7_1500DataService = s71500DataService;
        this.randomService = randomService;
    }

    @Override
    public void handleMessage(String topic, String payload, Message<?> message) throws MessagingException {
        String source = extractSource(topic);

        SPSDataModel spsDataModel = new SPSDataModel(source, topic, payload);
        //spsDataService.save(spsData);

        String key = spsDataModel.getKey();
        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put(spsDataModel.getKey(), spsDataModel.getPayload());

        switch (source){
            case "Wago750" -> {
                Wago750Model wago750Model = new Wago750Model(payloadMap, key);
                wago750Service.save(wago750Model);
            }
            case "S7_1500" -> {
                S7_1500Model s7_1500Model = new S7_1500Model(payloadMap, key);
                s7_1500DataService.save(s7_1500Model);
            }
            case "Random" -> {
                RandomModel randomModel = new RandomModel(payloadMap, key);
                randomService.save(randomModel);
            }
            default -> log.warn("Unknown source: {}", source);
        }
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
