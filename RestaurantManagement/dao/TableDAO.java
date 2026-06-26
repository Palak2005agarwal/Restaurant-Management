package Projects.RestaurantManagement.dao;

import Projects.RestaurantManagement.model.Table;
import Projects.RestaurantManagement.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Tables table.
 * Manages physical dining table seat capacities and occupancy flags.
 */
public class TableDAO {

    /**
     * Retrieves all table layouts in the system.
     * 
     * @return List of Table objects
     */
    public List<Table> getAllTables() {
        List<Table> tables = new ArrayList<>();
        String query = "SELECT id, table_number, capacity, status FROM Tables ORDER BY table_number";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                tables.add(new Table(
                        rs.getInt("id"),
                        rs.getInt("table_number"),
                        rs.getInt("capacity"),
                        Table.Status.valueOf(rs.getString("status").toUpperCase())
                ));
            }
        } catch (SQLException e) {
            System.err.println("🔥 Error fetching tables list: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
        return tables;
    }

    /**
     * Finds a table matching its table number.
     * 
     * @param tableNumber unique table number
     * @return Table object, or null if not found
     */
    public Table findByNumber(int tableNumber) {
        String query = "SELECT id, table_number, capacity, status FROM Tables WHERE table_number = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, tableNumber);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Table(
                        rs.getInt("id"),
                        rs.getInt("table_number"),
                        rs.getInt("capacity"),
                        Table.Status.valueOf(rs.getString("status").toUpperCase())
                );
            }
        } catch (SQLException e) {
            System.err.println("🔥 Error finding table by number: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
        return null;
    }

    /**
     * Updates the status of a specific table.
     * 
     * @param tableId table primary key
     * @param status table occupancy status
     */
    public void updateTableStatus(int tableId, Table.Status status) {
        String query = "UPDATE Tables SET status = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, status.name());
            pstmt.setInt(2, tableId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("🔥 Error updating table status: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(pstmt, conn);
        }
    }
}
