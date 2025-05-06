package controllers.communities;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Service class for interacting with the OpenAI API to extract keywords from text.
 */
public class OpenAIService {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4.1-nano";
    private final String apiKey;
    private final HttpClient client;

    /**
     * Constructor that initializes the OpenAI service with the provided API key.
     * 
     * @param apiKey The OpenAI API key
     */
    public OpenAIService(String apiKey) {
        this.apiKey = apiKey;
        this.client = HttpClient.newHttpClient();
    }

    /**
     * Extracts keywords from the provided text using OpenAI's API.
     * 
     * @param text The text to extract keywords from
     * @return A list of extracted keywords
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     */
    public List<String> extractKeywords(String text) throws IOException, InterruptedException {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Create the request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", "Extract 3-5 relevant keywords from this text. Return only the keywords as a comma-separated list, with no additional text: " + text);
        messages.put(message);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 50);

        // Build the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the response
        JSONObject jsonResponse = new JSONObject(response.body());

        if (jsonResponse.has("error")) {
            System.err.println("OpenAI API error: " + jsonResponse.getJSONObject("error").getString("message"));
            return new ArrayList<>();
        }

        // Extract the content from the response
        String content = jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();

        // Split the comma-separated list into individual keywords
        return Arrays.stream(content.split(","))
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty())
                .collect(Collectors.toList());
    }
}
