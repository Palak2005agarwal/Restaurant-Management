package Projects.RestaurantManagement.model;

/**
 * Represents a user in the restaurant management system.
 * Governs authentication details and role-based dashboard routing.
 */
public class User {
    
    public enum Role {
        ADMIN,
        WAITER,
        CHEF
    }

    private final int id;
    private final String username;
    private final String passwordHash;
    private final Role role;

    public User(int id, String username, String passwordHash, Role role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty.");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null.");
        }
        this.id = id;
        this.username = username.trim();
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                '}';
    }
}
