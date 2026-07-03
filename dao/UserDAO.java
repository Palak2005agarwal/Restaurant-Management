package Projects.RestaurantManagement.dao;

import Projects.RestaurantManagement.model.User;
import Projects.RestaurantManagement.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for Users table.
 * Handles database operations for authenticating staff.
 */
public class UserDAO {

    /**
     * Authenticates a user by username and password.
     * 
     * @param username staff username
     * @param password staff password
     * @return User object if credentials are correct, null otherwise
     */
    public User authenticate(String username, String password) {
        String query = "SELECT id, username, password_hash, role FROM Users WHERE username = ? AND password_hash = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String uname = rs.getString("username");
                String phash = rs.getString("password_hash");
                String roleStr = rs.getString("role");
                User.Role role = User.Role.valueOf(roleStr.toUpperCase());
                
                return new User(id, uname, phash, role);
            }
        } catch (SQLException e) {
            System.err.println("🔥 Error authenticating user: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
        return null;
    }
}
