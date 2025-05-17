package pl.peth.hsbo_spring_demo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.peth.hsbo_spring_demo.model.S7_1500Model;
import pl.peth.hsbo_spring_demo.repository.S7_1500Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S7_1500ServiceTest {
    @Mock
    private S7_1500Repository s7_1500Repository;

    @InjectMocks
    private S7_1500Service s7_1500Service;

    @Test
    public void testFindAllByKey_WithKey(){
        String testKey = "Soll";
        Map<String, Object> payload = new HashMap<>();
        payload.put("key", "Soll");
        payload.put("value", 22.5f);

        S7_1500Model testModel = new S7_1500Model(payload, testKey);

        when(s7_1500Repository.findAllByKey(testKey)).thenReturn(List.of(testModel));

        List<S7_1500Model> result = s7_1500Service.findAllByKey(Optional.of(testKey), Optional.empty(), Optional.empty(), Optional.empty());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testKey, result.getFirst().getKey());
        assertEquals(22.5f, result.getFirst().getPayload().get("value"));

        verify(s7_1500Repository, times(1)).findAllByKey(testKey);
    }

    @Test
    public void testFindAllByKey_WithoutKey(){
        Map<String, Object> payload1 = new HashMap<>();
        payload1.put("key", "Soll");
        payload1.put("value", 22.5f);

        Map<String, Object> payload2 = new HashMap<>();
        payload2.put("key", "Ist");
        payload2.put("value", 50.0f);

        S7_1500Model testModel1 = new S7_1500Model(payload1, "Soll");
        S7_1500Model testModel2 = new S7_1500Model(payload2, "Ist");

        when(s7_1500Repository.findAll()).thenReturn(List.of(testModel1, testModel2));

        List<S7_1500Model> result = s7_1500Service.findAllByKey(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Soll", result.getFirst().getKey());
        assertEquals(22.5f, result.getFirst().getPayload().get("value"));
        assertEquals("Ist", result.getLast().getKey());
        assertEquals(50.0f, result.getLast().getPayload().get("value"));

        verify(s7_1500Repository, times(1)).findAll();
        verify(s7_1500Repository, never()).findAllByKey(any());
    }
}
