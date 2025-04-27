package com.example.upnext;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Font;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class UserCommunitiesController {

    @FXML
    private ImageView logoImage;

    @FXML
    private GridPane communitiesGrid;


    @FXML
    private Button communitiesButton;

    @FXML
    public void initialize() {
        Font.loadFont(getClass().getResourceAsStream("/Feather.ttf"), 18);
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")));
        logoImage.setImage(logo);


        // Add right-click handler to Communities button if it exists
        if (communitiesButton != null) {
            communitiesButton.setOnMouseClicked(event -> {
                // No special handling needed for right-click in user communities view
                // as we're already in this view
            });
        }

        // Load communities
        loadCommunities();
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
    private void handleCommunitiesButtonClick() {
        try {
            // Check the session type to determine which view to show
            SessionType currentSession = SessionManager.getInstance().getSessionType();

            if (currentSession == SessionType.ADMIN) {
                // Admin users see the communities-view
                SceneTransitionUtil.changeContentWithPreload(
                    "/com/example/upnext/communities-view.fxml", 
                    SceneTransitionUtil.TransitionType.SLIDE_LEFT, 
                    MainController.class,
                    controller -> controller.loadCommunities()
                );
            } else {
                // User and Artist users stay on user-communities-view
                // No need to navigate since we're already here
                // Just reload the communities to refresh the view
                loadCommunities();
            }
        } catch (IOException e) {
            System.err.println("Failed to load communities view: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void loadCommunities() {
        DatabaseService dbService = new DatabaseService();
        List<Community> communities = dbService.getCommunitiesWithStatus2();
        displayCommunities(communities);
    }

    private void displayCommunities(List<Community> communities) {
        communitiesGrid.getChildren().clear();

        int column = 0;
        int row = 0;
        int maxColumns = 3;

        for (Community community : communities) {
            ImageView imageView = createCommunityImageView(community);

            Label nameLabel = new Label(community.getName());
            nameLabel.getStyleClass().add("name-label");
            nameLabel.setStyle("-fx-font-size: 16px; -fx-font-family: 'Feather Bold'");

            VBox communityBox = new VBox(imageView, nameLabel);
            communityBox.setSpacing(10);
            communityBox.setAlignment(Pos.CENTER);
            communityBox.getStyleClass().add("user-community-box");

            // Add click handler to navigate to community detail view
            final int communityId = community.getId(); // Create a final copy for lambda
            communityBox.setOnMouseClicked(event -> {
                try {
                    CommunityDetailController controller = SceneTransitionUtil.changeContent(
                        "/com/example/upnext/community-detail-view.fxml",
                        SceneTransitionUtil.TransitionType.SLIDE_LEFT,
                        CommunityDetailController.class
                    );
                    controller.loadCommunityDetails(communityId);
                } catch (IOException e) {
                    System.err.println("Failed to load community-detail-view.fxml: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            communitiesGrid.add(communityBox, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }
    }

    private ImageView createCommunityImageView(Community community) {
        ImageView imageView = new ImageView();
        byte[] imageData = community.getImage();
        if (imageData != null) {
            Image image = new Image(new ByteArrayInputStream(imageData));
            imageView.setImage(image);
        }
        imageView.setFitWidth(250);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        // Apply rounded corners using CSS
        imageView.setStyle("-fx-background-radius: 8px; -fx-border-radius: 8px;");

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(10);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(0);
        dropShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.3));
        imageView.setEffect(dropShadow);

        return imageView;
    }
}
