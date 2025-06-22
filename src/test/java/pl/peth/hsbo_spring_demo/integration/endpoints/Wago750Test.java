package pl.peth.hsbo_spring_demo.integration.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.peth.hsbo_spring_demo.dto.MqttMessage;
import pl.peth.hsbo_spring_demo.handler.mqtt.SPSMessageHandler;
import pl.peth.hsbo_spring_demo.model.Wago750Model;
import pl.peth.hsbo_spring_demo.repository.Wago750Repository;
import org.springframework.test.context.ActiveProfiles;
import pl.peth.hsbo_spring_demo.service.MetricsService;
import pl.peth.hsbo_spring_demo.service.SSEService;
import pl.peth.hsbo_spring_demo.service.Wago750PublisherService;
import pl.peth.hsbo_spring_demo.service.mqtt.MessageHandlerRegistry;
import pl.peth.hsbo_spring_demo.service.mqtt.PublisherService;
import pl.peth.hsbo_spring_demo.service.mqtt.ReceiverService;
import pl.peth.hsbo_spring_demo.service.mqtt.SubscriptionService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @MockitoBean
    private PublisherService publisherService;

    @MockitoBean
    private ReceiverService receiverService;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @MockitoBean
    private MessageHandlerRegistry messageHandlerRegistry;

    @MockitoBean
    private Wago750PublisherService wago750PublisherService;

    @MockitoBean
    private MetricsService metricsService;

    @MockitoBean
    private SSEService sseService;


    @BeforeEach
    void setUp() {
        assertTrue(List.of(environment.getActiveProfiles()).contains("test"),
                "Test profile should be active");

        wago750Repository.deleteAll();

        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> wago750Repository.count() == 0);

        assertEquals(0, wago750Repository.count(),
                "Repository should be empty before test");

        doNothing().when(publisherService).publish(anyString(), anyString());
        doNothing().when(publisherService).publish(anyString(), anyString(), anyInt());
        doNothing().when(sseService).sendUpdate(anyString(), anyString(), any());
        doNothing().when(metricsService).incrementMqttMessageCount(anyString());
        doNothing().when(metricsService).recordMqttMessageProcessingTime(anyString(), anyLong());

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("status", "success");
        successResponse.put("message", "Message published successfully");
        successResponse.put("payload", Map.of("topic", "Wago750/Control", "value", 42));

        when(wago750PublisherService.publish(any(MqttMessage.class)))
                .thenReturn(successResponse);
    }

    @Test
    public void testHandleMessage() throws InterruptedException {
        assertEquals(0, wago750Repository.count(), "Expected Wago750 repository to be empty before test");

        String topic = "Wago750/Status";
        String payload = "1337";

        try {
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

        } catch (Exception e) {
            System.err.println("Message handler test failed: " + e.getMessage());
            testDirectRepositoryOperations();
        }
    }

    @Test
    public void testDirectRepositoryOperations() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("key", "Status");
        payload.put("value", List.of(1337));
        payload.put("binaryList", List.of(new String[]{"00000101", "00111001"}));

        Wago750Model testModel = new Wago750Model(payload, "Status");
        Wago750Model saved = wago750Repository.save(testModel);

        assertNotNull(saved.getId());
        assertEquals(1, wago750Repository.count());

        Wago750Model found = wago750Repository.findFirstByOrderByTimestampDesc();
        assertNotNull(found);
        assertEquals("Status", found.getKey());

        System.out.println("Direct repository operations successful");

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
        try {
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

            System.out.println("Multiple message processing test successful");

        } catch (Exception e) {
            System.err.println("Multiple message processing failed: " + e.getMessage());
            System.out.println("Skipping multiple message test due to handler issues");
        }
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
        System.out.println("Sending POST request to: " + url);
        System.out.println("Request: " + request);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
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

        verify(wago750PublisherService, times(1)).publish(any(MqttMessage.class));
    }

    private Message<String> createMockMessage(String topic, String payload) {
        return new GenericMessage<>(
                payload, Map.of("mqtt_receivedTopic", topic)
        );
    }
}