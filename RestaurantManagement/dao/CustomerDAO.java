package Projects.RestaurantManagement.dao;

import Projects.RestaurantManagement.model.Customer;
import Projects.RestaurantManagement.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object for Customers table.
 * Handles customer registrations, loyalty upgrades, and details lookup.
 */
public class CustomerDAO {

    /**
     * Finds a customer by phone number.
     * 
     * @param phone customer mobile number
     * @return Customer object, or null if not found
     */
    public Customer findByPhone(String phone) {
        String query = "SELECT id, name, phone, loyalty_points FROM Customers WHERE phone = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, phone);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getInt("loyalty_points")
                );
            }
        } catch (SQLException e) {
            System.err.println("🔥 Error finding customer by phone: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
        return null;
    }

    /**
     * Finds a customer by primary key ID.
     * 
     * @param id customer identifier
     * @return Customer object, or null if not found
     */
    public Customer findById(int id) {
        String query = "SELECT id, name, phone, loyalty_points FROM Customers WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getInt("loyalty_points")
                );
            }
        } catch (SQLException e) {
            System.err.println("🔥 Error finding customer by ID: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
        return null;
    }

    /**
     * Creates a new customer registration in the system.
     * 
     * @param name customer name
     * @param phone customer phone number
     * @return newly created Customer object with generated ID, or null on failure
     */
    public Customer createCustomer(String name, String phone) {
        String query = "INSERT INTO Customers (name, phone, loyalty_points) VALUES (?, ?, 0)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    return new Customer(generatedId, name, phone, 0);
                }
            }
        } catch (SQLException e) {
            System.err.println("🔥 Error registering customer: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
        return null;
    }

    /**
     * Updates customer loyalty points.
     * 
     * @param id customer identifier
     * @param points total loyalty points to set
     */
    public void updateLoyaltyPoints(int id, int points) {
        String query = "UPDATE Customers SET loyalty_points = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, points);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("🔥 Error updating customer loyalty points: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(pstmt, conn);
        }
    }
}
