package pl.peth.hsbo_spring_demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class IndexController {
    /**
     * Returns a greeting message that includes the provided name.
     *
     * @param name String : The name to include in the greeting. Defaults to "World" if not provided.
     * @return String : A formatted greeting message: "Hello, __NAME__!"
     */
    @GetMapping("/hello")
    public String getHello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    /**
     *  Returns the current system timestamp in milliseconds inside a HashMap
     *
     * @return Map<String, Long> : {"timestamp": __TIMESTAMP_IN_MILLIS__}
     */
    @GetMapping("/timestamp")
    public Map<String, Long> getTimestamp() {
        Map<String, Long> response = new HashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
