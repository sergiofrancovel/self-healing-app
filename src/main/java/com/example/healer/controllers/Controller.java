package com.example.healer.controllers;

import com.example.healer.services.ChatGPTService;
import com.example.healer.services.ConfigUpdater;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/demo")
public class Controller {

    private final Logger logger = LoggerFactory.getLogger(Controller.class);

    @Autowired
    private final ChatGPTService chat = new ChatGPTService();

    @Autowired
    private ConfigUpdater configUpdater;

    @GetMapping("/vulnerable")
    public String vulnerableEndpoint(@RequestParam String input){
        return "you entered " + input;
    }

    @GetMapping("/analyze")
    public String analyze() {
        return chat.analyzeLogs();
    }

    @GetMapping("/self-heal")
    public String selfHeal() {
        String suggestions = chat.analyzeLogs();
        configUpdater.updateConfig(suggestions);
        return "Configuration updated!";
    }

    @GetMapping("/test-write")
    public String testWrite() {
        try {
            String dummyConfig = "test.property=dummyValue";
            configUpdater.updateConfig(dummyConfig);
            return "Test write successful: " + dummyConfig;
        } catch (Exception e) {
            e.printStackTrace();
            return "Test write failed: " + e.getMessage();
        }
    }
}
