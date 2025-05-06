package controllers.communities;

/**
 * Represents a comment in a community.
 */
public class Comment {
    private int id;
    private int communityId;
    private String comment;
    private String username; // Optional: to display who posted the comment
    private int userId; // ID of the user who posted the comment

    /**
     * Constructor for a new comment.
     * 
     * @param id The comment ID
     * @param communityId The ID of the community the comment belongs to
     * @param comment The comment text
     */
    public Comment(int id, int communityId, String comment) {
        this.id = id;
        this.communityId = communityId;
        this.comment = comment;
        this.username = "Anonymous"; // Default username
        this.userId = -1; // Default user ID (no user)
    }

    /**
     * Constructor for a new comment with username.
     * 
     * @param id The comment ID
     * @param communityId The ID of the community the comment belongs to
     * @param comment The comment text
     * @param username The username of the commenter
     */
    public Comment(int id, int communityId, String comment, String username) {
        this.id = id;
        this.communityId = communityId;
        this.comment = comment;
        this.username = username;
        this.userId = -1; // Default user ID (no user)
    }

    /**
     * Constructor for a new comment with username and user ID.
     * 
     * @param id The comment ID
     * @param communityId The ID of the community the comment belongs to
     * @param comment The comment text
     * @param username The username of the commenter
     * @param userId The ID of the user who posted the comment
     */
    public Comment(int id, int communityId, String comment, String username, int userId) {
        this.id = id;
        this.communityId = communityId;
        this.comment = comment;
        this.username = username;
        this.userId = userId;
    }

    /**
     * Gets the comment ID.
     * 
     * @return The comment ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the community ID.
     * 
     * @return The community ID
     */
    public int getCommunityId() {
        return communityId;
    }

    /**
     * Gets the comment text.
     * 
     * @return The comment text
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment text.
     * 
     * @param comment The new comment text
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Gets the username of the commenter.
     * 
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the commenter.
     * 
     * @param username The new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the user ID of the commenter.
     * 
     * @return The user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the user ID of the commenter.
     * 
     * @param userId The new user ID
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }
}
