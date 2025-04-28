package controllers;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import java.io.FileInputStream;
import java.io.IOException;

public class GoogleCloudConfig {
    public static ImageAnnotatorClient createVisionClient() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                        new FileInputStream("src/main/resources/credentials.json"))
                .createScoped("https://www.googleapis.com/auth/cloud-platform");
        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
        return ImageAnnotatorClient.create(settings);
    }
}