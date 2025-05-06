package controllers.event_reser_ouma;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class LocationPicker {

    @FXML
    private WebView mapView;

    private WebEngine webEngine;
    private String selectedLocation;

    // ➡️ Ajouter latitude et longitude statiques
    private static double latitude;
    private static double longitude;

    public static double getLatitude() {
        return latitude;
    }

    public static double getLongitude() {
        return longitude;
    }

    /** Pont JavaScript → Java */
    public class JSBridge {
        /** Appelé depuis mapbox.html */
        public void setLocation(String placeName, double lat, double lon) {
            selectedLocation = placeName;
            latitude = lat;
            longitude = lon;
            System.out.println("📍 Localisation choisie : " + placeName + " (" + lat + ", " + lon + ")");
        }
    }

    @FXML
    public void initialize() {
        webEngine = mapView.getEngine();

        webEngine.load(getClass().getResource("/map.html").toExternalForm());

        JSBridge bridge = new JSBridge();
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", bridge);

                // ⚡ Quand le WebView est ajouté à une scène
                mapView.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
                    if (newScene != null) {
                        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
                        pause.setOnFinished(e -> {
                            try {
                                webEngine.executeScript("""
                            if (window.map && typeof window.map.invalidateSize === 'function') {
                                window.map.invalidateSize();
                            }
                            """);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                        pause.play();
                    }
                });
            }
        });
    }




    /** Bouton "Confirmer" lié à onMouseClicked="#confirmLocation" */
    @FXML
    private void confirmLocation(MouseEvent evt) {
        if (selectedLocation != null) {
            System.out.println("✅ Localisation validée : " + selectedLocation);
            ((Button) evt.getSource()).getScene().getWindow().hide();
        } else {
            System.out.println("❌ Aucune localisation sélectionnée.");
        }
    }

    /** Récupère la valeur depuis le code appelant */
    public String getSelectedLocation() {
        return selectedLocation;
    }
}
