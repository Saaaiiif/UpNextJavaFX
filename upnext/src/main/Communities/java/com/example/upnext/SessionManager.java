package com.example.upnext;

/**
 * Singleton class to manage the current session type throughout the application.
 */
public class SessionManager {

    private static SessionManager instance;
    private SessionType sessionType;

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
}
