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
import java.util.Random;

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
     * Gets a random track from an artist's top tracks.
     * @param artistId The Spotify artist ID
     * @return A map containing track information or null if not found
     */
    public static Map<String, String> getRandomTrack(String artistId) {
        // Try to get a random track from all albums first
        Map<String, String> trackInfo = getRandomTrackFromAllAlbums(artistId);

        // If that fails, fall back to getting a random track from top tracks
        if (trackInfo != null) {
            return trackInfo;
        }

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
                        // Select a random track instead of always the first one
                        Random random = new Random();
                        int randomIndex = random.nextInt(tracks.length());
                        JSONObject track = tracks.getJSONObject(randomIndex);

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

                        Map<String, String> topTrackInfo = new HashMap<>();
                        topTrackInfo.put("id", trackId);
                        topTrackInfo.put("name", trackName);
                        topTrackInfo.put("artist", artistName);
                        topTrackInfo.put("imageUrl", imageUrl);

                        return topTrackInfo;
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
     * Gets a random track from a limited selection of an artist's albums.
     * This method:
     * 1. Fetches up to 3 albums for the artist (limited to improve loading time)
     * 2. For each album, fetches up to 10 tracks (limited to improve loading time)
     * 3. Builds a list of tracks from these albums
     * 4. Randomly selects one track from the list
     * 
     * @param artistId The Spotify artist ID
     * @return A map containing track information or null if not found
     */
    public static Map<String, String> getRandomTrackFromAllAlbums(String artistId) {
        // Maximum number of retry attempts
        final int MAX_RETRIES = 2;
        int attempt = 1;
        int backoffTime = 1000;

        while (attempt <= MAX_RETRIES) {
            try {
                if (!authenticate()) {
                    System.err.println("Failed to authenticate with Spotify API. Cannot get albums.");
                    return null;
                }

                // First, get a limited number of the artist's albums (3 max to improve loading time)
                URL url = new URL("https://api.spotify.com/v1/artists/" + artistId + "/albums?include_groups=album,single&limit=3");

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
                    JSONArray albums = jsonResponse.getJSONArray("items");

                    if (albums.length() > 0) {
                        // Create a list to hold all tracks
                        java.util.List<JSONObject> allTracks = new java.util.ArrayList<>();

                        // For each album, get all tracks (limit to 3 albums max to improve loading time)
                        int albumsProcessed = 0;
                        final int MAX_ALBUMS_TO_PROCESS = 3;

                        for (int i = 0; i < albums.length() && albumsProcessed < MAX_ALBUMS_TO_PROCESS; i++) {
                            JSONObject album = albums.getJSONObject(i);
                            String albumId = album.getString("id");
                            albumsProcessed++;

                            // Get tracks for this album (limit to 10 tracks per album to improve loading time)
                            URL tracksUrl = new URL("https://api.spotify.com/v1/albums/" + albumId + "/tracks?limit=10");
                            HttpURLConnection tracksConn = (HttpURLConnection) tracksUrl.openConnection();
                            tracksConn.setRequestMethod("GET");
                            tracksConn.setRequestProperty("Authorization", "Bearer " + accessToken);

                            responseCode = tracksConn.getResponseCode();
                            if (responseCode == 200) {
                                in = new BufferedReader(new InputStreamReader(tracksConn.getInputStream()));
                                response = new StringBuilder();

                                while ((inputLine = in.readLine()) != null) {
                                    response.append(inputLine);
                                }
                                in.close();

                                // Parse tracks response
                                JSONObject tracksResponse = new JSONObject(response.toString());
                                JSONArray tracks = tracksResponse.getJSONArray("items");

                                // Add all tracks to our list
                                for (int j = 0; j < tracks.length(); j++) {
                                    JSONObject track = tracks.getJSONObject(j);
                                    allTracks.add(track);
                                }
                            }
                        }

                        // If we found any tracks, randomly select one
                        if (!allTracks.isEmpty()) {
                            Random random = new Random();
                            int randomIndex = random.nextInt(allTracks.size());
                            JSONObject selectedTrack = allTracks.get(randomIndex);

                            String trackId = selectedTrack.getString("id");
                            String trackName = selectedTrack.getString("name");
                            String artistName = selectedTrack.getJSONArray("artists").getJSONObject(0).getString("name");

                            // Get full track details to get the album image
                            URL trackUrl = new URL("https://api.spotify.com/v1/tracks/" + trackId);
                            HttpURLConnection trackConn = (HttpURLConnection) trackUrl.openConnection();
                            trackConn.setRequestMethod("GET");
                            trackConn.setRequestProperty("Authorization", "Bearer " + accessToken);

                            responseCode = trackConn.getResponseCode();
                            if (responseCode == 200) {
                                in = new BufferedReader(new InputStreamReader(trackConn.getInputStream()));
                                response = new StringBuilder();

                                while ((inputLine = in.readLine()) != null) {
                                    response.append(inputLine);
                                }
                                in.close();

                                // Parse track response
                                JSONObject trackResponse = new JSONObject(response.toString());

                                // Get album image
                                String imageUrl = "";
                                if (trackResponse.has("album") && trackResponse.getJSONObject("album").has("images")) {
                                    JSONArray images = trackResponse.getJSONObject("album").getJSONArray("images");
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
                            }
                        }
                    }

                    // If we couldn't get any tracks from albums, return null and let the caller fall back to top tracks
                    System.err.println("No tracks found in albums for artist ID: " + artistId);
                    return null;
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
                    System.err.println("Failed to get albums. Response code: " + responseCode);
                    return null;
                }
            } catch (java.net.SocketTimeoutException e) {
                System.err.println("Connection timeout while getting albums (attempt " + attempt + " of " + MAX_RETRIES + "): " + e.getMessage());
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
                System.err.println("Error getting albums: " + e.getMessage());
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

    /**
     * Gets the followers count for an artist.
     * 
     * @param artistId The Spotify artist ID
     * @return The number of followers or -1 if not found
     */
    public static int getArtistFollowers(String artistId) {
        // Maximum number of retry attempts
        final int MAX_RETRIES = 2;
        int attempt = 1;
        int backoffTime = 1000;

        while (attempt <= MAX_RETRIES) {
            try {
                if (!authenticate()) {
                    System.err.println("Failed to authenticate with Spotify API. Cannot get artist details.");
                    return -1;
                }

                URL url = new URL("https://api.spotify.com/v1/artists/" + artistId);

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

                    // Get followers count from the followers object
                    JSONObject followers = jsonResponse.getJSONObject("followers");
                    int followersCount = followers.getInt("total");

                    return followersCount;
                } else if (responseCode == 401) {
                    // Token might have expired, force re-authentication
                    System.err.println("Authentication token expired. Reauthenticating...");
                    accessToken = null;
                    if (attempt < MAX_RETRIES) {
                        attempt++;
                        continue;
                    } else {
                        System.err.println("Failed to reauthenticate after token expiration.");
                        return -1;
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
                        return -1;
                    }
                } else {
                    System.err.println("Failed to get artist details. Response code: " + responseCode);
                    return -1;
                }
            } catch (java.net.SocketTimeoutException e) {
                System.err.println("Connection timeout while getting artist details (attempt " + attempt + " of " + MAX_RETRIES + "): " + e.getMessage());
                if (attempt >= MAX_RETRIES) {
                    return -1;
                }
                // Backoff before retry
                try {
                    Thread.sleep(backoffTime);
                    backoffTime *= 2;
                    attempt++;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return -1;
                }
            } catch (Exception e) {
                System.err.println("Error getting artist details: " + e.getMessage());
                e.printStackTrace();
                return -1;
            }
        }
        return -1;
    }

    /**
     * Gets the latest track for an artist.
     * Note: Spotify API doesn't directly provide the latest track,
     * so this method gets the artist's albums and finds the most recent track.
     * 
     * If the latest release is an album, it fetches a random song from that album.
     * If the latest release is a single, it returns that single.
     * If a single is newer than an album, it will be prioritized.
     * 
     * @param artistId The Spotify artist ID
     * @return A map containing track information or null if not found
     */
    public static Map<String, String> getLatestTrack(String artistId) {
        // Maximum number of retry attempts
        final int MAX_RETRIES = 2;
        int attempt = 1;
        int backoffTime = 1000;

        while (attempt <= MAX_RETRIES) {
            try {
                if (!authenticate()) {
                    System.err.println("Failed to authenticate with Spotify API. Cannot get latest track.");
                    return null;
                }

                // First, get the artist's albums
                URL url = new URL("https://api.spotify.com/v1/artists/" + artistId + "/albums?include_groups=album,single&limit=10");

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
                    JSONArray albums = jsonResponse.getJSONArray("items");

                    if (albums.length() > 0) {
                        // Find the latest single and the latest album
                        JSONObject latestSingle = null;
                        JSONObject latestAlbum = null;

                        // Go through all items to find the latest single and album
                        for (int i = 0; i < albums.length(); i++) {
                            JSONObject item = albums.getJSONObject(i);
                            String type = item.getString("album_type");

                            if ("single".equalsIgnoreCase(type) && latestSingle == null) {
                                latestSingle = item;
                            } else if ("album".equalsIgnoreCase(type) && latestAlbum == null) {
                                latestAlbum = item;
                            }

                            // If we found both, we can stop looking
                            if (latestSingle != null && latestAlbum != null) {
                                break;
                            }
                        }

                        // Determine which one to use based on release date
                        JSONObject selectedRelease;
                        boolean isAlbum;

                        if (latestSingle != null && latestAlbum != null) {
                            // Compare release dates to determine which is newer
                            String singleDate = latestSingle.getString("release_date");
                            String albumDate = latestAlbum.getString("release_date");

                            // If single is newer or equal to album date, prioritize the single
                            if (singleDate.compareTo(albumDate) >= 0) {
                                selectedRelease = latestSingle;
                                isAlbum = false;
                            } else {
                                selectedRelease = latestAlbum;
                                isAlbum = true;
                            }
                        } else if (latestSingle != null) {
                            // Only found a single
                            selectedRelease = latestSingle;
                            isAlbum = false;
                        } else {
                            // Only found an album or neither was found (use the first item)
                            selectedRelease = latestAlbum != null ? latestAlbum : albums.getJSONObject(0);
                            isAlbum = "album".equalsIgnoreCase(selectedRelease.getString("album_type"));
                        }

                        // Get the album ID from the selected release
                        String albumId = selectedRelease.getString("id");

                        // Now get the tracks from this album/single
                        URL tracksUrl;
                        if (isAlbum) {
                            // For albums, get all tracks (up to 20) so we can select a random one
                            tracksUrl = new URL("https://api.spotify.com/v1/albums/" + albumId + "/tracks?limit=20");
                        } else {
                            // For singles, just get the first track
                            tracksUrl = new URL("https://api.spotify.com/v1/albums/" + albumId + "/tracks?limit=1");
                        }

                        HttpURLConnection tracksConn = (HttpURLConnection) tracksUrl.openConnection();
                        tracksConn.setRequestMethod("GET");
                        tracksConn.setRequestProperty("Authorization", "Bearer " + accessToken);

                        responseCode = tracksConn.getResponseCode();
                        if (responseCode == 200) {
                            in = new BufferedReader(new InputStreamReader(tracksConn.getInputStream()));
                            response = new StringBuilder();

                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();

                            // Parse tracks response
                            JSONObject tracksResponse = new JSONObject(response.toString());
                            JSONArray tracks = tracksResponse.getJSONArray("items");

                            if (tracks.length() > 0) {
                                JSONObject track;

                                if (isAlbum && tracks.length() > 1) {
                                    // For albums with multiple tracks, select a random track
                                    Random random = new Random();
                                    int randomIndex = random.nextInt(tracks.length());
                                    track = tracks.getJSONObject(randomIndex);
                                } else {
                                    // For singles or albums with only one track, use the first track
                                    track = tracks.getJSONObject(0);
                                }

                                String trackId = track.getString("id");
                                String trackName = track.getString("name");
                                String artistName = track.getJSONArray("artists").getJSONObject(0).getString("name");

                                // Get full track details to get the album image
                                URL trackUrl = new URL("https://api.spotify.com/v1/tracks/" + trackId);
                                HttpURLConnection trackConn = (HttpURLConnection) trackUrl.openConnection();
                                trackConn.setRequestMethod("GET");
                                trackConn.setRequestProperty("Authorization", "Bearer " + accessToken);

                                responseCode = trackConn.getResponseCode();
                                if (responseCode == 200) {
                                    in = new BufferedReader(new InputStreamReader(trackConn.getInputStream()));
                                    response = new StringBuilder();

                                    while ((inputLine = in.readLine()) != null) {
                                        response.append(inputLine);
                                    }
                                    in.close();

                                    // Parse track response
                                    JSONObject trackResponse = new JSONObject(response.toString());

                                    // Get album image
                                    String imageUrl = "";
                                    if (trackResponse.has("album") && trackResponse.getJSONObject("album").has("images")) {
                                        JSONArray images = trackResponse.getJSONObject("album").getJSONArray("images");
                                        if (images.length() > 0) {
                                            imageUrl = images.getJSONObject(0).getString("url");
                                        }
                                    }

                                    Map<String, String> trackInfo = new HashMap<>();
                                    trackInfo.put("id", trackId);
                                    trackInfo.put("name", trackName);
                                    trackInfo.put("artist", artistName);
                                    trackInfo.put("imageUrl", imageUrl);
                                    trackInfo.put("releaseType", isAlbum ? "album" : "single");

                                    return trackInfo;
                                }
                            }
                        }
                    }

                    // If we couldn't get the latest track, fall back to top track
                    return getTopTrack(artistId);
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
                    System.err.println("Failed to get artist albums. Response code: " + responseCode);
                    return null;
                }
            } catch (java.net.SocketTimeoutException e) {
                System.err.println("Connection timeout while getting latest track (attempt " + attempt + " of " + MAX_RETRIES + "): " + e.getMessage());
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
                System.err.println("Error getting latest track: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    /**
     * Formats a number of listeners into a human-readable string.
     * 
     * @param listeners The number of listeners
     * @return A formatted string (e.g., "1.2M" for 1,200,000)
     */
    public static String formatListenerCount(int listeners) {
        if (listeners < 0) {
            return "Unknown";
        } else if (listeners >= 1000000) {
            return String.format("%.1fM", listeners / 1000000.0);
        } else if (listeners >= 1000) {
            return String.format("%.1fK", listeners / 1000.0);
        } else {
            return String.valueOf(listeners);
        }
    }
}
