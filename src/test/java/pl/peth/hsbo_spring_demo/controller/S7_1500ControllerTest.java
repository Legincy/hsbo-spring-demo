package pl.peth.hsbo_spring_demo.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import pl.peth.hsbo_spring_demo.model.S7_1500Model;
import pl.peth.hsbo_spring_demo.service.S7_1500Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(S7_1500Controller.class)
public class S7_1500ControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private S7_1500Service s7_1500Service;

    @Test
    public void testGetAll() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("value", 22.5f);

        S7_1500Model testModel = new S7_1500Model(payload, "Soll");

        when(s7_1500Service.findAllByKey(any(), any(), any(), any()))
                .thenReturn(List.of(testModel));

        mockMvc.perform(get("/api/v1/s7-1500")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].payload.value").value(22.5f));
    }

    @Test
    public void testGetLatest() throws Exception {
        Map<String, Object> payload2 = new HashMap<>();
        payload2.put("value", 50.0f);

        S7_1500Model testModel2 = new S7_1500Model(payload2, "Ist");

        when(s7_1500Service.findLatest(any()))
                .thenReturn(testModel2);

        mockMvc.perform(get("/api/v1/s7-1500/latest")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.value").value(50.0f));
    }
}
