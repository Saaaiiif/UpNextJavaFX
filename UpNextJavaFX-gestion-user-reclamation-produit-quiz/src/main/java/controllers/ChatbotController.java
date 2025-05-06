package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ChatbotController {

    @FXML
    private TextArea chatArea;   // Zone d'affichage des messages
    @FXML
    private TextField messageField;  // Champ de texte pour entrer un message
    @FXML
    private Button sendButton;  // Bouton d'envoi

    // Clé API OpenRouter (remplacez par votre clé personnelle)
    //private static final String API_KEY = "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";
public static final String API_KEY="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";
    // URL de l'API OpenRouter pour le modèle deepseek
    public static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    // Méthode pour envoyer un message

    @FXML
    // Méthode pour envoyer le message au webhook n8n
    public void sendMessage(ActionEvent event) {
        String message = messageField.getText();  // Récupère le message tapé par l'utilisateur

        if (!message.trim().isEmpty()) {
            // Affiche le message de l'utilisateur dans la zone de chat
            chatArea.appendText("Vous: " + message + "\n");



            // Affiche la réponse du chatbot (si nécessaire)
            String response = getChatbotResponse(message);
            chatArea.appendText("Chatbot: " + response + "\n");

            // Efface le champ de texte après l'envoi
            messageField.clear();
        }
    }



    //eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJlMjM2YjU2Mi0zZjVhLTQzOTctYjc3Yy0yZDc5NGNiM2M2NGYiLCJpc3MiOiJuOG4iLCJhdWQiOiJwdWJsaWMtYXBpIiwiaWF0IjoxNzQ1MDY4OTkzfQ.pqEXQpGG3O1gpyOvZE77ddzfhsVioHBerMce5muSrhA
    // Méthode pour obtenir la réponse du chatbot via l'API OpenRouter
    // Méthode pour obtenir la réponse du chatbot via l'API OpenRouter
    private String getChatbotResponse(String message) {
        String response = "";

        try {
            // Crée une instance de HttpClient
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost request = new HttpPost(API_URL);

            // Ajoute l'en-tête d'autorisation avec votre clé API
            request.setHeader("Authorization", "Bearer " + API_KEY);


            // Crée le corps de la requête avec le message de l'utilisateur
            String jsonBody = "{\"model\": \"deepseek/deepseek-r1-distill-llama-70b:free\", " +
                    "\"messages\": [{\"role\": \"user\", \"content\": \"" + message + "\"}]}";
            StringEntity entity = new StringEntity(jsonBody);
            request.setEntity(entity);

            // Exécute la requête HTTP
            HttpResponse httpResponse = httpClient.execute(request);

            // Récupère la réponse du serveur
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            // Affiche la réponse brute pour déboguer
            System.out.println("Réponse brute de l'API: " + responseBuilder.toString());

            // Utilise Jackson pour parser la réponse JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(responseBuilder.toString());

            System.out.println(jsonResponse);


            // Récupère la première réponse du modèle
            response = jsonResponse.get("choices").get(0).get("message").get("content").asText();

            System.out.println(response);

            // Ferme l'HttpClient
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }




}
