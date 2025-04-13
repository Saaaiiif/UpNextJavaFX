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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MainController {

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
            Label nameLabel = new Label(community.getName());
            nameLabel.setStyle("-fx-text-fill: #acacac; -fx-font-size: 16px; -fx-font-family: 'Feather Bold'");

            ImageView imageView = new ImageView();
            byte[] imageData = community.getImage();
            if (imageData != null) {
                Image image = new Image(new ByteArrayInputStream(imageData));
                imageView.setImage(image);
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
            }

            VBox communityBox = new VBox(imageView, nameLabel);
            communityBox.setSpacing(10);
            communityBox.setAlignment(Pos.CENTER);
            communityBox.getStyleClass().add("community-box");

            communitiesGrid.add(communityBox, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }
    }
}