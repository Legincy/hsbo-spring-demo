package pl.peth.hsbo_spring_demo.integration.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.peth.hsbo_spring_demo.dto.MqttMessage;
import pl.peth.hsbo_spring_demo.handler.mqtt.SPSMessageHandler;
import pl.peth.hsbo_spring_demo.model.Wago750Model;
import pl.peth.hsbo_spring_demo.repository.Wago750Repository;
import org.springframework.test.context.ActiveProfiles;


import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class Wago750Test {
    @Container
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:5.0")
            .withStartupTimeout(Duration.ofMinutes(2))
            .withReuse(false);

    @DynamicPropertySource
    static void mongoProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SPSMessageHandler spsMessageHandler;

    @Autowired
    private Wago750Repository wago750Repository;

    @Autowired
    private Environment environment;

    @BeforeEach
    void setUp() {
        assertTrue(List.of(environment.getActiveProfiles()).contains("test"),
                "Test profile should be active");

        wago750Repository.deleteAll();

        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> wago750Repository.count() == 0);

        assertEquals(0, wago750Repository.count(),
                "Repository should be empty before test");
    }

    @Test
    public void testHandleMessage() throws InterruptedException {
        assertEquals(0, wago750Repository.count(), "Expected Wago750 repository to be empty before test");

        String topic = "Wago750/Status";
        String payload = "1337";

        spsMessageHandler.handleMessage(createMockMessage(topic, payload));

        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> wago750Repository.count() > 0);

        List<Wago750Model> fetchedData = wago750Repository.findAll();
        assertFalse(fetchedData.isEmpty(), "Fetched data should not be empty");

        Wago750Model firstEntry = fetchedData.getFirst();
        assertEquals("Status", firstEntry.getKey());
        assertNotNull(firstEntry.getPayload().get("value"));

        System.out.println("Stored payload: " + firstEntry.getPayload());

        testRestApiEndpoints();
    }

    private void testRestApiEndpoints() {
        String url = "http://localhost:" + port + "/api/v1/wago750?key=Status";
        ResponseEntity<Wago750Model[]> httpResponse = restTemplate.getForEntity(url, Wago750Model[].class);

        assertEquals(HttpStatusCode.valueOf(200), httpResponse.getStatusCode());
        assertNotNull(httpResponse.getBody());
        assertTrue(httpResponse.getBody().length > 0);
        assertEquals("Status", httpResponse.getBody()[0].getPayload().get("key"));

        String latestUrl = "http://localhost:" + port + "/api/v1/wago750/latest";
        ResponseEntity<Wago750Model> latestResponse = restTemplate.getForEntity(latestUrl, Wago750Model.class);

        assertEquals(HttpStatusCode.valueOf(200), latestResponse.getStatusCode());
        assertNotNull(latestResponse.getBody());
        assertEquals("Status", latestResponse.getBody().getPayload().get("key"));
    }

    @Test
    public void testMultipleMessageProcessing() {
        String topic = "Wago750/Status";

        for (int i = 1; i <= 3; i++) {
            String payload = String.valueOf(1000 + i);
            spsMessageHandler.handleMessage(createMockMessage(topic, payload));
        }

        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> wago750Repository.count() >= 3);

        List<Wago750Model> allData = wago750Repository.findAll();
        assertEquals(3, allData.size(), "Should have processed 3 messages");

        Wago750Model latest = wago750Repository.findFirstByOrderByTimestampDesc();
        assertNotNull(latest);
        assertEquals("Status", latest.getKey());
    }

    @Test
    public void testPostRequestTriggersWago750() {
        MqttMessage controlMessage = new MqttMessage();
        controlMessage.setTopic("Control");
        controlMessage.setValue(42);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MqttMessage> request = new HttpEntity<>(controlMessage, headers);

        String url = "http://localhost:" + port + "/api/v1/wago750/control";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        System.out.println("Response: " + response.getBody());

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> responseBody = response.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals("Message published successfully", responseBody.get("message"));

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) responseBody.get("payload");
        assertEquals("Wago750/Control", payload.get("topic"));
        assertEquals(42, payload.get("value"));

        /*
        MqttMessage invalidMessage = new MqttMessage();
        invalidMessage.setTopic("Control");
        invalidMessage.setValue(null);

        HttpEntity<MqttMessage> invalidRequest = new HttpEntity<>(invalidMessage, headers);
        ResponseEntity<Map> invalidResponse = restTemplate.postForEntity(url, invalidRequest, Map.class);

        assertEquals(200, invalidResponse.getStatusCodeValue());
        Map<String, Object> invalidResponseBody = invalidResponse.getBody();
        assertEquals("error", invalidResponseBody.get("status"));
        assertEquals("Payload is empty", invalidResponseBody.get("message"));
        */
    }

    private Message<String> createMockMessage(String topic, String payload) {
        return new GenericMessage<>(
                payload, Map.of("mqtt_receivedTopic", topic)
        );
    }
}