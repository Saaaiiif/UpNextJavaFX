package controllers.communities;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class EditGenreController {
    @FXML private ComboBox<String> genreComboBox;
    @FXML private Button closeButton;
    @FXML private HBox titleBar;

    private BooleanProperty validGenre = new SimpleBooleanProperty(false);
    private int communityId;
    private boolean isDarkMode = true; // Default is dark mode

    // Variables for window dragging
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        // Populate the genre dropdown
        populateGenreDropdown();

        // Initialize isDarkMode from SessionManager
        isDarkMode = SessionManager.getInstance().isDarkMode();

        // Apply the theme if it was set before the scene was available
        applyThemeIfPossible();

        // Register as a theme change listener
        SessionManager.getInstance().addThemeChangeListener(this::handleThemeChange);

        // Set up title bar functionality for window dragging
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        // Set up validation for genre selection
        genreComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            validGenre.set(newVal != null && !newVal.isEmpty());
        });
    }

    /**
     * Populates the genre dropdown with genres from the database.
     */
    private void populateGenreDropdown() {
        DatabaseService dbService = new DatabaseService();
        List<String> genres = dbService.getAllGenres();

        ObservableList<String> genreOptions = FXCollections.observableArrayList(genres);
        genreComboBox.setItems(genreOptions);
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

    public void setCommunityId(int id) {
        this.communityId = id;
    }

    public void setGenre(String genre) {
        if (genre != null && !genre.isEmpty()) {
            genreComboBox.setValue(genre);
            validGenre.set(true);
        }
    }

    public String getGenre() {
        return genreComboBox.getValue();
    }

    public BooleanProperty validGenreProperty() {
        return validGenre;
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
     * Handles the close button click.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}