package com.example.upnext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class for interacting with the Spotify Web API.
 */
public class SpotifyService {
    // These should be replaced with actual credentials
    private static final String CLIENT_ID = "9eefc7a467f8402f80b3a21654ada144";
    private static final String CLIENT_SECRET = "75e28e24f0e944bab1db0dd2e4228034";

    private static String accessToken;
    private static long tokenExpirationTime;

    /**
     * Authenticates with Spotify API using Client Credentials flow.
     * Includes retry mechanism for handling temporary service unavailability.
     * @return true if authentication was successful
     */
    public static boolean authenticate() {
        // Maximum number of retry attempts
        final int MAX_RETRIES = 3;
        // Initial backoff time in milliseconds
        int backoffTime = 1000;

        // Check if we already have a valid token
        if (accessToken != null && System.currentTimeMillis() < tokenExpirationTime) {
            return true;
        }

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                URL url = new URL("https://accounts.spotify.com/api/token");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                // Set connection timeouts
                conn.setConnectTimeout(10000); // 10 seconds connection timeout
                conn.setReadTimeout(15000);    // 15 seconds read timeout

                // Set headers
                String auth = CLIENT_ID + ":" + CLIENT_SECRET;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Set request body
                String requestBody = "grant_type=client_credentials";
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                }

                // Get response
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    accessToken = jsonResponse.getString("access_token");
                    int expiresIn = jsonResponse.getInt("expires_in");
                    tokenExpirationTime = System.currentTimeMillis() + (expiresIn * 1000);

                    System.out.println("Successfully retrieved Spotify access token! Expires in: " + expiresIn + " seconds");
                    return true;
                } else if (responseCode == 503 && attempt < MAX_RETRIES) {
                    // Service unavailable - retry after backoff
                    System.err.println("Spotify API temporarily unavailable (503). Retry attempt " + attempt + " of " + MAX_RETRIES + " after " + backoffTime + "ms");
                    try {
                        Thread.sleep(backoffTime);
                        // Exponential backoff with jitter
                        backoffTime = (int) (backoffTime * 1.5 + Math.random() * 300);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        System.err.println("Retry interrupted: " + ie.getMessage());
                        return false;
                    }
                } else {
                    System.err.println("Failed to authenticate with Spotify API. Response code: " + responseCode + 
                                      (attempt < MAX_RETRIES ? ". Will retry." : ". No more retries."));
                    if (attempt >= MAX_RETRIES) {
                        return false;
                    }
                }
            } catch (java.net.SocketTimeoutException e) {
                System.err.println("Connection timeout while authenticating with Spotify API (attempt " + attempt + " of " + MAX_RETRIES + "): " + e.getMessage());
                if (attempt >= MAX_RETRIES) {
                    e.printStackTrace();
                    return false;
                }
                // Backoff before retry
                try {
                    Thread.sleep(backoffTime);
                    backoffTime *= 2; // Double the backoff time for next attempt
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            } catch (Exception e) {
                System.err.println("Error authenticating with Spotify API: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        // If we get here, all retries failed
        return false;
    }

    /**
     * Searches for an artist by name and returns the Spotify artist ID.
     * @param artistName The name of the artist to search for
     * @return The Spotify artist ID or null if not found
     */
    public static String getArtistId(String artistName) {
        // Maximum number of retry attempts
        final int MAX_RETRIES = 2;
        int attempt = 1;
        int backoffTime = 1000;

        while (attempt <= MAX_RETRIES) {
            try {
                if (!authenticate()) {
                    System.err.println("Failed to authenticate with Spotify API. Cannot search for artist.");
                    return null;
                }

                // URL encode the artist name
                String encodedName = java.net.URLEncoder.encode(artistName, StandardCharsets.UTF_8.name());
                URL url = new URL("https://api.spotify.com/v1/search?q=" + encodedName + "&type=artist&limit=1");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);

                // Set connection timeouts
                conn.setConnectTimeout(10000); // 10 seconds connection timeout
                conn.setReadTimeout(15000);    // 15 seconds read timeout

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONObject artists = jsonResponse.getJSONObject("artists");
                    JSONArray items = artists.getJSONArray("items");

                    if (items.length() > 0) {
                        JSONObject artist = items.getJSONObject(0);
                        return artist.getString("id");
                    } else {
                        System.err.println("No artists found matching: " + artistName);
                        return null; // No need to retry if no artists found
                    }
                } else if (responseCode == 401) {
                    // Token might have expired, force re-authentication
                    System.err.println("Authentication token expired. Reauthenticating...");
                    accessToken = null;
                    if (attempt < MAX_RETRIES) {
                        attempt++;
                        continue;
                    } else {
                        System.err.println("Failed to reauthenticate after token expiration.");
                        return null;
                    }
                } else if ((responseCode == 429 || responseCode == 503) && attempt < MAX_RETRIES) {
                    // Rate limit or service unavailable - retry after backoff
                    System.err.println("Spotify API temporarily unavailable or rate limited. Response code: " + responseCode + 
                                      ". Retry attempt " + attempt + " of " + MAX_RETRIES + " after " + backoffTime + "ms");
                    try {
                        Thread.sleep(backoffTime);
                        backoffTime *= 2; // Exponential backoff
                        attempt++;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        System.err.println("Retry interrupted: " + ie.getMessage());
                        return null;
                    }
                } else {
                    System.err.println("Failed to search for artist. Response code: " + responseCode);
                    return null;
                }
            } catch (java.net.SocketTimeoutException e) {
                System.err.println("Connection timeout while searching for artist (attempt " + attempt + " of " + MAX_RETRIES + "): " + e.getMessage());
                if (attempt >= MAX_RETRIES) {
                    return null;
                }
                // Backoff before retry
                try {
                    Thread.sleep(backoffTime);
                    backoffTime *= 2;
                    attempt++;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            } catch (Exception e) {
                System.err.println("Error searching for artist: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * Gets the top track for an artist.
     * @param artistId The Spotify artist ID
     * @return A map containing track information or null if not found
     */
    public static Map<String, String> getTopTrack(String artistId) {
        // Maximum number of retry attempts
        final int MAX_RETRIES = 2;
        int attempt = 1;
        int backoffTime = 1000;

        while (attempt <= MAX_RETRIES) {
            try {
                if (!authenticate()) {
                    System.err.println("Failed to authenticate with Spotify API. Cannot get top tracks.");
                    return null;
                }

                URL url = new URL("https://api.spotify.com/v1/artists/" + artistId + "/top-tracks?market=US");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);

                // Set connection timeouts
                conn.setConnectTimeout(10000); // 10 seconds connection timeout
                conn.setReadTimeout(15000);    // 15 seconds read timeout

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray tracks = jsonResponse.getJSONArray("tracks");

                    if (tracks.length() > 0) {
                        JSONObject track = tracks.getJSONObject(0);
                        String trackId = track.getString("id");
                        String trackName = track.getString("name");
                        String artistName = track.getJSONArray("artists").getJSONObject(0).getString("name");

                        // Get album image
                        String imageUrl = "";
                        if (track.has("album") && track.getJSONObject("album").has("images")) {
                            JSONArray images = track.getJSONObject("album").getJSONArray("images");
                            if (images.length() > 0) {
                                imageUrl = images.getJSONObject(0).getString("url");
                            }
                        }

                        Map<String, String> trackInfo = new HashMap<>();
                        trackInfo.put("id", trackId);
                        trackInfo.put("name", trackName);
                        trackInfo.put("artist", artistName);
                        trackInfo.put("imageUrl", imageUrl);

                        return trackInfo;
                    } else {
                        System.err.println("No top tracks found for artist ID: " + artistId);
                        return null; // No need to retry if no tracks found
                    }
                } else if (responseCode == 401) {
                    // Token might have expired, force re-authentication
                    System.err.println("Authentication token expired. Reauthenticating...");
                    accessToken = null;
                    if (attempt < MAX_RETRIES) {
                        attempt++;
                        continue;
                    } else {
                        System.err.println("Failed to reauthenticate after token expiration.");
                        return null;
                    }
                } else if ((responseCode == 429 || responseCode == 503) && attempt < MAX_RETRIES) {
                    // Rate limit or service unavailable - retry after backoff
                    System.err.println("Spotify API temporarily unavailable or rate limited. Response code: " + responseCode + 
                                      ". Retry attempt " + attempt + " of " + MAX_RETRIES + " after " + backoffTime + "ms");
                    try {
                        Thread.sleep(backoffTime);
                        backoffTime *= 2; // Exponential backoff
                        attempt++;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        System.err.println("Retry interrupted: " + ie.getMessage());
                        return null;
                    }
                } else {
                    System.err.println("Failed to get top tracks. Response code: " + responseCode);
                    return null;
                }
            } catch (java.net.SocketTimeoutException e) {
                System.err.println("Connection timeout while getting top tracks (attempt " + attempt + " of " + MAX_RETRIES + "): " + e.getMessage());
                if (attempt >= MAX_RETRIES) {
                    return null;
                }
                // Backoff before retry
                try {
                    Thread.sleep(backoffTime);
                    backoffTime *= 2;
                    attempt++;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            } catch (Exception e) {
                System.err.println("Error getting top tracks: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * Gets the embed URL for a Spotify track.
     * @param trackId The Spotify track ID
     * @return The embed URL for the track
     */
    public static String getTrackEmbedUrl(String trackId) {
        return "https://open.spotify.com/embed/track/" + trackId;
    }
}
