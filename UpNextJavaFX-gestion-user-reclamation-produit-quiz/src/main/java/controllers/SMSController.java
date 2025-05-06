package controllers;

import edu.up_next.services.TwilioService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;


public class SMSController {
    @FXML
    private TextField artistPhoneField;

    private final TwilioService twilioService;

    public SMSController() {
        this.twilioService = new TwilioService();
    }

    @FXML
    private void sendSMS() {
        String artistPhoneNumber = artistPhoneField.getText().trim();
        if (artistPhoneNumber.isEmpty()) {
            return;
        }
        String message = "Bonjour, je n'ai pas encore reçu ma commande. Pourriez-vous vérifier s'il vous plaît ? Merci.";
        twilioService.sendDeliveryNotification(artistPhoneNumber, message);
    }
} 