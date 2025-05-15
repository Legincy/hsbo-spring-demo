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

    public void incrementMqttMessageCount(String topic) {
        getOrCreateCounter("mqtt.messages.count", "topic", topic).increment();
    }

    public void recordMqttMessageProcessingTime(String topic, long timeInMs) {
        getOrCreateTimer("mqtt.messages.processing.time", "topic", topic)
                .record(timeInMs, TimeUnit.MILLISECONDS);
    }

    public void incrementApiCallCount(String endpoint) {
        getOrCreateCounter("api.calls.count", "endpoint", endpoint).increment();
    }

    public Timer.Sample startApiCallTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopApiCallTimer(Timer.Sample sample, String endpoint) {
        sample.stop(getOrCreateTimer("api.calls.time", "endpoint", endpoint));
    }

    private Counter getOrCreateCounter(String name, String tagKey, String tagValue) {
        String key = name + "." + tagKey + "." + tagValue;
        return counters.computeIfAbsent(key, k ->
                Counter.builder(name)
                        .tag(tagKey, tagValue)
                        .description("Counter for " + name)
                        .register(meterRegistry)
        );
    }

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
