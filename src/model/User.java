package model;

/**
 * User Model Entity
 * -----------------
 * Represents an authenticated or registered user in RailBook.
 * Stores user profile information and salted password hash.
 * 
 * Note: Academic demonstration model for college microproject.
 */
public class User {

    private String username;
    private String fullName;
    private String email;
    private String passwordHash;
    private String salt;
    private String createdAt;

    /**
     * Primary constructor for a registered user.
     *
     * @param username     Unique username identifier
     * @param fullName     Full legal or display name of the passenger
     * @param email        Contact email address
     * @param passwordHash Salted SHA-256 hash
     * @param salt         Cryptographic salt used during hashing
     * @param createdAt    Creation timestamp string
     */
    public User(String username, String fullName, String email, String passwordHash, String salt, String createdAt) {
        validateUsername(username);
        validateFullName(fullName);
        validateEmail(email);

        this.username = username.trim().toLowerCase();
        this.fullName = fullName.trim();
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.createdAt = (createdAt == null || createdAt.trim().isEmpty()) ? "Unknown" : createdAt.trim();
    }

    private void validateUsername(String uname) {
        if (uname == null || uname.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long.");
        }
        if (!uname.trim().matches("^[a-zA-Z0-9_.-]+$")) {
            throw new IllegalArgumentException("Username can only contain letters, numbers, dots, hyphens, and underscores.");
        }
    }

    private void validateFullName(String name) {
        if (name == null || name.trim().length() < 2) {
            throw new IllegalArgumentException("Full name must have at least 2 characters.");
        }
    }

    private void validateEmail(String em) {
        if (em == null || !em.trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Please enter a valid email address (e.g. user@example.com).");
        }
    }

    // --- Getters & Setters ---

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        validateFullName(fullName);
        this.fullName = fullName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        validateEmail(email);
        this.email = email.trim().toLowerCase();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("User[%s | %s | %s]", username, fullName, email);
    }
}
