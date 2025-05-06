package edu.up_next.tools;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class QRCodeScanner {
    public static String scanQRCode() {
        Webcam webcam = Webcam.getDefault();
        webcam.open();
        WebcamPanel panel = new WebcamPanel(webcam);
        JFrame window = new JFrame("Scanner QR Code");
        window.add(panel);
        window.setResizable(true);
        window.pack();
        window.setVisible(true);

        String resultText = null;
        while (resultText == null) {
            BufferedImage image = webcam.getImage();
            if (image == null) continue;
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                Result result = new MultiFormatReader().decode(bitmap);
                resultText = result.getText();
                break;
            } catch (NotFoundException e) {
                // Aucun QR code détecté, continue
            }
        }
        webcam.close();
        window.dispose();
        return resultText;
    }
} 