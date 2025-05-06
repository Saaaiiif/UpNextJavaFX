package edu.up_next.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HuggingFaceSpaceImageService {
    private static final String API_URL = "https://huggingface.co/spaces/stabilityai/stable-diffusion/api/predict";

    public byte[] generateImageFromPrompt(String prompt) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(60000); // 60 secondes
        conn.setReadTimeout(60000); // 60 secondes

        // Prépare le JSON
        JSONObject payload = new JSONObject();
        JSONArray data = new JSONArray();
        data.put(prompt);
        data.put(""); // negative prompt vide
        payload.put("data", data);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.toString().getBytes("utf-8"));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
            throw new Exception("Erreur Hugging Face Space: " + response.toString());
        }

        // Récupère l'URL de l'image générée
        String imageUrl = null;
        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseBuilder.append(line.trim());
            }
            JSONObject json = new JSONObject(responseBuilder.toString());
            if (json.has("data")) {
                JSONArray dataArr = json.getJSONArray("data");
                if (dataArr.length() > 0) {
                    imageUrl = dataArr.getString(0);
                }
            }
        }
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new Exception("Réponse inattendue de Hugging Face : " + responseBuilder.toString());
        }

        // Télécharge l'image
        URL imgUrl = new URL(imageUrl);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream is = imgUrl.openStream()) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = is.read(buffer)) != -1) {
                baos.write(buffer, 0, n);
            }
        }
        return baos.toByteArray();
    }
} 