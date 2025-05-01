package pl.peth.hsbo_spring_demo.handler.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import pl.peth.hsbo_spring_demo.config.MqttConfiguration;
import pl.peth.hsbo_spring_demo.model.RandomModel;
import pl.peth.hsbo_spring_demo.model.S7_1500Model;
import pl.peth.hsbo_spring_demo.model.SPSDataModel;
import pl.peth.hsbo_spring_demo.model.Wago750Model;
import pl.peth.hsbo_spring_demo.service.*;

import java.util.HashMap;
import java.util.List;
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
            //"Random/+"
    };

    private final SPSDataService spsDataService;
    private final Wago750Service wago750Service;
    private final S7_1500Service s7_1500DataService;
    private final RandomService randomService;
    private final MqttConfiguration mqttConfiguration;
    private final SSEService sseService;

    public SPSMessageHandler(SPSDataService spsDataService, Wago750Service wago750Service, S7_1500Service s71500DataService, RandomService randomService, MqttConfiguration mqttConfiguration, SSEService sseService) {
        this.spsDataService = spsDataService;
        this.wago750Service = wago750Service;
        s7_1500DataService = s71500DataService;
        this.randomService = randomService;
        this.mqttConfiguration = mqttConfiguration;
        this.sseService = sseService;
    }

    @Override
    public void handleMessage(String topic, String payload, Message<?> message) throws MessagingException {
        String source = extractSource(topic);

        SPSDataModel spsDataModel = new SPSDataModel(source, topic, payload);
        //spsDataService.save(spsData);

        String key = spsDataModel.getKey();
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("key", key);

        switch (source){
            case "Wago750" -> {
                String rawValue = spsDataModel.getPayload().replace("[", "").replace("]", "").trim();
                int value = Integer.parseInt(rawValue);

                byte[] bytes = new byte[2];
                bytes[0] = (byte) ((value >> 8) & 0xFF);
                bytes[1] = (byte) (value & 0xFF);

                String highByteBinary = String.format("%8s", Integer.toBinaryString(bytes[0] & 0xFF)).replace(' ', '0');
                String lowByteBinary = String.format("%8s", Integer.toBinaryString(bytes[1] & 0xFF)).replace(' ', '0');

                payloadMap.put("value", List.of(value));
                payloadMap.put("binaryList", List.of(new String[] { highByteBinary, lowByteBinary }));

                Wago750Model wago750Model = new Wago750Model(payloadMap, key);
                sseService.sendWago750Update(wago750Model);
                wago750Service.save(wago750Model);
            }
            case "S7_1500" -> {
                payloadMap.put("value", Float.parseFloat(spsDataModel.getPayload()));

                S7_1500Model s7_1500Model = new S7_1500Model(payloadMap, key);
                s7_1500DataService.save(s7_1500Model);
            }
            case "Random" -> {
                if (mqttConfiguration.isIgnoreRandomGenerator()) {
                    return;
                }
                payloadMap.put("value", spsDataModel.getPayload());

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
