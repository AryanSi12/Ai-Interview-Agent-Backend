package com.Interview.AiAgent.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generate(String prompt) {
        // ✅ Dynamically construct the full API URL using the injected key
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        // Step 1: Create the request body
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("parts", List.of(Map.of("text", prompt)));

        Map<String, Object> requestBody = Map.of("contents", List.of(message));

        // Step 2: Set headers for JSON request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Step 3: Wrap body and headers into an HttpEntity
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // Step 4: Make the POST request to Gemini API
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);

            // Step 5: Extract generated text from the response
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, String>> parts = (List<Map<String, String>>) content.get("parts");

            return parts.get(0).get("text"); // final result

        } catch (Exception e) {
            return "⚠️ Error generating content: " + e.getMessage();
        }
    }
}
