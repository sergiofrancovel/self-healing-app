package com.example.healer.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ChatGPTService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey = "api_key";

    String currentConfig = loadCurrentConfig();

    public String analyzeLogs() {
        try {
            // Read the log file dynamically
            String logs = Files.readString(Paths.get("logs/app.log")); // Update this path to your actual log file
            return analyzeLogsContent(logs);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading logs: " + e.getMessage();
        }
    }

    public String analyzeLogsContent(String logs) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // Construct the JSON body
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode body = mapper.createObjectNode();
        body.put("model", "gpt-4o");

        // Create the messages array
        ArrayNode messages = mapper.createArrayNode();

        ObjectNode systemMessage = mapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content",
                "You are a cybersecurity assistant analyzing application logs. " +
                        "Your goal is to identify potential security issues or application misconfigurations. " +
                        "Provide specific, actionable suggestions to improve security or fix problems. " +
                        "Output only configuration changes or concise actions needed, avoiding verbose descriptions."+
                        "ONLY RESPOND IN CONFIGURATION. You will be directly writing to an application.properties file, so any additional characters will break the document");
        messages.add(systemMessage);

        ObjectNode userMessage = mapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content",
                "Analyze these logs:\n" + logs +
                        "Here is also the existing config: " + currentConfig +
                        "\nIdentify potential security threats or misconfigurations and recommend specific configuration changes. " +
                        "Output suggestions in the form of key=value pairs, if applicable. "+
                        "ONLY RESPOND IN CONFIGURATION. You will be directly writing to an application.properties file, so any additional characters will break the document");
        messages.add(userMessage);

        body.set("messages", messages);

        // Make the POST request
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                request,
                String.class
        );

        try {
            JsonNode root = mapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            return cleanResponse(content);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing ChatGPT response.";
        }
    }

    private String cleanResponse(String response) {
        // Remove code block markers like ```plaintext``` or similar
        return response.replaceAll("```[a-zA-Z]*", "").replaceAll("```", "").trim();
    }



    private String loadCurrentConfig() {
        try {
            Path configFilePath = Paths.get("src/main/resources/application.properties").toAbsolutePath();
            if (Files.exists(configFilePath)) {
                return Files.readString(configFilePath);
            }
        } catch (IOException e) {
            System.out.println("Failed to load current configuration: " + e.getMessage());
        }
        return "No configuration found.";
    }
}