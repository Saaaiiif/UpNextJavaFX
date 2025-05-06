package controllers;

import edu.up_next.services.PaymentService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;



public class PaymentController {
    @FXML
    private Label amountLabel;
    @FXML
    private Button payButton;
    @FXML
    private Label statusLabel;
    @FXML
    private VBox paymentContainer;
    @FXML
    private WebView paymentWebView;

    private PaymentService paymentService;
    private double totalAmount;
    private String paymentId;
    private boolean paymentSuccess = false;

    public void initialize() {
        paymentService = new PaymentService();
        totalAmount = 0.0;
        amountLabel.setText("Montant total: " + totalAmount + " TND");
    }

    @FXML
    private void handlePayment() {
        try {
            payButton.setDisable(true);
            String callbackUrl = "https://www.google.com";
            PaymentService.PaymentInitResult paymentResult = paymentService.createPayment(totalAmount, "TND", callbackUrl);
            this.paymentId = paymentResult.paymentId;
            paymentContainer.getChildren().remove(paymentWebView);
            paymentWebView.getEngine().load(paymentResult.paymentLink);
            paymentContainer.getChildren().add(paymentWebView);
            statusLabel.setText("Redirection vers la page de paiement...");
            new Thread(() -> {
                try {
                    Thread.sleep(15000); // Attendre 15 secondes pour laisser le temps à l'utilisateur
                    boolean paymentSuccessResult = paymentService.verifyPayment(paymentId);
                    if (paymentSuccessResult) {
                        this.paymentSuccess = true;
                        javafx.application.Platform.runLater(() -> {
                            statusLabel.setText("Paiement réussi !");
                            paymentContainer.getChildren().remove(paymentWebView);
                            payButton.setDisable(true);
                        });
                    } else {
                        this.paymentSuccess = false;
                        javafx.application.Platform.runLater(() -> {
                            statusLabel.setText("Paiement échoué ou non finalisé. Veuillez réessayer.");
                            paymentContainer.getChildren().remove(paymentWebView);
                            payButton.setDisable(false);
                        });
                    }
                } catch (Exception e) {
                    this.paymentSuccess = false;
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("Erreur lors de la vérification du paiement: " + e.getMessage());
                        paymentContainer.getChildren().remove(paymentWebView);
                        payButton.setDisable(false);
                    });
                }
            }).start();
        } catch (Exception e) {
            statusLabel.setText("Erreur lors du paiement: " + e.getMessage());
            payButton.setDisable(false);
        }
    }

    public void setTotalAmount(double amount) {
        this.totalAmount = amount;
        amountLabel.setText("Montant total: " + amount + " TND");
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public boolean isPaymentSuccess() {
        return paymentSuccess;
    }
} 