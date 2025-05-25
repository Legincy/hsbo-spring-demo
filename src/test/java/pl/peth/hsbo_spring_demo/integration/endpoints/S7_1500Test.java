package pl.peth.hsbo_spring_demo.integration.endpoints;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.peth.hsbo_spring_demo.handler.mqtt.SPSMessageHandler;
import pl.peth.hsbo_spring_demo.model.S7_1500Model;
import pl.peth.hsbo_spring_demo.repository.S7_1500Repository;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class S7_1500Test {
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

    @Autowired
    private SPSMessageHandler spsMessageHandler;

    @Autowired
    private S7_1500Repository s7_1500Repository;

    @LocalServerPort
    private int port;

    @Autowired
    private Environment environment;

    @BeforeEach
    void setUp() {
        assertTrue(List.of(environment.getActiveProfiles()).contains("test"),
                "Test profile should be active");

        s7_1500Repository.deleteAll();

        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> s7_1500Repository.count() == 0);

        assertEquals(0, s7_1500Repository.count(),
                "Repository should be empty before test");
    }

    @Test
    public void testHandleMessage() throws Exception {
        String sollTopic = "S7_1500/Temperatur/Soll";
        double sollPayload = 22.5;

        String istTopic = "S7_1500/Temperatur/Ist";
        double istPayload = 21.8;

        String differenzTopic = "S7_1500/Temperatur/Differenz";
        double differenzPayload = istPayload - sollPayload;

        spsMessageHandler.handleMessage(createMockMessage(sollTopic, Double.toString(sollPayload)));
        spsMessageHandler.handleMessage(createMockMessage(istTopic, Double.toString(istPayload)));
        spsMessageHandler.handleMessage(createMockMessage(differenzTopic, Double.toString(differenzPayload)));

        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> s7_1500Repository.count() > 0);

        testRestApiEndpoints();
    }

    public void testRestApiEndpoints() {
        String url = "http://localhost:" + port + "/api/v1/s7-1500";
        ResponseEntity<S7_1500Model[]> response = restTemplate.getForEntity(url, S7_1500Model[].class);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0, "Response body should not be empty");

        String sollUrl = "http://localhost:" + port + "/api/v1/s7-1500?key=Soll";
        ResponseEntity<S7_1500Model[]> sollResponse = restTemplate.getForEntity(sollUrl, S7_1500Model[].class);
        System.out.println("Response body 'Soll': " + Arrays.toString(sollResponse.getBody()));

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        assertTrue(sollResponse.getBody().length > 0, "Response body 'Soll' should not be empty");
        assertEquals("Soll", sollResponse.getBody()[0].getPayload().get("key"),
                "First entry should have key 'Soll'");

        String latestUrl = "http://localhost:" + port + "/api/v1/s7-1500/latest";
        ResponseEntity<S7_1500Model> latestResponse = restTemplate.getForEntity(latestUrl, S7_1500Model.class);
        System.out.println("Latest response body: " + latestResponse.getBody());

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        assertNotNull(latestResponse.getBody());

        String latestIstUrl = "http://localhost:" + port + "/api/v1/s7-1500/latest?key=Ist";
        ResponseEntity<S7_1500Model> latestIstResponse = restTemplate.getForEntity(latestIstUrl, S7_1500Model.class);
        System.out.println("Latest 'Ist' response body: " + latestIstResponse.getBody());

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        assertEquals("Ist", latestIstResponse.getBody().getPayload().get("key"),
                "Latest entry should have key 'Ist'");
    }

    @Test
    public void testS7_1500RestApiWithoutData() throws Exception {
        String url = "http://localhost:" + port + "/api/v1/s7-1500";
        ResponseEntity<S7_1500Model[]> response = restTemplate.getForEntity(url, S7_1500Model[].class);

        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }

    private org.springframework.messaging.Message<String> createMockMessage(String topic, String payload) {
        return new GenericMessage<>(
                payload,
                Map.of("mqtt_receivedTopic", topic)
        );
    }
}
