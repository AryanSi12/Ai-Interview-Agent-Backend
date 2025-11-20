package com.Interview.AiAgent.Services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class GeminiClient {

    @Value("${gemini.api.keys}")
    private String apiKeysString;

    private final RestTemplate restTemplate = new RestTemplate();
    private List<String> apiKeys;
    private int currentIndex = 0;

    @PostConstruct
    public void init() {
        apiKeys = Arrays.asList(apiKeysString.split(","));
    }

    private synchronized String getNextApiKey() {
        String key = apiKeys.get(currentIndex);
        System.out.println(currentIndex);
        currentIndex = (currentIndex + 1) % apiKeys.size();
        return key;
    }

    public String generate(String prompt) {
        String apiKey = getNextApiKey();
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("parts", List.of(Map.of("text", prompt)));

        Map<String, Object> requestBody = Map.of("contents", List.of(message));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");

            return parts.get(0).get("text");

        } catch (Exception e) {
            return "⚠️ Error generating content with key [" + apiKey + "]: " + e.getMessage();
        }
    }
}

