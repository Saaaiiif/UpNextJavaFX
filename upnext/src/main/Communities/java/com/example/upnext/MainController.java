package com.example.upnext;

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
import java.nio.file.Files;
import java.io.ByteArrayInputStream;
import javafx.scene.control.Dialog;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

public class MainController {

    public ImageView searchIcon;
    @FXML
    private ImageView logoImage;

    @FXML
    private VBox communitiesContainer;

    @FXML
    private GridPane communitiesGrid;

    @FXML
    private TextField searchField;

    @FXML
    private HBox addCardContainer;

    @FXML
    private Button communitiesButton;

    private VBox addCard; // Add class-level reference

    // Field for window operations
    private Stage stage;

    @FXML
    public void initialize() {
        Font.loadFont(getClass().getResourceAsStream("/Feather.ttf"), 18);
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        logoImage.setImage(logo);

        // Display session type information
        SessionType currentSession = SessionManager.getInstance().getSessionType();
        String sessionTypeText;
        if (currentSession == SessionType.ADMIN) {
            sessionTypeText = "Administrator";
        } else if (currentSession == SessionType.ARTIST) {
            sessionTypeText = "Artist";
        } else {
            sessionTypeText = "User";
        }
        String sessionInfo = "Current session: " + sessionTypeText;
        Label sessionLabel = new Label(sessionInfo);
        sessionLabel.setStyle("-fx-text-fill: #acacac; -fx-font-size: 12px; -fx-font-family: 'Feather';");

        // If we have a container to add this to, add it
        if (communitiesContainer != null) {
            communitiesContainer.getChildren().add(0, sessionLabel);
        }
    }

    @FXML
    private void handleCommunitiesButtonClick() {
        try {
            // Check the session type to determine which view to show
            SessionType currentSession = SessionManager.getInstance().getSessionType();

            if (currentSession == SessionType.ADMIN) {
                // Admin users see the communities-view
                SceneTransitionUtil.changeContentWithPreload(
                    "/com/example/upnext/communities-view.fxml", 
                    SceneTransitionUtil.TransitionType.FADE, 
                    MainController.class,
                    controller -> {
                        controller.loadCommunities();

                        controller.searchField.textProperty().addListener(new ChangeListener<String>() {
                            @Override
                            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                                controller.handleSearch();
                            }
                        });
                    }
                );
            } else {
                // User and Artist users see the user-communities-view
                SceneTransitionUtil.changeContentWithPreload(
                    "/com/example/upnext/user-communities-view.fxml", 
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
                "/com/example/upnext/hello-view.fxml", 
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
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        DatabaseService dbService = new DatabaseService();
        List<Community> communities;

        if (!searchTerm.isEmpty()) {
            communities = dbService.searchCommunities(searchTerm);
        } else {
            communities = dbService.getCommunities();
        }

        displayCommunities(communities);
    }

    public void loadCommunities() {
        DatabaseService dbService = new DatabaseService();
        List<Community> communities = dbService.getCommunities();
        displayCommunities(communities);
    }

    private void displayCommunities(List<Community> communities) {
        communitiesGrid.getChildren().clear();

        int column = 0;
        int row = 0;
        int maxColumns = 3;

        // Check if we're in admin mode
        boolean isAdmin = SessionManager.getInstance().isAdminSession();

        for (Community community : communities) {
            TextField nameField = new TextField(community.getName());
            nameField.setVisible(false);
            nameField.setStyle("-fx-font-size: 16px; -fx-font-family: 'Feather Bold'; " +
                              "-fx-text-fill: #acacac; -fx-background-color: transparent; " +
                              "-fx-border-color: transparent; -fx-alignment: center;");

            ImageView imageView = createCommunityImageView(community);

            Label nameLabel = new Label(community.getName());
            nameLabel.setStyle("-fx-text-fill: #acacac; -fx-font-size: 16px; -fx-font-family: 'Feather Bold'");

            VBox communityBox = new VBox(imageView, nameLabel, nameField);
            communityBox.setSpacing(10);
            communityBox.setAlignment(Pos.CENTER);
            communityBox.getStyleClass().add("community-box");

            if (isAdmin) {
                // Admin-only features
                ContextMenu contextMenu = new ContextMenu();
                MenuItem editItem = new MenuItem("Edit Name/Image");
                MenuItem editDescriptionItem = new MenuItem("Edit Description");
                MenuItem deleteItem = new MenuItem("Delete");
                contextMenu.getItems().addAll(editItem, editDescriptionItem, deleteItem);

                editItem.setOnAction(e -> {
                    nameLabel.setVisible(false);
                    nameField.setVisible(true);
                    nameField.requestFocus();
                });

                editDescriptionItem.setOnAction(e -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/upnext/edit-description-dialog.fxml"));
                        Dialog<String> dialog = new Dialog<>();
                        dialog.setTitle("Edit Community Description");

                        DialogPane dialogPane = loader.load();
                        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                        EditDescriptionController controller = loader.getController();
                        controller.setCommunityId(community.getId());
                        controller.setDescription(community.getDescription());

                        dialog.setResultConverter(buttonType -> {
                            if (buttonType == ButtonType.OK) {
                                return controller.getDescription();
                            }
                            return null;
                        });

                        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
                        okButton.disableProperty().bind(controller.validDescriptionProperty().not());

                        dialog.setDialogPane(dialogPane);
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

                imageView.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
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
                                imageView.setImage(new Image(new ByteArrayInputStream(newImageData)));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
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

                communityBox.setUserData(community);
                communityBox.setOnContextMenuRequested(event -> {
                    contextMenu.show(communityBox, event.getScreenX(), event.getScreenY());
                });
            } else {
                // User session - add tooltip to explain admin features are disabled
                Tooltip tooltip = new Tooltip("Admin privileges required to edit communities");
                Tooltip.install(communityBox, tooltip);

                // Set single click handler to view community details
                imageView.setOnMouseClicked(event -> {
                    // Navigate to community detail view
                    try {
                        CommunityDetailController controller = SceneTransitionUtil.changeContent(
                            "/com/example/upnext/community-detail-view.fxml",
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
            plusLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: #BD2526;");

            Label addLabel = new Label("Add New Community");
            addLabel.setStyle("-fx-text-fill: #acacac; -fx-font-size: 16px; -fx-font-family: 'Feather Bold'");

            addCard.getChildren().addAll(plusLabel, addLabel);
            addCard.setOnMouseClicked(e -> handleAddCommunity());
        }

        addCardContainer.getChildren().add(addCard);
    }

    private ImageView createCommunityImageView(Community community) {
        ImageView imageView = new ImageView();
        byte[] imageData = community.getImage();
        if (imageData != null) {
            Image image = new Image(new ByteArrayInputStream(imageData));
            imageView.setImage(image);
        }
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        Circle clip = new Circle(50, 50, 50);
        imageView.setClip(clip);

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(0);
        dropShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.3));
        imageView.setEffect(dropShadow);

        return imageView;
    }

    @FXML
    private void handleAddCommunity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/upnext/add-community-dialog.fxml"));
            Dialog<Community> dialog = new Dialog<>();
            dialog.setTitle("Add New Community");

            DialogPane dialogPane = loader.load();
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            AddCommunityController controller = loader.getController();
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
            );

            dialog.setDialogPane(dialogPane);
            Optional<Community> result = dialog.showAndWait();

            if (result.isPresent()) {
                Community newCommunity = result.get();
                DatabaseService dbService = new DatabaseService();
                dbService.addCommunityWithId(
                    newCommunity.getId(),
                    newCommunity.getName(),
                    newCommunity.getImage(),
                    newCommunity.getDescription()
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
     * Sets the stage for window operations.
     * @param stage The primary stage of the application
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
