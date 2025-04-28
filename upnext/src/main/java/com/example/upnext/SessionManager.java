package com.example.upnext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Singleton class to manage the current session type throughout the application.
 */
public class SessionManager {

    private static SessionManager instance;
    private SessionType sessionType;
    private boolean isDarkMode = true; // Default is dark mode

    // List of theme change listeners
    private final List<Consumer<Boolean>> themeChangeListeners = new ArrayList<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private SessionManager() {
        // Default to USER session
        sessionType = SessionType.USER;
    }

    /**
     * Gets the singleton instance of the SessionManager.
     * 
     * @return the singleton instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Gets the current session type.
     * 
     * @return the current session type
     */
    public SessionType getSessionType() {
        return sessionType;
    }

    /**
     * Sets the current session type.
     * 
     * @param sessionType the session type to set
     */
    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    /**
     * Checks if the current session is an admin session.
     * 
     * @return true if the current session is an admin session, false otherwise
     */
    public boolean isAdminSession() {
        return sessionType == SessionType.ADMIN;
    }

    /**
     * Checks if the current session is a user session.
     * 
     * @return true if the current session is a user session, false otherwise
     */
    public boolean isUserSession() {
        return sessionType == SessionType.USER;
    }

    /**
     * Checks if the current session is an artist session.
     * 
     * @return true if the current session is an artist session, false otherwise
     */
    public boolean isArtistSession() {
        return sessionType == SessionType.ARTIST;
    }

    /**
     * Gets the current theme mode.
     * 
     * @return true if dark mode, false if light mode
     */
    public boolean isDarkMode() {
        return isDarkMode;
    }

    /**
     * Sets the current theme mode.
     * 
     * @param isDarkMode true for dark mode, false for light mode
     */
    public void setDarkMode(boolean isDarkMode) {
        if (this.isDarkMode != isDarkMode) {
            this.isDarkMode = isDarkMode;
            // Notify all listeners of the theme change
            notifyThemeChangeListeners();
        }
    }

    /**
     * Adds a listener to be notified when the theme changes.
     * 
     * @param listener the listener to add
     */
    public void addThemeChangeListener(Consumer<Boolean> listener) {
        themeChangeListeners.add(listener);
    }

    /**
     * Removes a theme change listener.
     * 
     * @param listener the listener to remove
     */
    public void removeThemeChangeListener(Consumer<Boolean> listener) {
        themeChangeListeners.remove(listener);
    }

    /**
     * Notifies all theme change listeners of the current theme.
     */
    private void notifyThemeChangeListeners() {
        for (Consumer<Boolean> listener : themeChangeListeners) {
            listener.accept(isDarkMode);
        }
    }
}
