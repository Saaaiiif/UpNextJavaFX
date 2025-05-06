package controllers.communities;

/**
 * Enum representing the different types of sessions in the application.
 */
public enum SessionType {
    /**
     * Regular user session with limited privileges.
     */
    USER,

    /**
     * Administrator session with full privileges.
     */
    ADMIN,

    /**
     * Artist session with specific privileges for artists.
     */
    ARTIST
}
