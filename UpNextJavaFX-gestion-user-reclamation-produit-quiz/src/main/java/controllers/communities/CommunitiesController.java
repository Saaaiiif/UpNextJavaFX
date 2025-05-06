package controllers.communities;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.text.Font;
import javafx.scene.shape.Circle;
import javafx.scene.effect.DropShadow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.io.ByteArrayInputStream;
import javafx.scene.control.Dialog;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.animation.KeyValue;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class CommunitiesController {
    @FXML
    public ImageView searchIcon;

    @FXML
    private ImageView logoImage;

    @FXML
    private GridPane communitiesGrid;

    @FXML
    private TextField searchField;

    @FXML
    private HBox addCardContainer;

    @FXML
    private Button communitiesButton;

    @FXML
    private Button createCommunityButton;

    @FXML
    private Button artistsButton;

    @FXML
    private CheckBox statusFilter0;

    @FXML
    private CheckBox statusFilter1;

    @FXML
    private CheckBox statusFilter2;

    @FXML
    private ComboBox<String> genreComboBox;

    @FXML
    private VBox statusSummaryBox;

    private VBox addCard; // Add class-level reference

    private boolean isDarkMode = true; // Default is dark mode

    @FXML
    public void initialize() {
        // Load font first to ensure it's available for the pie chart
        Font font = Font.loadFont(getClass().getResourceAsStream("/Feather.ttf"), 18);
        if (font == null) {
            System.err.println("Failed to load Feather.ttf font");
        }
        // Register the font as "Feather Bold" for use in CSS
        Font boldFont = Font.font("Feather Bold", 18);
        if (boldFont == null) {
            System.err.println("Failed to create Feather Bold font");
        }

        // Load logo image
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        logoImage.setImage(logo);

        // Get the theme preference from SessionManager
        isDarkMode = SessionManager.getInstance().isDarkMode();

        // Display session type information
        SessionType currentSession = SessionManager.getInstance().getSessionType();
        String sessionTypeText;
        if (currentSession == SessionType.ADMIN) {
            sessionTypeText = "Administrator";
            createCommunityButton.setVisible(false);
        } else if (currentSession == SessionType.ARTIST) {
            sessionTypeText = "Artist";
            createCommunityButton.setVisible(true);
        } else {
            sessionTypeText = "User";
            createCommunityButton.setVisible(false);
        }
        String sessionInfo = "Current session: " + sessionTypeText;
        Label sessionLabel = new Label(sessionInfo);
        sessionLabel.getStyleClass().add("session-info-label");

        // Set up search field listener
        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                handleSearch();
            }
        });

        // Set up status filter listeners
        statusFilter0.selectedProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        statusFilter1.selectedProperty().addListener((observable, oldValue, newValue) -> handleSearch());
        statusFilter2.selectedProperty().addListener((observable, oldValue, newValue) -> handleSearch());

        // Populate genre dropdown
        populateGenreDropdown();

        // Register as a theme change listener
        SessionManager.getInstance().addThemeChangeListener(this::handleThemeChange);

        // Highlight the current page's navigation label
        if (communitiesButton != null) {
            communitiesButton.getStyleClass().add("active");
        }

        // No need to apply CSS to the status summary box

        // Load communities
        loadCommunities();
    }

    /**
     * Handles theme changes from SessionManager.
     * 
     * @param isDarkMode true for dark mode, false for light mode
     */
    private void handleThemeChange(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        // No need to update UI directly, as the root layout will handle that
        // This ensures that any new dialogs or views created by this controller will use the correct theme
    }

    @FXML
    private void handleCommunitiesButtonClick() {
        try {
            // Check the session type to determine which view to show
            SessionType currentSession = SessionManager.getInstance().getSessionType();

            if (currentSession == SessionType.ADMIN) {
                // Admin users see the communities-view - we're already here, just refresh
                loadCommunities();
            } else {
                // User and Artist users see the user-communities-view
                SceneTransitionUtil.changeContentWithPreload(
                        "/views/communities/user-communities-view.fxml",
                    SceneTransitionUtil.TransitionType.FADE, 
                    UserCommunitiesController.class,
                    controller -> controller.loadCommunities()
                );
            }
        } catch (IOException e) {
            System.err.println("Failed to load communities view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpNextButtonClick() {
        try {
            SceneTransitionUtil.changeContentWithPreload(
                    "/views/communities/hello-view.fxml",
                SceneTransitionUtil.TransitionType.SLIDE_RIGHT, 
                MainController.class,
                controller -> {} // No data loading needed for this view
            );
        } catch (IOException e) {
            System.err.println("Failed to load hello-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleArtistsButtonClick() {
        try {
            SceneTransitionUtil.changeContentWithPreload(
                    "/views/communities/artists-view.fxml",
                SceneTransitionUtil.TransitionType.FADE, 
                ArtistsController.class,
                controller -> controller.loadArtists()
            );
        } catch (IOException e) {
            System.err.println("Failed to load artists-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateCommunityButtonClick() {
        try {
            // Navigate to the create community page
            SceneTransitionUtil.changeContentWithPreload(
                    "/views/communities/create-community-view.fxml",
                SceneTransitionUtil.TransitionType.FADE, 
                AddCommunityController.class,
                controller -> {
                    // Set the theme mode
                    controller.setThemeMode(SessionManager.getInstance().isDarkMode());
                    // Generate a unique ID automatically
                    controller.handleGenerateId();
                }
            );
        } catch (IOException e) {
            System.err.println("Failed to load create-community-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        String selectedGenre = genreComboBox.getValue();
        DatabaseService dbService = new DatabaseService();
        List<Community> communities;

        if (!searchTerm.isEmpty()) {
            communities = dbService.searchCommunities(searchTerm);
        } else {
            communities = dbService.getCommunities();
        }

        // Filter by genre if a specific genre is selected
        if (selectedGenre != null && !selectedGenre.equals("All Genres")) {
            communities = communities.stream()
                .filter(community -> selectedGenre.equals(community.getGenre()))
                .collect(Collectors.toList());
        }

        // Filter by status based on checkbox selections
        communities = filterCommunitiesByStatus(communities);

        displayCommunities(communities);
    }

    /**
     * Filters communities based on the status filter checkboxes.
     * 
     * @param communities The list of communities to filter
     * @return The filtered list of communities
     */
    private List<Community> filterCommunitiesByStatus(List<Community> communities) {
        // If all checkboxes are unticked, show all communities
        if (!statusFilter0.isSelected() && !statusFilter1.isSelected() && !statusFilter2.isSelected()) {
            return communities;
        }

        return communities.stream()
            .filter(community -> {
                int status = community.getStatus();
                if (status == 0 && !statusFilter0.isSelected()) return false;
                if (status == 1 && !statusFilter1.isSelected()) return false;
                if (status == 2 && !statusFilter2.isSelected()) return false;
                return true;
            })
            .collect(Collectors.toList());
    }

    public void loadCommunities() {
        DatabaseService dbService = new DatabaseService();
        List<Community> communities = dbService.getCommunities();

        // Apply genre filter if a specific genre is selected
        String selectedGenre = genreComboBox != null ? genreComboBox.getValue() : null;
        if (selectedGenre != null && !selectedGenre.equals("All Genres")) {
            communities = communities.stream()
                .filter(community -> selectedGenre.equals(community.getGenre()))
                .collect(Collectors.toList());
        }

        // Apply status filters if the checkboxes are initialized
        if (statusFilter0 != null && statusFilter1 != null && statusFilter2 != null) {
            communities = filterCommunitiesByStatus(communities);
        }

        displayCommunities(communities, true);
    }

    private void displayCommunities(List<Community> communities) {
        displayCommunities(communities, false);
    }

    private void displayCommunities(List<Community> communities, boolean updateChart) {
        communitiesGrid.getChildren().clear();

        // Update the status summary with ALL communities, not just filtered ones
        if (updateChart) {
            // Get all communities from database for status summary
            DatabaseService dbService = new DatabaseService();
            List<Community> allCommunities = dbService.getCommunities();
            updateStatusSummary(allCommunities);
        }

        int column = 0;
        int row = 0;
        int maxColumns = 3;

        // Check if we're in admin mode
        boolean isAdmin = SessionManager.getInstance().isAdminSession();

        for (Community community : communities) {
            TextField nameField = new TextField(community.getName());
            nameField.setVisible(false);
            nameField.getStyleClass().add("editable-name-field");

            ImageView imageView = createCommunityImageView(community);

            // Create a container for the image with border based on status
            javafx.scene.layout.StackPane imageContainer = new javafx.scene.layout.StackPane();
            imageContainer.setAlignment(Pos.CENTER);

            // Get the community status
            int status = community.getStatus();

            // For status 0 or 1, create an enhanced shadow effect
            if (status == 0 || status == 1) {
                // Create a more sophisticated drop shadow effect based on status
                DropShadow statusShadow = new DropShadow();
                statusShadow.setRadius(15);
                statusShadow.setSpread(0.5);
                statusShadow.setOffsetX(0);
                statusShadow.setOffsetY(0);

                // Set shadow color based on status with more vibrant appearance
                if (status == 0) {
                    statusShadow.setColor(Color.rgb(255, 0, 0, 0.7)); // Vibrant red shadow
                } else {
                    statusShadow.setColor(Color.rgb(255, 215, 0, 0.7)); // Vibrant gold shadow
                }

                // Apply the effect to the image view
                imageView.setEffect(statusShadow);

                // Add the image to the container
                imageContainer.getChildren().add(imageView);
            } else {
                // Just add the image for status 2
                imageContainer.getChildren().add(imageView);
            }

            Label nameLabel = new Label(community.getName());
            nameLabel.getStyleClass().add("name-label");

            VBox communityBox = new VBox(imageContainer, nameLabel, nameField);
            communityBox.setSpacing(10);
            communityBox.setAlignment(Pos.CENTER);
            communityBox.getStyleClass().add("community-box");

            // Add status-specific style class
            if (status == 0) {
                communityBox.getStyleClass().add("community-box-status-0");
            } else if (status == 1) {
                communityBox.getStyleClass().add("community-box-status-1");
            }

            if (isAdmin) {
                // Admin-only features
                ContextMenu contextMenu = new ContextMenu();
                MenuItem editItem = new MenuItem("Edit Name/Image");
                MenuItem editDescriptionItem = new MenuItem("Edit Description");
                MenuItem editGenreItem = new MenuItem("Edit Genre");
                MenuItem editSocialItem = new MenuItem("Edit Social");
                MenuItem deleteItem = new MenuItem("Delete");

                // Add verify option only if community is not already verified (status != 2)
                MenuItem verifyItem = new MenuItem("Verify Community");
                // Add unverify option only if community is verified (status = 2)
                MenuItem unverifyItem = new MenuItem("Unverify Community");

                if (community.getStatus() != 2) {
                    contextMenu.getItems().addAll(editItem, editDescriptionItem, editGenreItem, editSocialItem, deleteItem, verifyItem);
                } else {
                    contextMenu.getItems().addAll(editItem, editDescriptionItem, editGenreItem, editSocialItem, deleteItem, unverifyItem);
                }

                editItem.setOnAction(e -> {
                    nameLabel.setVisible(false);
                    nameField.setVisible(true);
                    nameField.requestFocus();
                });

                editDescriptionItem.setOnAction(e -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/communities/edit-description-dialog.fxml"));
                        Dialog<String> dialog = new Dialog<>();
                        dialog.setTitle("Edit Community Description");

                        DialogPane dialogPane = loader.load();
                        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                        dialogPane.setHeaderText(null); // Remove the default title bar

                        EditDescriptionController controller = loader.getController();
                        controller.setCommunityId(community.getId());
                        controller.setDescription(community.getDescription());
                        controller.setThemeMode(isDarkMode);

                        dialog.setResultConverter(buttonType -> {
                            if (buttonType == ButtonType.OK) {
                                return controller.getDescription();
                            }
                            return null;
                        });

                        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                        okButton.disableProperty().bind(controller.validDescriptionProperty().not());

                        dialog.setDialogPane(dialogPane);

                        // Set the dialog to use StageStyle.UNDECORATED
                        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                        stage.initStyle(StageStyle.UNDECORATED);

                        Optional<String> result = dialog.showAndWait();

                        if (result.isPresent()) {
                            String newDescription = result.get();
                            if (!newDescription.equals(community.getDescription())) {
                                DatabaseService dbService = new DatabaseService();
                                dbService.updateCommunityDescription(community.getId(), newDescription);
                                community.setDescription(newDescription);
                            }
                        }
                    } catch (IOException ex) {
                        showErrorAlert("Dialog Error", "Failed to load description dialog");
                        ex.printStackTrace();
                    }
                });

                editGenreItem.setOnAction(e -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/communities/edit-genre-dialog.fxml"));
                        Dialog<String> dialog = new Dialog<>();
                        dialog.setTitle("Edit Community Genre");

                        DialogPane dialogPane = loader.load();
                        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                        dialogPane.setHeaderText(null); // Remove the default title bar

                        EditGenreController controller = loader.getController();
                        controller.setCommunityId(community.getId());
                        controller.setGenre(community.getGenre());
                        controller.setThemeMode(isDarkMode);

                        dialog.setResultConverter(buttonType -> {
                            if (buttonType == ButtonType.OK) {
                                return controller.getGenre();
                            }
                            return null;
                        });

                        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                        okButton.disableProperty().bind(controller.validGenreProperty().not());

                        dialog.setDialogPane(dialogPane);

                        // Set the dialog to use StageStyle.UNDECORATED
                        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                        stage.initStyle(StageStyle.UNDECORATED);

                        Optional<String> result = dialog.showAndWait();

                        if (result.isPresent()) {
                            String newGenre = result.get();
                            if (!newGenre.equals(community.getGenre())) {
                                DatabaseService dbService = new DatabaseService();
                                dbService.updateCommunityGenre(community.getId(), newGenre);
                                community.setGenre(newGenre);
                                // Reload communities to reflect the changes
                                loadCommunities();
                            }
                        }
                    } catch (IOException ex) {
                        showErrorAlert("Dialog Error", "Failed to load genre dialog");
                        ex.printStackTrace();
                    }
                });

                editSocialItem.setOnAction(e -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/communities/edit-social-dialog.fxml"));
                        Dialog<String> dialog = new Dialog<>();
                        dialog.setTitle("Edit Social Media Link");

                        DialogPane dialogPane = loader.load();
                        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                        dialogPane.setHeaderText(null); // Remove the default title bar

                        EditSocialController controller = loader.getController();
                        controller.setCommunityId(community.getId());
                        controller.setSocial(community.getSocial());
                        controller.setThemeMode(isDarkMode);

                        dialog.setResultConverter(buttonType -> {
                            if (buttonType == ButtonType.OK) {
                                return controller.getSocial();
                            }
                            return null;
                        });

                        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                        okButton.disableProperty().bind(controller.validSocialProperty().not());

                        dialog.setDialogPane(dialogPane);

                        // Set the dialog to use StageStyle.UNDECORATED
                        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                        stage.initStyle(StageStyle.UNDECORATED);

                        Optional<String> result = dialog.showAndWait();

                        if (result.isPresent()) {
                            String newSocial = result.get();
                            if (!newSocial.equals(community.getSocial())) {
                                DatabaseService dbService = new DatabaseService();
                                dbService.updateCommunitySocial(community.getId(), newSocial);
                                community.setSocial(newSocial);
                            }
                        }
                    } catch (IOException ex) {
                        showErrorAlert("Dialog Error", "Failed to load social media dialog");
                        ex.printStackTrace();
                    }
                });

                nameField.setOnKeyPressed(event -> {
                    if (event.getCode().toString().equals("ENTER")) {
                        String newName = nameField.getText().trim();
                        if (!newName.isEmpty() && !newName.equals(community.getName())) {
                            DatabaseService dbService = new DatabaseService();
                            dbService.updateCommunity(community.getId(), newName);
                            community.setName(newName);
                            nameLabel.setText(newName);
                        }
                        nameField.setVisible(false);
                        nameLabel.setVisible(true);
                    }
                });

                imageContainer.setOnMouseClicked(event -> {
                    // Only respond to left clicks
                    if (event.getButton() != MouseButton.PRIMARY) {
                        return;
                    }

                    if (event.getClickCount() == 2) {
                        // Double-click: edit image
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
                        );
                        File file = fileChooser.showOpenDialog(communityBox.getScene().getWindow());
                        if (file != null) {
                            try {
                                byte[] newImageData = Files.readAllBytes(file.toPath());
                                DatabaseService dbService = new DatabaseService();
                                dbService.updateCommunityImage(community.getId(), newImageData);
                                community.setImage(newImageData);
                                // Load image with size constraints to prevent loading overly large images
                                imageView.setImage(new Image(new ByteArrayInputStream(newImageData), 300, 300, true, true));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    } else if (event.getClickCount() == 1) {
                        // Single-click: navigate to community detail view
                        try {
                            CommunityDetailController controller = SceneTransitionUtil.changeContent(
                                    "/views/communities/community-detail-view.fxml",
                                SceneTransitionUtil.TransitionType.FADE,
                                CommunityDetailController.class
                            );
                            controller.loadCommunityDetails(community.getId());
                        } catch (IOException ex) {
                            showErrorAlert("Navigation Error", "Failed to navigate to community details");
                            ex.printStackTrace();
                        }
                    }
                });

                deleteItem.setOnAction(e -> {
                    Community communityToDelete = (Community) communityBox.getUserData();
                    if (communityToDelete != null) {
                        DatabaseService dbService = new DatabaseService();
                        dbService.deleteCommunity(communityToDelete.getId());
                        loadCommunities();
                    }
                });

                verifyItem.setOnAction(e -> {
                    Community communityToVerify = (Community) communityBox.getUserData();
                    if (communityToVerify != null) {
                        DatabaseService dbService = new DatabaseService();
                        // Update community status to 2 (verified)
                        dbService.updateCommunityStatus(communityToVerify.getId(), 2);
                        communityToVerify.setStatus(2);
                        // Reload communities to reflect the changes
                        loadCommunities();
                    }
                });

                unverifyItem.setOnAction(e -> {
                    Community communityToUnverify = (Community) communityBox.getUserData();
                    if (communityToUnverify != null) {
                        DatabaseService dbService = new DatabaseService();
                        // Update community status to 0 (unverified)
                        dbService.updateCommunityStatus(communityToUnverify.getId(), 0);
                        communityToUnverify.setStatus(0);
                        // Reload communities to reflect the changes
                        loadCommunities();
                    }
                });

                communityBox.setUserData(community);
                communityBox.setOnContextMenuRequested(event -> {
                    contextMenu.show(communityBox, event.getScreenX(), event.getScreenY());
                });
            } else {
                // User session - add tooltip to explain admin features are disabled
                Tooltip tooltip = new Tooltip("Admin privileges required to edit communities");
                Tooltip.install(communityBox, tooltip);

                // Set single click handler to view community details
                imageContainer.setOnMouseClicked(event -> {
                    // Only respond to left clicks
                    if (event.getButton() != MouseButton.PRIMARY) {
                        return;
                    }

                    // Navigate to community detail view
                    try {
                        CommunityDetailController controller = SceneTransitionUtil.changeContent(
                                "/views/communities/community-detail-view.fxml",
                            SceneTransitionUtil.TransitionType.FADE,
                            CommunityDetailController.class
                        );
                        controller.loadCommunityDetails(community.getId());
                    } catch (IOException ex) {
                        showErrorAlert("Navigation Error", "Failed to navigate to community details");
                        ex.printStackTrace();
                    }
                });
            }

            communitiesGrid.add(communityBox, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }

        // Only show the add new community card for admin users
        if (isAdmin) {
            addNewCommunityCard();
        }
    }

    private void addNewCommunityCard() {
        if (addCardContainer == null) return;

        addCardContainer.getChildren().clear();

        if (addCard == null) {
            addCard = new VBox();
            addCard.setAlignment(Pos.CENTER);
            addCard.setSpacing(10);
            addCard.getStyleClass().add("community-box");
            addCard.setCursor(Cursor.HAND);

            Label plusLabel = new Label("+");
            plusLabel.getStyleClass().add("plus-label");

            Label addLabel = new Label("Add New Community");
            addLabel.getStyleClass().add("add-label");

            addCard.getChildren().addAll(plusLabel, addLabel);
            addCard.setOnMouseClicked(e -> {
                // Only respond to left clicks
                if (e.getButton() == MouseButton.PRIMARY) {
                    handleAddCommunity();
                }
            });
        }

        addCardContainer.getChildren().add(addCard);
    }

    private ImageView createCommunityImageView(Community community) {
        // Create the image view
        ImageView imageView = new ImageView();
        byte[] imageData = community.getImage();
        if (imageData != null) {
            // Load image with size constraints to prevent loading overly large images
            // This improves performance for large images while maintaining display quality
            Image image = new Image(new ByteArrayInputStream(imageData), 300, 300, true, true);
            imageView.setImage(image);
        }

        // Set properties for better image display
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Center the image within the view
        imageView.setCache(true);

        // Don't use viewport - let the ImageView handle fitting the image with preserveRatio=true
        // This ensures the whole image is visible without zooming in
        imageView.setViewport(null);

        // Create a clip for the circular shape
        Circle clip = new Circle(50, 50, 50);
        imageView.setClip(clip);

        // Apply drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));
        imageView.setEffect(dropShadow);

        // Apply enhanced effect based on status
        int status = community.getStatus();
        if (status == 0 || status == 1) {
            // Create a more sophisticated drop shadow effect based on status
            DropShadow statusShadow = new DropShadow();
            statusShadow.setRadius(15);
            statusShadow.setSpread(0.5);
            statusShadow.setOffsetX(0);
            statusShadow.setOffsetY(0);

            // Set shadow color based on status with more vibrant appearance
            if (status == 0) {
                statusShadow.setColor(Color.rgb(255, 0, 0, 0.7)); // Vibrant red shadow
            } else {
                statusShadow.setColor(Color.rgb(255, 215, 0, 0.7)); // Vibrant gold shadow
            }

            // Combine with the existing drop shadow
            DropShadow existingShadow = (DropShadow) imageView.getEffect();
            statusShadow.setInput(existingShadow);

            // Apply the combined effect
            imageView.setEffect(statusShadow);
        }

        return imageView;
    }

    @FXML
    private void handleAddCommunity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/communities/add-community-dialog.fxml"));
            Dialog<Community> dialog = new Dialog<>();
            dialog.setTitle("Add New Community");

            DialogPane dialogPane = loader.load();
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialogPane.setHeaderText(null); // Remove the default title bar

            AddCommunityController controller = loader.getController();
            controller.setThemeMode(isDarkMode);
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return controller.getNewCommunity();
                }
                return null;
            });

            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            okButton.disableProperty().bind(
                controller.validNameProperty().not()
                    .or(controller.validIdProperty().not())
                    .or(controller.validImageProperty().not())
                    .or(controller.validDescriptionProperty().not())
                    .or(controller.validGenreProperty().not())
            );

            dialog.setDialogPane(dialogPane);

            // Set the dialog to use StageStyle.UNDECORATED
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.initStyle(StageStyle.UNDECORATED);

            Optional<Community> result = dialog.showAndWait();

            if (result.isPresent()) {
                Community newCommunity = result.get();
                DatabaseService dbService = new DatabaseService();
                dbService.addCommunityWithId(
                    newCommunity.getId(),
                    newCommunity.getName(),
                    newCommunity.getImage(),
                    newCommunity.getDescription(),
                    newCommunity.getStatus(),
                    newCommunity.getSocial(),
                    newCommunity.getGenre()
                );
                loadCommunities();
            }
        } catch (IOException e) {
            showErrorAlert("Dialog Error", "Failed to load community dialog");
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Sets the theme mode for this controller.
     * 
     * @param isDarkMode true for dark mode, false for light mode
     */
    public void setThemeMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
    }

    /**
     * Populates the genre dropdown with genres from the database.
     */
    private void populateGenreDropdown() {
        DatabaseService dbService = new DatabaseService();
        List<String> genres = dbService.getAllGenres();

        // Add "All Genres" option at the beginning
        ObservableList<String> genreOptions = FXCollections.observableArrayList();
        genreOptions.add("All Genres");
        genreOptions.addAll(genres);

        genreComboBox.setItems(genreOptions);
        genreComboBox.getSelectionModel().selectFirst(); // Select "All Genres" by default
    }

    /**
     * Handles genre filter selection.
     */
    @FXML
    private void handleGenreFilter() {
        handleSearch();
    }

    // Constants for status labels
    private static final String STATUS_UNVERIFIED = "Unverified";
    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_VERIFIED = "Verified";

    /**
     * Updates the status summary with community status data.
     * 
     * @param communities The list of communities to analyze
     */
    private void updateStatusSummary(List<Community> communities) {
        if (statusSummaryBox == null) return;

        // Count communities by status
        int unverifiedCount = 0;
        int pendingCount = 0;
        int verifiedCount = 0;

        for (Community community : communities) {
            int status = community.getStatus();
            if (status == 0) {
                unverifiedCount++;
            } else if (status == 1) {
                pendingCount++;
            } else if (status == 2) {
                verifiedCount++;
            }
        }

        // Update the status summary
        updateStatusSummary(verifiedCount, pendingCount, unverifiedCount);
    }

    /**
     * Updates the status summary with the given counts.
     * 
     * @param verified The number of verified communities
     * @param pending The number of pending communities
     * @param unverified The number of unverified communities
     */
    private void updateStatusSummary(int verified, int pending, int unverified) {
        statusSummaryBox.getChildren().clear();

        int total = verified + pending + unverified;
        if (total == 0) return;

        int delayIncrementMs = 150; // Delay between each bar (in milliseconds)

        addAnimatedStatusBar("✔ Verified", verified, total, "#4CAF50", 0 * delayIncrementMs);     // Green
        addAnimatedStatusBar("⏳ Pending", pending, total, "#FFC107", 1 * delayIncrementMs);      // Amber
        addAnimatedStatusBar("❌ Unverified", unverified, total, "#F44336", 2 * delayIncrementMs); // Red
    }

    /**
     * Adds an animated status bar to the status summary box.
     * 
     * @param label The label for the status bar
     * @param value The value for the status bar
     * @param total The total value for calculating the percentage
     * @param colorHex The color for the status bar
     * @param delayMs The delay before starting the animation (in milliseconds)
     */
    private void addAnimatedStatusBar(String label, int value, int total, String colorHex, int delayMs) {
        double percent = (double) value / total;
        int percentInt = (int) (percent * 100);

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("status-bar-name-label");

        Region bar = new Region();
        bar.setPrefHeight(8);
        bar.setPrefWidth(0); // Start with width 0
        bar.setOpacity(0);   // Start fully transparent
        bar.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 5;");

        Label percentLabel = new Label(percentInt + "%");
        percentLabel.getStyleClass().add("status-bar-percent-label");

        HBox row = new HBox(8, nameLabel, bar, percentLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        statusSummaryBox.getChildren().add(row);

        // Animate width and opacity
        Timeline timeline = new Timeline();

        double targetWidth = percent * 180; // Max width 180

        KeyValue widthValue = new KeyValue(bar.prefWidthProperty(), targetWidth);
        KeyValue fadeValue = new KeyValue(bar.opacityProperty(), 1.0); // Fade to fully visible

        // Add delay to the animation
        KeyFrame keyFrame = new KeyFrame(Duration.millis(800), widthValue, fadeValue);

        timeline.getKeyFrames().add(keyFrame);

        // Set the delay before starting the animation
        timeline.setDelay(Duration.millis(delayMs));

        timeline.play();
    }
}
