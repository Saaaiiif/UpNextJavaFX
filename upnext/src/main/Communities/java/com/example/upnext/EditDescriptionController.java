package com.example.upnext;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class EditDescriptionController {
    @FXML private TextArea descriptionField;
    
    private BooleanProperty validDescription = new SimpleBooleanProperty(false);
    private int communityId;
    
    @FXML
    public void initialize() {
        setupDescriptionValidation();
    }
    
    private void setupDescriptionValidation() {
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> {
            validDescription.set(!newVal.trim().isEmpty());
            
            // Limit to 500 characters
            if (newVal.length() > 500) {
                descriptionField.setText(newVal.substring(0, 500));
            }
        });
    }
    
    public void setCommunityId(int id) {
        this.communityId = id;
    }
    
    public void setDescription(String description) {
        descriptionField.setText(description);
        validDescription.set(!description.trim().isEmpty());
    }
    
    public String getDescription() {
        return descriptionField.getText().trim();
    }
    
    public BooleanProperty validDescriptionProperty() {
        return validDescription;
    }
}