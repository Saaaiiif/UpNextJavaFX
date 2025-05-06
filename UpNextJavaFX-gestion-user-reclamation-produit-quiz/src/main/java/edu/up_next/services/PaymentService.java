package edu.up_next.services;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class PaymentService {
    private static final String FLOUCI_PUBLIC_KEY = "cdb77358-0b99-4d82-bbf2-8674cfaa0d11";
    private static final String FLOUCI_API_KEY = "32b18808-5d4f-4426-9c94-f9dc5da9bf4e";
    private static final String FLOUCI_API_URL = "https://developers.flouci.com/api/generate_payment";

    public PaymentService() {
        // Initialisation du service
    }

    public static class PaymentInitResult {
        public final String paymentLink;
        public final String paymentId;
        public PaymentInitResult(String paymentLink, String paymentId) {
            this.paymentLink = paymentLink;
            this.paymentId = paymentId;
        }
    }

    public PaymentInitResult createPayment(double amount, String currency, String callbackUrl) throws Exception {
        URL url = new URL(FLOUCI_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        int amountInMillimes = (int) Math.round(amount * 1000);

        JSONObject paymentData = new JSONObject();
        paymentData.put("app_token", FLOUCI_PUBLIC_KEY);
        paymentData.put("app_secret", FLOUCI_API_KEY);
        paymentData.put("accept_card", true);
        paymentData.put("amount", amountInMillimes);
        paymentData.put("session_timeout_secs", 1200);
        paymentData.put("success_link", "https://www.google.com");
        paymentData.put("fail_link", "https://www.google.com");
        paymentData.put("developer_tracking_id", UUID.randomUUID().toString());
        // callbackUrl n'est pas utilisé dans la doc officielle, mais tu peux l'ajouter si tu as un webhook

        try(OutputStream os = conn.getOutputStream()) {
            byte[] input = paymentData.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        BufferedReader br;
        if (responseCode >= 200 && responseCode < 300) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
        }
        StringBuilder response = new StringBuilder();
        String responseLine = null;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        String responseStr = response.toString();
        try {
            JSONObject jsonResponse = new JSONObject(responseStr);
            if (responseCode >= 200 && responseCode < 300) {
                JSONObject result = jsonResponse.getJSONObject("result");
                String paymentLink = result.getString("link");
                String paymentId = result.getString("payment_id");
                return new PaymentInitResult(paymentLink, paymentId);
            } else {
                throw new Exception("Erreur Flouci: " + jsonResponse.optString("message", responseStr));
            }
        } catch (Exception ex) {
            System.out.println("Réponse brute de Flouci : " + responseStr);
            throw new Exception("Erreur Flouci (non JSON): " + responseStr);
        }
    }

    public boolean verifyPayment(String paymentId) throws Exception {
        // À adapter selon la doc, endpoint de vérification :
        // https://developers.flouci.com/api/verify_payment/<payment_id>
        URL url = new URL("https://developers.flouci.com/api/verify_payment/" + paymentId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("apppublic", FLOUCI_PUBLIC_KEY);
        conn.setRequestProperty("appsecret", FLOUCI_API_KEY);

        try(BufferedReader br = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getBoolean("success");
        }
    }
} 