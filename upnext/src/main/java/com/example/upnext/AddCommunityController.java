package com.example.upnext;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javafx.scene.control.Alert;
import javafx.scene.control.TextFormatter;

public class AddCommunityController {
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private ImageView imagePreview;
    
    private byte[] imageData;
    private DatabaseService dbService = new DatabaseService();

    @FXML
    public void initialize() {
        // Remove auto-generation and add validation
        setupIdValidation();
    }

    private void setupIdValidation() {
        // Restrict to numbers only
        idField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));
        
        // Validate on focus loss
        idField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // When focus lost
                validateId();
            }
        });
    }

    private void validateId() {
        String input = idField.getText();
        if (input.length() != 8 || !input.matches("\\d{8}")) {
            showError("ID must be exactly 8 digits");
            idField.setText(generateUniqueId());
        } else if (dbService.idExists(Integer.parseInt(input))) {
            showError("ID already exists");
            idField.setText(generateUniqueId());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid ID");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String generateUniqueId() {
        // Generate 8-digit unique ID
        int min = 10000000;
        int max = 99999999;
        int generatedId;
        do {
            generatedId = (int) (Math.random() * (max - min + 1) + min);
        } while (dbService.idExists(generatedId));
        
        return String.valueOf(generatedId);
    }

    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        Window window = imagePreview.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        
        if (file != null) {
            try {
                imageData = Files.readAllBytes(file.toPath());
                imagePreview.setImage(new Image(file.toURI().toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleGenerateId() {
        idField.setText(generateUniqueId());
    }
    
    public Community getNewCommunity() {
        if (nameField.getText().isEmpty()) return null;
        
        return new Community(
            Integer.parseInt(idField.getText()),
            nameField.getText(),
            imageData
        );
    }
} 