package pl.peth.hsbo_spring_demo.handler.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import pl.peth.hsbo_spring_demo.config.MqttConfiguration;
import pl.peth.hsbo_spring_demo.model.RandomModel;
import pl.peth.hsbo_spring_demo.model.S7_1500Model;
import pl.peth.hsbo_spring_demo.model.SPSDataModel;
import pl.peth.hsbo_spring_demo.model.Wago750Model;
import pl.peth.hsbo_spring_demo.service.*;

import java.util.Arrays;
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
            "Wago750/+",
            "S7_1500/Temperatur/+",
            //"Random/+"
    };

    private final SPSDataService spsDataService;
    private final Wago750Service wago750Service;
    private final S7_1500Service s7_1500DataService;
    private final RandomService randomService;
    private final MqttConfiguration mqttConfiguration;
    private final SSEService sseService;
    private final MetricsService metricsService;
    private final Environment environment;

    public SPSMessageHandler(SPSDataService spsDataService, Wago750Service wago750Service,
                             S7_1500Service s71500DataService, RandomService randomService,
                             MqttConfiguration mqttConfiguration, SSEService sseService,
                             MetricsService metricsService, Environment environment) {
        this.spsDataService = spsDataService;
        this.wago750Service = wago750Service;
        this.s7_1500DataService = s71500DataService;
        this.randomService = randomService;
        this.mqttConfiguration = mqttConfiguration;
        this.sseService = sseService;
        this.metricsService = metricsService;
        this.environment = environment;
    }


    /**
     * Handles incoming MQTT messages, processes them based on the source and key,
     * and saves the data to the appropriate service.
     *
     * @param message the incoming MQTT message
     * @throws MessagingException if an error occurs while processing the message
     */
    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        long now = System.currentTimeMillis();
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
        String payload = (String) message.getPayload();

        try {
            metricsService.incrementMqttMessageCount(topic);

            String source = extractSource(topic);
            SPSDataModel spsDataModel = new SPSDataModel(source, topic, payload);
            String key = spsDataModel.getKey().trim();
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("key", key);

            switch (source){
                case "Wago750" -> handleWago750Message(spsDataModel, key, payloadMap);
                case "S7_1500" -> handleS7_1500Message(spsDataModel, key, payloadMap);
                case "Random" -> handleRandomMessage(spsDataModel, key, payloadMap);
                default -> log.warn("Unknown source: {}", source);
            }

            long processingTime = System.currentTimeMillis() - now;
            metricsService.recordMqttMessageProcessingTime(topic, processingTime);

        } catch (Exception e) {
            log.error("Error processing message for topic {}: {}", topic, e.getMessage(), e);

            if (!isTestEnvironment()) {
                throw new MessagingException("Failed to process MQTT message", e);
            }
        }
    }

    /**
     * Handles messages from the Wago750 source, processes the payload,
     * and saves the data to the Wago750 service.
     *
     * @param spsDataModel the SPS data model containing the message details
     * @param key          the key extracted from the topic
     * @param payloadMap   a map to hold processed payload data
     */
    private void handleWago750Message(SPSDataModel spsDataModel, String key, Map<String, Object> payloadMap) {
        try {
            if(key.equals("Control")){
                String rawValue = spsDataModel.getPayload().trim();
                int value = Integer.parseInt(rawValue);
                payloadMap.put("value", value);
            } else if (key.equals("Status")) {
                String rawValue = spsDataModel.getPayload().replace("[", "").replace("]", "").trim();
                int value = Integer.parseInt(rawValue);

                byte[] bytes = new byte[2];
                bytes[0] = (byte) ((value >> 8) & 0xFF);
                bytes[1] = (byte) (value & 0xFF);

                String highByteBinary = String.format("%8s", Integer.toBinaryString(bytes[0] & 0xFF)).replace(' ', '0');
                String lowByteBinary = String.format("%8s", Integer.toBinaryString(bytes[1] & 0xFF)).replace(' ', '0');

                payloadMap.put("value", List.of(value));
                payloadMap.put("binaryList", List.of(new String[] { highByteBinary, lowByteBinary }));
            }

            Wago750Model wago750Model = new Wago750Model(payloadMap, key);

            try {
                sseService.sendUpdate("wago750", "data", wago750Model);
            } catch (Exception e) {
                log.warn("Failed to send SSE update: {}", e.getMessage());
            }

            try {
                wago750Service.save(wago750Model);
            } catch (Exception e) {
                log.error("Failed to save Wago750 data: {}", e.getMessage());
                if (!isTestEnvironment()) {
                    throw e;
                }
            }
        } catch (NumberFormatException e) {
            log.error("Invalid number format in Wago750 payload: {}", spsDataModel.getPayload());
        }
    }

    /**
     * Handles messages from the S7_1500 source, processes the payload,
     * and saves the data to the S7_1500 service.
     *
     * @param spsDataModel the SPS data model containing the message details
     * @param key          the key extracted from the topic
     * @param payloadMap   a map to hold processed payload data
     */
    private void handleS7_1500Message(SPSDataModel spsDataModel, String key, Map<String, Object> payloadMap) {
        try {
            payloadMap.put("value", Float.parseFloat(spsDataModel.getPayload()));
            S7_1500Model s7_1500Model = new S7_1500Model(payloadMap, key);

            try {
                s7_1500DataService.save(s7_1500Model);
            } catch (Exception e) {
                log.error("Failed to save S7_1500 data: {}", e.getMessage());
                if (!isTestEnvironment()) {
                    throw e;
                }
            }
        } catch (NumberFormatException e) {
            log.error("Invalid float format in S7_1500 payload: {}", spsDataModel.getPayload());
        }
    }

    /**
     * Handles messages from the Random source, processes the payload,
     * and saves the data to the Random service if configured to do so.
     *
     * @param spsDataModel the SPS data model containing the message details
     * @param key          the key extracted from the topic
     * @param payloadMap   a map to hold processed payload data
     */
    private void handleRandomMessage(SPSDataModel spsDataModel, String key, Map<String, Object> payloadMap) {
        if (mqttConfiguration.isIgnoreRandomGenerator()) {
            return;
        }

        payloadMap.put("value", spsDataModel.getPayload());
        RandomModel randomModel = new RandomModel(payloadMap, key);

        try {
            randomService.save(randomModel);
        } catch (Exception e) {
            log.error("Failed to save Random data: {}", e.getMessage());
            if (!isTestEnvironment()) {
                throw e;
            }
        }
    }

    /**
     * Checks if the application is running in a test environment.
     *
     * @return true if the active profile is "test", false otherwise
     */
    private boolean isTestEnvironment() {
        return Arrays.asList(environment.getActiveProfiles()).contains("test");
    }

    /**
     * Returns the MQTT topics that this handler is subscribed to.
     *
     * @return an array of topic strings
     */
    @Override
    public String[] getTargetTopics() {
        return SUBSCRIPTIONS;
    }

    /**
     * Checks if this handler is responsible for processing messages from the given topic.
     *
     * @param topic the MQTT topic to check
     * @return true if the topic matches one of the subscriptions, false otherwise
     */
    @Override
    public boolean isResponsible(String topic) {
        for (String subscription : SUBSCRIPTIONS) {
            if (mqttTopicMatches(subscription, topic)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts the source from the MQTT topic using a regex pattern.
     *
     * @param topic the MQTT topic string
     * @return the extracted source, or "unknown" if no match is found
     */
    private String extractSource(String topic) {
        Matcher matcher = SOURCE_PATTERN.matcher(topic);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "unknown";
    }

    /**
     * Checks if the MQTT topic matches the subscription pattern.
     *
     * @param subscription the subscription pattern
     * @param actualTopic  the actual MQTT topic
     * @return true if the topic matches the subscription, false otherwise
     */
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