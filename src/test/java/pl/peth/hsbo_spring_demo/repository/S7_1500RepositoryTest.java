package pl.peth.hsbo_spring_demo.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.peth.hsbo_spring_demo.model.S7_1500Model;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@Testcontainers
public class S7_1500RepositoryTest {
    @Container
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void mongoProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
    }

    @Autowired
    private S7_1500Repository s7_1500Repository;

    @Test
    public void testSaveAndFindById() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("key", "Soll");
        payload.put("value", 22.5f);

        S7_1500Model testModel = new S7_1500Model(payload, "Soll");
        S7_1500Model savedModel = s7_1500Repository.save(testModel);

        assertNotNull(savedModel.getId());

        S7_1500Model foundModel = s7_1500Repository.findById(savedModel.getId()).orElse(null);

        assertNotNull(foundModel);
        assertEquals(savedModel.getId(), foundModel.getId());
        assertEquals("Soll", foundModel.getKey());
        assertEquals(22.5f, ((Number) foundModel.getPayload().get("value")).floatValue());
    }

    @Test
    public void testFindByTimestampBetween() {
        Map<String, Object> payload1 = new HashMap<>();
        payload1.put("key", "Soll");
        payload1.put("value", 22.5f);

        Map<String, Object> payload2 = new HashMap<>();
        payload2.put("key", "Ist");
        payload2.put("value", 50.0f);

        S7_1500Model testModel1 = new S7_1500Model(payload1, "Soll");
        S7_1500Model testModel2 = new S7_1500Model(payload2, "Ist");

        s7_1500Repository.save(testModel1);
        s7_1500Repository.save(testModel2);

        Instant currentTime = Instant.now();
        Instant startTime = currentTime.minusSeconds(60);
        Instant endTime = currentTime.plusSeconds(60);

        List<S7_1500Model> foundModels = s7_1500Repository.findByTimestampBetween(startTime, endTime);

        assertNotNull(foundModels);
        assertEquals(2, foundModels.size());
        assertEquals("Soll", foundModels.get(0).getKey());
        assertEquals(22.5f, ((Number) foundModels.get(0).getPayload().get("value")).floatValue());
        assertEquals("Ist", foundModels.get(1).getKey());
        assertEquals(50.0f, ((Number) foundModels.get(1).getPayload().get("value")).floatValue());
    }
}
