package edu.up_next.tools;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class TTSMp3 {
    public static File getSpeechMp3(String text, String lang) throws IOException {
        URL url = new URL("https://ttsmp3.com/makemp3_new.php");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        String params = "msg=" + java.net.URLEncoder.encode(text, "UTF-8") +
                        "&lang=" + lang + "&source=ttsmp3";
        try (OutputStream os = con.getOutputStream()) {
            os.write(params.getBytes());
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        String json = response.toString();
        System.out.println("Réponse JSON : " + json);
        String mp3Url = null;
        int urlIndex = json.indexOf("\"URL\":\"");
        if (urlIndex != -1) {
            int start = urlIndex + 7;
            int end = json.indexOf("\"", start);
            if (end != -1) {
                mp3Url = json.substring(start, end).replace("\\/", "/");
            }
        }
        if (mp3Url == null) throw new IOException("MP3 URL not found");
        // Télécharger le MP3
        File tempMp3 = File.createTempFile("tts_", ".mp3");
        try (InputStream is = new URL(mp3Url).openStream();
             FileOutputStream fos = new FileOutputStream(tempMp3)) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = is.read(buffer)) != -1) fos.write(buffer, 0, n);
        }
        return tempMp3;
    }
} 