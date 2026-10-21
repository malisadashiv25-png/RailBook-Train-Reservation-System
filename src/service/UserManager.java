package service;

import model.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * UserManager (Authentication & User Persistence)
 * ------------------------------------------------
 * Demonstrates:
 * - Practicum 10: Dynamic ArrayList<User> for in-memory user registry.
 * - Practicum 12: Safe file persistence in 'users.txt' using BufferedReader and BufferedWriter.
 *
 * Security:
 * - Uses java.security.SecureRandom for cryptographic 16-byte salt generation.
 * - Uses SHA-256 via java.security.MessageDigest for salted password hashing.
 * - Never stores plain-text passwords in memory or on disk.
 *
 * Note: Academic demonstration for college microproject; not a production identity provider.
 */
public class UserManager {

    public static final String DEFAULT_USERS_FILE = "users.txt";

    // Practicum 10: In-memory dynamic list of registered users
    private final ArrayList<User> users;
    private final String filePath;

    public UserManager() {
        this(DEFAULT_USERS_FILE);
    }

    public UserManager(String filePath) {
        this.filePath = filePath;
        this.users = new ArrayList<>();

        // Load existing users from disk on startup
        try {
            loadUsers();
        } catch (IOException e) {
            System.err.println("Notice: Could not load initial users file: " + e.getMessage());
        }

        // Pre-seed default demonstration user if user list is empty
        if (users.isEmpty()) {
            try {
                registerUser("demo", "College Demo User", "demo@railbook.edu", "demo123");
            } catch (Exception ex) {
                System.err.println("Notice: Could not seed demo user: " + ex.getMessage());
            }
        }
    }

    /**
     * Registers a new user, hashes the password with a cryptographic salt,
     * appends to the ArrayList, and saves to users.txt.
     *
     * @param username Unique username
     * @param fullName Full legal or display name
     * @param email    Unique email address
     * @param password Plain-text password (will be salted and hashed immediately)
     * @return Created User object
     * @throws IllegalArgumentException if validation fails or username/email already exists
     * @throws IOException              if file persistence fails
     */
    public synchronized User registerUser(String username, String fullName, String email, String password) throws IOException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be empty.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters.");
        }

        String unameClean = username.trim().toLowerCase();
        String emailClean = email.trim().toLowerCase();

        // Check uniqueness in in-memory ArrayList
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(unameClean)) {
                throw new IllegalArgumentException("Username '" + username + "' is already taken. Please choose another.");
            }
            if (u.getEmail().equalsIgnoreCase(emailClean)) {
                throw new IllegalArgumentException("Email '" + email + "' is already registered. Please sign in instead.");
            }
        }

        // Generate 16-byte random salt and salted SHA-256 hash
        String salt = generateSalt();
        String passwordHash = hashPassword(password, salt);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        User newUser = new User(unameClean, fullName, emailClean, passwordHash, salt, timestamp);

        // Practicum 10: Add to dynamic ArrayList
        users.add(newUser);

        // Practicum 12: Persist changes to disk
        saveUsers();

        return newUser;
    }

    /**
     * Authenticates a user with username/email and plain-text password.
     *
     * @param usernameOrEmail Username or registered email
     * @param password        Plain-text password to verify
     * @return Authenticated User object if credentials are valid; null otherwise
     */
    public synchronized User authenticate(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty() || password == null) {
            return null;
        }

        String query = usernameOrEmail.trim().toLowerCase();
        // Practicum 10: Traversing ArrayList to locate user
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(query) || u.getEmail().equalsIgnoreCase(query)) {
                // Verify password hash against stored salt
                String expectedHash = hashPassword(password, u.getSalt());
                if (expectedHash.equals(u.getPasswordHash())) {
                    return u;
                }
                return null; // Username found but password incorrect
            }
        }
        return null;
    }

    /**
     * Finds a user by exact username.
     */
    public synchronized User getUserByUsername(String username) {
        if (username == null) return null;
        String target = username.trim().toLowerCase();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(target)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Returns defensive copy of all registered users.
     */
    public synchronized ArrayList<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public synchronized int getUserCount() {
        return users.size();
    }

    // =========================================================================
    // Practicum 12: File Handling (users.txt persistence)
    // =========================================================================

    /**
     * Reads users from text file using BufferedReader inside try-with-resources.
     */
    public synchronized void loadUsers() throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        ArrayList<User> loaded = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    List<String> fields = splitFields(line);
                    if (fields.size() >= 6) {
                        String uname = fields.get(0);
                        String name = fields.get(1);
                        String email = fields.get(2);
                        String hash = fields.get(3);
                        String salt = fields.get(4);
                        String created = fields.get(5);

                        loaded.add(new User(uname, name, email, hash, salt, created));
                    }
                } catch (Exception ex) {
                    System.err.println("Warning: Skipping malformed user on line " + lineNum + ": " + ex.getMessage());
                }
            }
        }

        users.clear();
        users.addAll(loaded);
    }

    /**
     * Writes users to temporary file using BufferedWriter inside try-with-resources
     * and atomically replaces users.txt to prevent data loss.
     */
    public synchronized void saveUsers() throws IOException {
        File targetFile = new File(filePath);
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        File tempFile = new File(targetFile.getAbsolutePath() + ".tmp");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile, StandardCharsets.UTF_8))) {
            writer.write("# RailBook User Accounts Database");
            writer.newLine();
            writer.write("# Format: USERNAME|FULL_NAME|EMAIL|PASSWORD_HASH|SALT|CREATED_AT");
            writer.newLine();

            for (User u : users) {
                String line = escape(u.getUsername()) + "|"
                        + escape(u.getFullName()) + "|"
                        + escape(u.getEmail()) + "|"
                        + escape(u.getPasswordHash()) + "|"
                        + escape(u.getSalt()) + "|"
                        + escape(u.getCreatedAt());
                writer.write(line);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            if (tempFile.exists()) tempFile.delete();
            throw e;
        }

        try {
            Files.move(tempFile.toPath(), targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // =========================================================================
    // Cryptographic Helpers (Salt & Salted SHA-256)
    // =========================================================================

    /**
     * Generates a 16-byte random salt encoded as hexadecimal.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        return bytesToHex(saltBytes);
    }

    /**
     * Computes salted SHA-256 hash of the password.
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String input = salt + password;
            byte[] hashed = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 cryptographic algorithm not available: " + e.getMessage(), e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String escape(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\r", "")
                .replace("\n", " ");
    }

    private static List<String> splitFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (escaped) {
                current.append(c);
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '|') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
