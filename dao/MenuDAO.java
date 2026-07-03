package Projects.RestaurantManagement.dao;

import Projects.RestaurantManagement.model.MenuItem;
import Projects.RestaurantManagement.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Menu_Items table.
 * Handles menu changes, listing, and item dependencies recipes lookup.
 */
public class MenuDAO {

    /**
     * Lists all items on the active menu.
     * 
     * @return List of MenuItems
     */
    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        String query = "SELECT id, item_name, price, category, is_available FROM Menu_Items ORDER BY category, id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                items.add(new MenuItem(
                        rs.getInt("id"),
                        rs.getString("item_name"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getBoolean("is_available")
                ));
            }
        } catch (SQLException e) {
            System.err.println("🔥 Error fetching menu items: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
        return items;
    }

    /**
     * Finds a menu item by ID.
     * 
     * @param id menu item identifier
     * @return MenuItem object, or null if not found
     */
    public MenuItem findById(int id) {
        String query = "SELECT id, item_name, price, category, is_available FROM Menu_Items WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return new MenuItem(
                        rs.getInt("id"),
                        rs.getString("item_name"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getBoolean("is_available")
                );
            }
        } catch (SQLException e) {
            System.err.println("🔥 Error finding menu item by ID: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
        return null;
    }

    /**
     * Inserts a new menu item into catalog.
     * 
     * @param item MenuItem details
     * @return true if success
     */
    public boolean addMenuItem(MenuItem item) {
        String query = "INSERT INTO Menu_Items (id, item_name, price, category, is_available) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, item.getId());
            pstmt.setString(2, item.getName());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setString(4, item.getCategory());
            pstmt.setBoolean(5, item.isAvailable());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("🔥 Error inserting menu item: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnection.closeResources(pstmt, conn);
        }
    }

    /**
     * Updates an existing menu item details.
     * 
     * @param item MenuItem details
     * @return true if success
     */
    public boolean updateMenuItem(MenuItem item) {
        String query = "UPDATE Menu_Items SET item_name = ?, price = ?, category = ?, is_available = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, item.getName());
            pstmt.setDouble(2, item.getPrice());
            pstmt.setString(3, item.getCategory());
            pstmt.setBoolean(4, item.isAvailable());
            pstmt.setInt(5, item.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("🔥 Error updating menu item: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnection.closeResources(pstmt, conn);
        }
    }

    /**
     * Deletes a menu item from database.
     * 
     * @param id menu item id
     * @return true if success
     */
    public boolean deleteMenuItem(int id) {
        String query = "DELETE FROM Menu_Items WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("🔥 Error deleting menu item: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnection.closeResources(pstmt, conn);
        }
    }

    /**
     * Fetches recipe requirements for a menu item.
     * 
     * @param menuItemId menu item id
     * @return Map of ingredient ID to quantity needed
     */
    public Map<Integer, Double> getRecipeForMenuItem(int menuItemId) {
        Map<Integer, Double> recipe = new HashMap<>();
        String query = "SELECT inventory_id, quantity_needed FROM Menu_Item_Ingredients WHERE menu_item_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, menuItemId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                recipe.put(rs.getInt("inventory_id"), rs.getDouble("quantity_needed"));
            }
        } catch (SQLException e) {
            System.err.println("🔥 Error loading recipe ingredients: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
        return recipe;
    }

    /**
     * Configures/adds an ingredient mapping recipe requirement.
     */
    public void addRecipeRequirement(int menuItemId, int inventoryId, double qtyNeeded) {
        String query = "INSERT INTO Menu_Item_Ingredients (menu_item_id, inventory_id, quantity_needed) " +
                       "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE quantity_needed = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, menuItemId);
            pstmt.setInt(2, inventoryId);
            pstmt.setDouble(3, qtyNeeded);
            pstmt.setDouble(4, qtyNeeded);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("🔥 Error saving recipe mapping: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(pstmt, conn);
        }
    }
}
