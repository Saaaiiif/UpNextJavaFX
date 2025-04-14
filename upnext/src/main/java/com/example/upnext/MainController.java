package com.example.upnext;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
import javafx.stage.Modality;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
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
    public void initialize() {
        Font.loadFont(getClass().getResourceAsStream("/Feather.ttf"), 18);
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        logoImage.setImage(logo);
    }

    @FXML
    private void handleCommunitiesButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/upnext/communities-view.fxml"));
            Stage stage = (Stage) logoImage.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/example/upnext/styles.css")).toExternalForm());
            stage.setScene(scene);
            stage.show();

            MainController controller = loader.getController();
            controller.loadCommunities();

            controller.searchField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    controller.handleSearch();
                }
            });
        } catch (IOException e) {
            System.err.println("Failed to load communities-view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpNextButtonClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/upnext/hello-view.fxml"));
            Stage stage = (Stage) logoImage.getScene().getWindow();
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/example/upnext/styles.css")).toExternalForm());
            stage.setScene(scene);
            stage.show();
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

        for (Community community : communities) {
            // Create editable text field (hidden by default)
            TextField nameField = new TextField(community.getName());
            nameField.setVisible(false);
            nameField.setStyle("-fx-font-size: 16px; -fx-font-family: 'Feather Bold'; " +
                              "-fx-text-fill: #acacac; -fx-background-color: transparent; " +
                              "-fx-border-color: transparent; -fx-alignment: center;");
            
            // Create image view
            ImageView imageView = createCommunityImageView(community);
            
            // Create visible label
            Label nameLabel = new Label(community.getName());
            nameLabel.setStyle("-fx-text-fill: #acacac; -fx-font-size: 16px; -fx-font-family: 'Feather Bold'");

            VBox communityBox = new VBox(imageView, nameLabel, nameField);
            communityBox.setSpacing(10);
            communityBox.setAlignment(Pos.CENTER);
            communityBox.getStyleClass().add("community-box");

            // Context menu setup
            ContextMenu contextMenu = new ContextMenu();
            MenuItem editItem = new MenuItem("Edit");
            MenuItem deleteItem = new MenuItem("Delete");
            contextMenu.getItems().addAll(editItem, deleteItem);

            editItem.setOnAction(e -> {
                nameLabel.setVisible(false);
                nameField.setVisible(true);
                nameField.requestFocus();
            });

            // Handle text field changes
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

            // Handle image editing on double-click
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

            communitiesGrid.add(communityBox, column, row);
            
            // Grid position updates
            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }

        // Add a "+" card as the last item
        addNewCommunityCard();
    }

    private void addNewCommunityCard() {
        VBox addCard = new VBox();
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
        
        // Add to the grid
        int lastRow = communitiesGrid.getRowCount();
        int lastColumn = communitiesGrid.getColumnCount();
        communitiesGrid.add(addCard, lastColumn, lastRow);
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

        // Add circular clip and shadow
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
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader.load());
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            AddCommunityController controller = loader.getController();
            Optional<ButtonType> result = dialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Community newCommunity = controller.getNewCommunity();
                if (newCommunity != null) {
                    DatabaseService dbService = new DatabaseService();
                    dbService.addCommunityWithId(
                        newCommunity.getId(),
                        newCommunity.getName(),
                        newCommunity.getImage()
                    );
                    loadCommunities(); // Refresh the list
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}