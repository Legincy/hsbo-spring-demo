package pl.peth.hsbo_spring_demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class IndexController {
    @GetMapping("/hello")
    public String getHello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    @GetMapping("/timestamp")
    public Map<String, Long> getTimestamp() {
        Map<String, Long> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
