package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class StatistiquesCategorieController {
    @FXML private ImageView statistiquesImageView;
    @FXML private Button retourButton;

    public void initialize() {
        retourButton.setOnAction(e -> {
            Stage stage = (Stage) retourButton.getScene().getWindow();
            stage.close();
        });
    }

    public void setStatistiquesImageUrl(String url) {
        statistiquesImageView.setImage(new Image(url));
    }
} 