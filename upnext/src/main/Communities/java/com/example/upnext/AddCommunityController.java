package com.example.upnext;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javafx.scene.control.Alert;
import javafx.scene.control.TextFormatter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;

public class AddCommunityController {
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private ImageView imagePreview;

    private byte[] imageData;
    private DatabaseService dbService = new DatabaseService();
    private BooleanProperty validName = new SimpleBooleanProperty(false);
    private BooleanProperty validDescription = new SimpleBooleanProperty(false);
    private final BooleanProperty validImage = new SimpleBooleanProperty(false);
    private BooleanProperty validId = new SimpleBooleanProperty(false);

    @FXML
    public void initialize() {
        setupIdValidation();
        setupNameValidation();
        setupDescriptionValidation();
        validId.set(false); // Ensure OK button is disabled until Generate button is clicked
        validImage.set(true); // Set validImage to true by default
    }

    private void setupIdValidation() {
        idField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));

        idField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                validateId();
            }
        });
    }

    private void validateId() {
        String input = idField.getText();
        if (input.length() != 8 || !input.matches("\\d{8}")) {
            // Generate a new ID without showing an alert
            idField.setText(generateUniqueId());
            validId.set(true); // Set validId to true when a valid ID is generated
        } else if (dbService.idExists(Integer.parseInt(input))) {
            showError("ID already exists");
            idField.setText(generateUniqueId());
            validId.set(true); // Set validId to true when a valid ID is generated
        } else {
            validId.set(true); // Set validId to true for valid IDs
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String generateUniqueId() {
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
                validImage.set(true);
            } catch (IOException e) {
                validImage.set(false);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleGenerateId() {
        idField.setText(generateUniqueId());
        validId.set(true);
    }

    public Community getNewCommunity() {
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            return null;
        }

        String description = descriptionField.getText() != null ? descriptionField.getText().trim() : "";

        // Limit description to 500 characters
        if (description.length() > 500) {
            description = description.substring(0, 500);
        }

        return new Community(
            Integer.parseInt(idField.getText()),
            nameField.getText().trim(),
            imageData,
            description
        );
    }

    private void setupNameValidation() {
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            validName.set(!newVal.trim().isEmpty());
        });
    }

    private void setupDescriptionValidation() {
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> {
            validDescription.set(!newVal.trim().isEmpty());
        });
    }

    public BooleanProperty validNameProperty() {
        return validName;
    }

    public StringProperty idTextProperty() {
        return idField.textProperty();
    }

    public BooleanProperty validImageProperty() {
        return validImage;
    }

    public BooleanProperty validDescriptionProperty() {
        return validDescription;
    }

    public BooleanProperty validIdProperty() {
        return validId;
    }
}
