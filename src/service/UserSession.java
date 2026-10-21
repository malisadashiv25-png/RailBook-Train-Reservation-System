package service;

import model.User;

/**
 * UserSession
 * -----------
 * Manages the current authenticated user session within the desktop application.
 * Supports logged-in user profile as well as Guest Demonstration Mode.
 */
public class UserSession {

    private static UserSession instance;

    private User currentUser;
    private boolean isGuest;

    private UserSession() {
        this.currentUser = null;
        this.isGuest = false;
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Logs in a verified registered user.
     */
    public synchronized void login(User user) {
        this.currentUser = user;
        this.isGuest = false;
    }

    /**
     * Activates guest mode for demonstration purposes.
     */
    public synchronized void loginAsGuest() {
        this.currentUser = null;
        this.isGuest = true;
    }

    /**
     * Clears session upon logout.
     */
    public synchronized void logout() {
        this.currentUser = null;
        this.isGuest = false;
    }

    public synchronized boolean isAuthenticated() {
        return currentUser != null || isGuest;
    }

    public synchronized boolean isGuest() {
        return isGuest;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns passenger name to display in the UI or pre-fill booking forms.
     */
    public synchronized String getDisplayName() {
        if (currentUser != null) {
            return currentUser.getFullName();
        } else if (isGuest) {
            return "Guest Passenger";
        }
        return "Not Logged In";
    }

    public synchronized String getUsername() {
        if (currentUser != null) {
            return currentUser.getUsername();
        } else if (isGuest) {
            return "guest";
        }
        return "";
    }
}
