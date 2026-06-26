package Projects.RestaurantManagement.dao;

import Projects.RestaurantManagement.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Inventory table.
 * Controls raw ingredient stock validations and updates.
 */
public class InventoryDAO {

    /**
     * Data Transfer Object for raw stock items.
     */
    public static class Ingredient {
        private final int id;
        private final String name;
        private final double quantity;
        private final String unit;

        public Ingredient(int id, String name, double quantity, String unit) {
            this.id = id;
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public double getQuantity() { return quantity; }
        public String getUnit() { return unit; }

        @Override
        public String toString() {
            return String.format("ID: %-3d | %-20s | Qty: %8.2f %s", id, name, quantity, unit);
        }
    }

    /**
     * Lists all stock ingredients.
     * 
     * @return List of Ingredient DTOs
     */
    public List<Ingredient> getAllInventory() {
        List<Ingredient> list = new ArrayList<>();
        String query = "SELECT id, ingredient_name, quantity, unit FROM Inventory ORDER BY id";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new Ingredient(
                        rs.getInt("id"),
                        rs.getString("ingredient_name"),
                        rs.getDouble("quantity"),
                        rs.getString("unit")
                ));
            }
        } catch (SQLException e) {
            System.err.println("🔥 Error fetching inventory: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
        return list;
    }

    /**
     * Checks if there are enough ingredients in stock to fulfill a menu item quantity.
     * 
     * @param menuItemId ID of the menu item ordered
     * @param orderQty quantity ordered
     * @return true if stock is sufficient, false otherwise
     */
    public boolean hasSufficientStock(int menuItemId, int orderQty) {
        String query = "SELECT i.ingredient_name, i.quantity AS current_stock, r.quantity_needed " +
                       "FROM Menu_Item_Ingredients r " +
                       "JOIN Inventory i ON r.inventory_id = i.id " +
                       "WHERE r.menu_item_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, menuItemId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("ingredient_name");
                double currentStock = rs.getDouble("current_stock");
                double quantityNeeded = rs.getDouble("quantity_needed");
                
                if (currentStock < (quantityNeeded * orderQty)) {
                    System.out.printf("⚠️ Insufficient Stock: %s (Required: %.2f, Available: %.2f)\n", 
                                      name, (quantityNeeded * orderQty), currentStock);
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("🔥 Error validating stock levels: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
    }

    /**
     * Deducts the ingredient stock required for an order.
     * 
     * @param conn database connection (to support transactions)
     * @param menuItemId ID of the menu item ordered
     * @param orderQty quantity ordered
     * @throws SQLException on database error
     */
    public void deductStockForOrderItem(Connection conn, int menuItemId, int orderQty) throws SQLException {
        String query = "UPDATE Inventory i " +
                       "INNER JOIN Menu_Item_Ingredients r ON i.id = r.inventory_id " +
                       "SET i.quantity = i.quantity - (r.quantity_needed * ?) " +
                       "WHERE r.menu_item_id = ?";
        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, orderQty);
            pstmt.setInt(2, menuItemId);
            pstmt.executeUpdate();
        } finally {
            DatabaseConnection.closeResources(pstmt);
        }
    }

    /**
     * Replenishes an ingredient's stock level.
     * 
     * @param ingredientId ingredient id
     * @param addedQty quantity to add
     * @return true if success
     */
    public boolean replenishStock(int ingredientId, double addedQty) {
        if (addedQty <= 0) {
            throw new IllegalArgumentException("Replenishment quantity must be positive.");
        }
        String query = "UPDATE Inventory SET quantity = quantity + ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setDouble(1, addedQty);
            pstmt.setInt(2, ingredientId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("🔥 Error replenishing ingredient stock: " + e.getMessage());
            return false;
        } finally {
            DatabaseConnection.closeResources(pstmt, conn);
        }
    }
}
