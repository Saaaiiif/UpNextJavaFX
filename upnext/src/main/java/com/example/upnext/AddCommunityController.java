package com.example.upnext;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextFormatter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;

public class AddCommunityController {
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField socialField;
    @FXML private ImageView imagePreview;
    @FXML private Button closeButton;
    @FXML private HBox titleBar;
    @FXML private Button createButton;

    // Variables for window dragging
    private double xOffset = 0;
    private double yOffset = 0;

    private byte[] imageData;
    private DatabaseService dbService = new DatabaseService();
    private BooleanProperty validName = new SimpleBooleanProperty(false);
    private BooleanProperty validDescription = new SimpleBooleanProperty(false);
    private final BooleanProperty validImage = new SimpleBooleanProperty(false);
    private BooleanProperty validId = new SimpleBooleanProperty(false);
    private boolean isDarkMode = true; // Default is dark mode

    @FXML
    public void initialize() {
        setupIdValidation();
        setupNameValidation();
        setupDescriptionValidation();
        validId.set(false); // Ensure OK button is disabled until Generate button is clicked
        validImage.set(false); // Set validImage to false by default, requiring image selection

        // Generate ID automatically
        handleGenerateId();

        // Initialize isDarkMode from SessionManager
        isDarkMode = SessionManager.getInstance().isDarkMode();

        // Apply the theme if it was set before the scene was available
        applyThemeIfPossible();

        // Register as a theme change listener
        SessionManager.getInstance().addThemeChangeListener(this::handleThemeChange);

        // Set up title bar functionality for window dragging if titleBar exists
        if (titleBar != null) {
            titleBar.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            titleBar.setOnMouseDragged(event -> {
                Stage stage = (Stage) titleBar.getScene().getWindow();
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            });
        }

        // Bind the create button's disabled property to the validation logic if it exists
        if (createButton != null) {
            createButton.disableProperty().bind(
                validName.not().or(validDescription.not()).or(validImage.not()).or(validId.not())
            );
        }
    }

    /**
     * Handles theme changes from SessionManager.
     * 
     * @param isDarkMode true for dark mode, false for light mode
     */
    private void handleThemeChange(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        applyThemeIfPossible();
    }

    private void setupIdValidation() {
        if (idField != null) {
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
    }

    private void validateId() {
        if (idField != null && idField.getText() != null) {
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
        } else {
            validId.set(false); // Set validId to false if idField is null
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
        if (imagePreview != null && imagePreview.getScene() != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );

            Window window = imagePreview.getScene().getWindow();
            File file = fileChooser.showOpenDialog(window);

            if (file != null) {
                try {
                    imageData = Files.readAllBytes(file.toPath());
                    // Load image with size constraints to prevent loading overly large images
                    // This improves performance for large images while maintaining display quality
                    imagePreview.setImage(new Image(file.toURI().toString(), 300, 300, true, true));
                    validImage.set(true);
                } catch (IOException e) {
                    validImage.set(false);
                    e.printStackTrace();
                }
            }
        } else {
            validImage.set(false);
        }
    }

    @FXML
    public void handleGenerateId() {
        if (idField != null) {
            idField.setText(generateUniqueId());
            validId.set(true);
        }
    }

    public Community getNewCommunity() {
        if (nameField == null || nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            return null;
        }

        String description = (descriptionField != null && descriptionField.getText() != null) ? descriptionField.getText().trim() : "";

        // Limit description to 500 characters
        if (description.length() > 500) {
            description = description.substring(0, 500);
        }

        String social = (socialField != null && socialField.getText() != null) ? socialField.getText().trim() : "";

        // Check if idField is null before using it
        if (idField == null || idField.getText() == null) {
            return null;
        }

        Community community = new Community(
            Integer.parseInt(idField.getText()),
            nameField.getText().trim(),
            imageData,
            description,
            1  // Status 1 for artist-created communities
        );
        community.setSocial(social);
        return community;
    }

    private void setupNameValidation() {
        if (nameField != null) {
            nameField.textProperty().addListener((obs, oldVal, newVal) -> {
                validName.set(!newVal.trim().isEmpty());
            });
        }
    }

    private void setupDescriptionValidation() {
        if (descriptionField != null) {
            descriptionField.textProperty().addListener((obs, oldVal, newVal) -> {
                validDescription.set(!newVal.trim().isEmpty());
            });
        }
    }

    public BooleanProperty validNameProperty() {
        return validName;
    }

    public StringProperty idTextProperty() {
        return idField != null ? idField.textProperty() : null;
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

    /**
     * Sets the initial theme mode for the dialog.
     * 
     * @param isDarkMode true for dark mode, false for light mode
     */
    public void setThemeMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;

        // Store the theme mode for later application when the dialog is shown
        // The actual application will happen in initialize or when the scene becomes available
        applyThemeIfPossible();
    }

    /**
     * Applies the theme if the scene is available, otherwise does nothing.
     * This method is safe to call at any time.
     */
    private void applyThemeIfPossible() {
        // Apply the theme to the dialog if the scene is available
        if (closeButton != null && closeButton.getScene() != null && closeButton.getScene().getRoot() != null) {
            Parent root = closeButton.getScene().getRoot();
            if (isDarkMode) {
                root.getStyleClass().remove("light-mode");
            } else {
                if (!root.getStyleClass().contains("light-mode")) {
                    root.getStyleClass().add("light-mode");
                }
            }
        } else if (closeButton != null) {
            // If the scene is not available yet, add a listener to apply the theme when it becomes available
            closeButton.sceneProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue.getRoot() != null) {
                    Parent root = newValue.getRoot();
                    if (isDarkMode) {
                        root.getStyleClass().remove("light-mode");
                    } else {
                        if (!root.getStyleClass().contains("light-mode")) {
                            root.getStyleClass().add("light-mode");
                        }
                    }
                }
            });
        }
    }

    /**
     * Updates the theme toggle button text based on the current theme.
     * This method is kept for compatibility but does nothing since the button has been removed.
     */
    private void updateThemeButtonText() {
        // No-op - button has been removed
    }


    /**
     * Handles the close button click.
     */
    @FXML
    private void handleClose() {
        if (closeButton != null && closeButton.getScene() != null) {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Handles the create community button click.
     */
    @FXML
    private void handleCreateCommunity() {
        Community newCommunity = getNewCommunity();
        if (newCommunity != null) {
            // Add the new community to the database
            DatabaseService dbService = new DatabaseService();
            dbService.addCommunityWithId(newCommunity.getId(), newCommunity.getName(), newCommunity.getImage(), 
                                        newCommunity.getDescription(), newCommunity.getStatus(), newCommunity.getSocial());

            // Create a custom dialog with the same title bar style as other windows
            Stage dialogStage = new Stage();
            dialogStage.initOwner(closeButton.getScene().getWindow());

            // Create the dialog content
            VBox dialogRoot = new VBox();
            dialogRoot.getStylesheets().add(getClass().getResource("/com/example/upnext/styles.css").toExternalForm());
            dialogRoot.getStyleClass().add("dialog-pane");

            // Apply dark/light mode
            if (!SessionManager.getInstance().isDarkMode()) {
                dialogRoot.getStyleClass().add("light-mode");
            }

            // Create title bar
            HBox titleBar = new HBox();
            titleBar.getStyleClass().add("title-bar");
            titleBar.setPrefHeight(30);

            // Add title to title bar
            Label titleLabel = new Label("Success");
            titleLabel.getStyleClass().add("title-label");
            titleLabel.setPadding(new Insets(5, 0, 0, 10));

            // Add close button to title bar
            Button closeDialogButton = new Button("X");
            closeDialogButton.getStyleClass().add("close-button");
            closeDialogButton.setOnAction(e -> dialogStage.close());

            // Set up title bar for dragging
            titleBar.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            titleBar.setOnMouseDragged(event -> {
                dialogStage.setX(event.getScreenX() - xOffset);
                dialogStage.setY(event.getScreenY() - yOffset);
            });

            // Add title and close button to title bar with spacing
            HBox.setHgrow(titleLabel, Priority.ALWAYS);
            titleBar.getChildren().addAll(titleLabel, closeDialogButton);

            // Create content area
            VBox contentBox = new VBox();
            contentBox.setPadding(new Insets(20));
            contentBox.setAlignment(Pos.CENTER);

            // Add success message
            Label messageLabel = new Label("Community created successfully!");
            messageLabel.getStyleClass().add("dialog-message");

            // Add OK button
            Button okButton = new Button("OK");
            okButton.getStyleClass().add("action-button");
            okButton.setOnAction(e -> dialogStage.close());
            okButton.setPrefWidth(100);

            // Add message and button to content box
            contentBox.getChildren().addAll(messageLabel, new Region() {{ setPrefHeight(20); }}, okButton);

            // Add title bar and content to dialog root
            dialogRoot.getChildren().addAll(titleBar, contentBox);

            // Create scene and set it on the stage
            Scene dialogScene = new Scene(dialogRoot, 350, 180);
            dialogStage.setScene(dialogScene);
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.showAndWait();

            // Navigate back to the main page
            try {
                SceneTransitionUtil.changeContentWithPreload(
                        "/com/example/upnext/hello-view.fxml",
                    SceneTransitionUtil.TransitionType.FADE, 
                    MainController.class,
                    controller -> {} // No data loading needed for this view
                );
            } catch (IOException e) {
                System.err.println("Failed to navigate back to main page: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles the cancel button click.
     */
    @FXML
    private void handleCancel() {
        // Navigate back to the main page
        try {
            SceneTransitionUtil.changeContentWithPreload(
                    "/com/example/upnext/hello-view.fxml",
                SceneTransitionUtil.TransitionType.FADE, 
                MainController.class,
                controller -> {} // No data loading needed for this view
            );
        } catch (IOException e) {
            System.err.println("Failed to navigate back to main page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
