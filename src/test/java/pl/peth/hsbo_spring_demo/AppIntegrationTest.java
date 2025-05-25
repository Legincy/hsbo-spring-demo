package pl.peth.hsbo_spring_demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.peth.hsbo_spring_demo.handler.mqtt.SPSMessageHandler;
import pl.peth.hsbo_spring_demo.model.Wago750Model;
import pl.peth.hsbo_spring_demo.repository.Wago750Repository;
import org.springframework.test.context.ActiveProfiles;


import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class AppIntegrationTest {
    @Container
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:5.0").withStartupTimeout(Duration.ofMinutes(2))
            .withReuse(false);;

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

    @BeforeEach
    void setUp() {
        wago750Repository.deleteAll();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testMqttStorageAndRetrieval() throws InterruptedException {
        assertEquals(0, wago750Repository.count(), "Expected Wago750 repository to be empty before test");

        String topic = "Wago750/Status";
        String payload = "1337";

        spsMessageHandler.handleMessage(topic, payload, createMockMessage(topic));
        try {
            spsMessageHandler.handleMessage(topic, payload, createMockMessage(topic));
            Thread.sleep(200);
        } catch (Exception e) {
            System.err.println("Error while processing Mqtt message: " + e.getMessage());
        }

        List<Wago750Model> fetchedData = wago750Repository.findAll();
        assertFalse(fetchedData.isEmpty(), "Fetched data should not be empty");

        Wago750Model firstEntry = fetchedData.getFirst();
        assertEquals("Status", firstEntry.getKey());
        assertNotNull(firstEntry.getPayload().get("value"));

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

    private Message<String> createMockMessage(String topic) {
        return new GenericMessage<>(
                "", Map.of("mqtt_receivedTopic", topic)
        );
    }
}