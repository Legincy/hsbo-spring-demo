package pl.peth.hsbo_spring_demo.handler.mqtt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import pl.peth.hsbo_spring_demo.config.MqttConfiguration;
import pl.peth.hsbo_spring_demo.model.S7_1500Model;
import pl.peth.hsbo_spring_demo.model.Wago750Model;
import pl.peth.hsbo_spring_demo.service.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SPSMessageHandlerTest {
    @Mock
    private SPSDataService spsDataService;

    @Mock
    private Wago750Service wago750Service;

    @Mock
    private S7_1500Service s7_1500Service;

    @Mock
    private MqttConfiguration mqttConfiguration;

    @Mock
    private SSEService sseService;

    @Mock
    private MetricsService metricsService;

    @Captor
    private ArgumentCaptor<S7_1500Model> s7_1500ModelCaptor;

    @Captor
    private ArgumentCaptor<Wago750Model> wago750Captor;

    @InjectMocks
    private SPSMessageHandler spsMessageHandler;

    @Test
    public void testHandleS7_1500TemperatureMessage() {
        String topic = "S7_1500/Temperatur/Soll";
        String payload = "25.1";

        Map<String, Object> headers = new HashMap<>();
        headers.put("mqtt_receivedTopic", topic);

        Message<String> message = new GenericMessage<>(payload, headers);

        spsMessageHandler.handleMessage(message);

        verify(s7_1500Service, times(1)).save(s7_1500ModelCaptor.capture());

        S7_1500Model capturedModel = s7_1500ModelCaptor.getValue();
        assertEquals("Soll", capturedModel.getKey());
        assertEquals(25.1f, capturedModel.getPayload().get("value"));
    }
}
