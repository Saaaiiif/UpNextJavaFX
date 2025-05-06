package edu.up_next.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public class TwilioService {
    private static final String CONFIG_FILE = "/config.properties";
    private static String ACCOUNT_SID;
    private static String AUTH_TOKEN;
    private static String TWILIO_PHONE_NUMBER;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream input = TwilioService.class.getResourceAsStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.load(input);
            
            ACCOUNT_SID = Dotenv.load().get("TWILIO_ACCOUNT_SID");
            AUTH_TOKEN = Dotenv.load().get("TWILIO_AUTH_TOKEN");
            TWILIO_PHONE_NUMBER = Dotenv.load().get("TWILIO_PHONE_NUMBER");
            
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        } catch (IOException e) {
            showError("Erreur de configuration", "Impossible de charger la configuration Twilio");
        }
    }

    public boolean sendDeliveryNotification(String artistPhoneNumber, String message) {
        if (!isValidPhoneNumber(artistPhoneNumber)) {
            showError("Numéro invalide", "Le numéro de téléphone doit être au format international (ex: +33612345678)");
            return false;
        }
        try {
            Message.creator(
                new PhoneNumber(artistPhoneNumber),
                new PhoneNumber(TWILIO_PHONE_NUMBER),
                message
            ).create();
            showSuccess("SMS envoyé", "Le message a été envoyé avec succès à l'artiste");
            return true;
        } catch (Exception e) {
            showError("Erreur d'envoi", "Erreur lors de l'envoi du SMS: " + e.getMessage());
            return false;
        }
    }

    public static String getArtistPhoneNumber() {
        try (InputStream input = TwilioService.class.getResourceAsStream("/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return Dotenv.load().get("ARTIST_PHONE_NUMBER");
        } catch (IOException e) {
            showError("Erreur de configuration", "Impossible de charger le numéro de l'artiste");
            return null;
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    private static void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static void showSuccess(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 