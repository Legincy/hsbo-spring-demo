package pl.peth.hsbo_spring_demo.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class MetricsService {
    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Increment the count of MQTT messages received for a specific topic.
     *
     * @param topic the MQTT topic
     */
    public void incrementMqttMessageCount(String topic) {
        getOrCreateCounter("mqtt.messages.count", "topic", topic).increment();
    }

    /**
     * Record the processing time of MQTT messages for a specific topic.
     *
     * @param topic the MQTT topic
     * @param timeInMs the processing time in milliseconds
     */
    public void recordMqttMessageProcessingTime(String topic, long timeInMs) {
        getOrCreateTimer("mqtt.messages.processing.time", "topic", topic)
                .record(timeInMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Increment the count of API calls for a specific endpoint.
     *
     * @param endpoint the API endpoint
     */
    public void incrementApiCallCount(String endpoint) {
        getOrCreateCounter("api.calls.count", "endpoint", endpoint).increment();
    }

    /**
     * Start a timer for measuring the duration of an API call.
     *
     * @return a Timer.Sample to be used for stopping the timer later
     */
    public Timer.Sample startApiCallTimer() {
        return Timer.start(meterRegistry);
    }


    /**
     * Stop the timer for an API call and record the duration.
     *
     * @param sample the Timer.Sample to stop
     * @param endpoint the API endpoint for which the timer is stopped
     */
    public void stopApiCallTimer(Timer.Sample sample, String endpoint) {
        sample.stop(getOrCreateTimer("api.calls.time", "endpoint", endpoint));
    }

    /**
     * Get or create a counter for counting occurrences of events.
     *
     * @param name
     * @param tagKey
     * @param tagValue
     * @return
     */
    private Counter getOrCreateCounter(String name, String tagKey, String tagValue) {
        String key = name + "." + tagKey + "." + tagValue;
        return counters.computeIfAbsent(key, k ->
                Counter.builder(name)
                        .tag(tagKey, tagValue)
                        .description("Counter for " + name)
                        .register(meterRegistry)
        );
    }

    /**
     * Get or create a timer for measuring the duration of operations.
     *
     * @param name
     * @param tagKey
     * @param tagValue
     * @return
     */
    private Timer getOrCreateTimer(String name, String tagKey, String tagValue) {
        String key = name + "." + tagKey + "." + tagValue;
        return timers.computeIfAbsent(key, k ->
                Timer.builder(name)
                        .tag(tagKey, tagValue)
                        .description("Timer for " + name)
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry)
        );
    }
}
