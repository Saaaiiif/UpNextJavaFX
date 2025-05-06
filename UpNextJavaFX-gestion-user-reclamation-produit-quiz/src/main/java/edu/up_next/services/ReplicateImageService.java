package edu.up_next.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class ReplicateImageService {
    private final String REPLICATE_API_TOKEN;
    private final HttpClient client;
    
    public ReplicateImageService() {
        // Load API token from config.properties
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            props.load(input);
            REPLICATE_API_TOKEN = props.getProperty("replicate.api.token");
        } catch (IOException e) {
            throw new RuntimeException("Could not load config.properties", e);
        }
        client = HttpClient.newHttpClient();
    }
    
    public String generateImageFromPrompt(String prompt) throws IOException, InterruptedException {
        // Create the request body
        String requestBody = String.format("""
                {
                    "version": "39ed52f2a78e934b3ba6e2a89f5b1c712de7dfea535525255b1aa35c5565e08b",
                    "input": {
                        "prompt": "%s"
                    }
                }""", prompt);

        // Create the POST request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.replicate.com/v1/predictions"))
                .header("Authorization", "Token " + REPLICATE_API_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Send the request and get the prediction ID
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 201) {
            throw new IOException("Failed to create prediction: " + response.body());
        }
        
        // Extract the prediction ID from the response
        String predictionId = extractPredictionId(response.body());
        
        // Poll for the result
        return pollForResult(predictionId);
    }
    
    private String extractPredictionId(String responseBody) {
        // Simple extraction - in production you should use proper JSON parsing
        int start = responseBody.indexOf("\"id\":\"") + 6;
        int end = responseBody.indexOf("\"", start);
        return responseBody.substring(start, end);
    }
    
    private String pollForResult(String predictionId) throws IOException, InterruptedException {
        String url = "https://api.replicate.com/v1/predictions/" + predictionId;
        
        while (true) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Token " + REPLICATE_API_TOKEN)
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new IOException("Failed to get prediction status: " + response.body());
            }
            
            String responseBody = response.body();
            
            if (responseBody.contains("\"status\":\"succeeded\"")) {
                // Extract the output URL - in production use proper JSON parsing
                int start = responseBody.indexOf("\"output\":\"") + 10;
                int end = responseBody.indexOf("\"", start);
                return responseBody.substring(start, end);
            } else if (responseBody.contains("\"status\":\"failed\"")) {
                throw new IOException("Image generation failed");
            }
            
            // Wait before polling again
            Thread.sleep(1000);
        }
    }
} 