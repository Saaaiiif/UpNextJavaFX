package controllers.communities;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;
import edu.up_next.entities.User;

/**
 * Controller for the root layout that contains the title bar and content area.
 */
public class RootLayoutController {

    @FXML
    private ImageView logoImage;

    @FXML
    private HBox titleBar;

    @FXML
    private Button themeToggleButton;

    @FXML
    private Button minimizeButton;

    @FXML
    private Button closeButton;

    @FXML
    private Button backButton;

    private boolean isDarkMode = true; // Default is dark mode

    @FXML
    private StackPane contentArea;

    private Stage stage;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        // Load logo
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        logoImage.setImage(logo);

        // Set up title bar functionality
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            if (stage != null) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        // Set up minimize button functionality
        minimizeButton.setOnAction(event -> {
            if (stage != null) {
                stage.setIconified(true);
            }
        });

        // Set up close button functionality
        closeButton.setOnAction(event -> {
            if (stage != null) {
                // Ensure the application fully terminates when closed
                javafx.application.Platform.exit();
            }
        });

        // Set up theme toggle button functionality
        themeToggleButton.setOnAction(event -> {
            toggleTheme();
        });
    }

    /**
     * Sets the stage for window operations.
     * @param stage The primary stage of the application
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Loads content into the content area.
     * @param fxmlPath The path to the FXML file to load
     * @return The controller of the loaded FXML
     * @throws IOException If the FXML file cannot be loaded
     */
    public <T> T loadContent(String fxmlPath, Class<T> controllerType) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent content = loader.load();

        // Clear existing content and add new content
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);

        return loader.getController();
    }

    /**
     * Gets the content area for direct manipulation.
     * @return The content area StackPane
     */
    public StackPane getContentArea() {
        return contentArea;
    }

    /**
     * Gets the stage for window operations.
     * @return The primary stage of the application
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * Gets the current theme mode.
     * @return true if dark mode, false if light mode
     */
    public boolean isDarkMode() {
        return isDarkMode;
    }

    /**
     * Sets the theme mode.
     * @param isDarkMode true for dark mode, false for light mode
     */
    public void setThemeMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        updateThemeButtonText();
    }

    /**
     * Updates the theme toggle button text based on the current theme.
     */
    private void updateThemeButtonText() {
        if (themeToggleButton != null) {
            if (isDarkMode) {
                themeToggleButton.setText("☀"); // Sun icon for light mode toggle
            } else {
                themeToggleButton.setText("☾"); // Moon icon for dark mode toggle
            }
        }
    }

    /**
     * Toggles between light and dark mode.
     */
    private void toggleTheme() {
        isDarkMode = !isDarkMode;

        // Save the theme preference to SessionManager
        SessionManager.getInstance().setDarkMode(isDarkMode);

        // Update the button text
        updateThemeButtonText();

        // Get the scene
        if (stage != null && stage.getScene() != null) {
            if (isDarkMode) {
                // Switch to dark mode
                stage.getScene().getRoot().getStyleClass().remove("light-mode");

                // Also remove light-mode class from current content
                if (!contentArea.getChildren().isEmpty()) {
                    Node currentContent = contentArea.getChildren().get(0);
                    if (currentContent instanceof Parent) {
                        ((Parent) currentContent).getStyleClass().remove("light-mode");
                    }
                }
            } else {
                // Switch to light mode
                stage.getScene().getRoot().getStyleClass().add("light-mode");

                // Also add light-mode class to current content
                if (!contentArea.getChildren().isEmpty()) {
                    Node currentContent = contentArea.getChildren().get(0);
                    if (currentContent instanceof Parent) {
                        ((Parent) currentContent).getStyleClass().add("light-mode");
                    }
                }
            }
        }
    }

    /**
     * Handles the back button click.
     * Navigates back to the appropriate view based on the user's role.
     */
    @FXML
    private void handleBackButtonClick() {
        try {
            // Get the current session type from SessionManager
            SessionType sessionType = SessionManager.getInstance().getSessionType();
            User currentUser = SessionManager.getInstance().getCurrentUser();

            FXMLLoader loader;
            Parent root;

            // Load the home.fxml view for all roles
            loader = new FXMLLoader(getClass().getResource("/home.fxml"));
            root = loader.load();

            // Get the controller and set the current user
            controllers.home homeController = loader.getController();
            if (currentUser != null) {
                homeController.setUser(currentUser);
            }

            // Set the scene with the appropriate view
            Stage stage = this.stage;
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Failed to load view: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
