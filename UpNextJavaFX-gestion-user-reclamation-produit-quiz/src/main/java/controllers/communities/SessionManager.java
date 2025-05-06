package controllers.communities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import edu.up_next.entities.User;

/**
 * Singleton class to manage session state across the application.
 */
public class SessionManager {
    private static SessionManager instance;
    private boolean isDarkMode = true; // Default is dark mode
    private SessionType sessionType = SessionType.USER; // Default is USER
    private List<Consumer<Boolean>> themeChangeListeners = new ArrayList<>();
    private User currentUser; // Current logged-in user

    // Private constructor to prevent instantiation
    private SessionManager() {
    }

    /**
     * Gets the singleton instance of SessionManager.
     * @return The SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Gets the current theme mode.
     * @return true if dark mode, false if light mode
     */
    public boolean isDarkMode() {
        return isDarkMode;
    }

    /**
     * Sets the theme mode.
     * @param isDarkMode true for dark mode, false for light mode
     */
    public void setDarkMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        // Notify all listeners of the theme change
        notifyThemeChangeListeners();
    }

    /**
     * Adds a listener to be notified when the theme changes.
     * @param listener The listener to add
     */
    public void addThemeChangeListener(Consumer<Boolean> listener) {
        themeChangeListeners.add(listener);
    }

    /**
     * Removes a listener from the list of theme change listeners.
     * @param listener The listener to remove
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

    /**
     * Gets the current session type.
     * @return The current session type
     */
    public SessionType getSessionType() {
        return sessionType;
    }

    /**
     * Sets the session type.
     * @param sessionType The session type to set
     */
    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    /**
     * Checks if the current session is an admin session.
     * @return true if the current session is an admin session, false otherwise
     */
    public boolean isAdminSession() {
        return sessionType == SessionType.ADMIN;
    }

    /**
     * Gets the current user.
     * @return The current user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the current user.
     * @param user The user to set as the current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}
