package Projects.RestaurantManagement.dao;

import Projects.RestaurantManagement.model.Order;
import Projects.RestaurantManagement.model.OrderItem;
import Projects.RestaurantManagement.model.MenuItem;
import Projects.RestaurantManagement.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Orders and Order_Items tables.
 * Handles database operations for placing orders in transactional boundaries,
 * processing payments, updating table states, and pulling aggregated sales metrics.
 */
public class OrderDAO {

    private final InventoryDAO inventoryDAO = new InventoryDAO();

    /**
     * Inserts a new Order and all its associated items within a single database transaction.
     * Deducts stock for each item and sets the table status to OCCUPIED.
     * 
     * @param order Order object to insert
     * @return the saved Order containing database generated ID, or null on failure
     */
    public Order placeOrder(Order order) {
        String insertOrderSQL = "INSERT INTO Orders (customer_id, table_id, served_by_user_id, order_status, total_amount, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        String insertOrderItemSQL = "INSERT INTO Order_Items (order_id, menu_item_id, quantity, special_instructions) VALUES (?, ?, ?, ?)";
        String updateTableSQL = "UPDATE Tables SET status = 'OCCUPIED' WHERE id = ?";

        Connection conn = null;
        PreparedStatement psOrder = null;
        PreparedStatement psItem = null;
        PreparedStatement psTable = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Insert parent Order
            psOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
            if (order.getCustomerId() != null) {
                psOrder.setInt(1, order.getCustomerId());
            } else {
                psOrder.setNull(1, Types.INTEGER);
            }
            psOrder.setInt(2, order.getTableId());
            psOrder.setInt(3, order.getServedByUserId());
            psOrder.setString(4, order.getStatus().name());
            psOrder.setDouble(5, order.calculateTotal());
            psOrder.setTimestamp(6, order.getTimestamp());
            psOrder.executeUpdate();

            generatedKeys = psOrder.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("Placing order failed, no ID obtained.");
            }
            int orderId = generatedKeys.getInt(1);
            order.setId(orderId);

            // 2. Insert child OrderItems and deduct stock
            psItem = conn.prepareStatement(insertOrderItemSQL);
            for (OrderItem item : order.getOrderedItems()) {
                item.setOrderId(orderId);

                // Verify stock availability (double check)
                if (!inventoryDAO.hasSufficientStock(item.getItem().getId(), item.getQuantity())) {
                    throw new SQLException("Insufficient stock for item: " + item.getItem().getName());
                }

                // Deduct inventory stock
                inventoryDAO.deductStockForOrderItem(conn, item.getItem().getId(), item.getQuantity());

                // Save item row
                psItem.setInt(1, orderId);
                psItem.setInt(2, item.getItem().getId());
                psItem.setInt(3, item.getQuantity());
                psItem.setString(4, item.getSpecialInstructions());
                psItem.executeUpdate();
            }

            // 3. Mark Table as Occupied
            psTable = conn.prepareStatement(updateTableSQL);
            psTable.setInt(1, order.getTableId());
            psTable.executeUpdate();

            conn.commit(); // Commit Transaction
            return order;

        } catch (SQLException e) {
            System.err.println("🔥 Order transaction failed. Rolling back changes. Error: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("🔥 Rollback failed: " + ex.getMessage());
                }
            }
        } finally {
            DatabaseConnection.closeResources(generatedKeys, psOrder, psItem, psTable, conn);
        }
        return null;
    }

    /**
     * Appends a new item to an existing active order, deducting stock from database.
     * 
     * @param orderId ID of the parent order
     * @param item OrderItem details
     * @return true if success
     */
    public boolean appendOrderItem(int orderId, OrderItem item) {
        String insertItemSQL = "INSERT INTO Order_Items (order_id, menu_item_id, quantity, special_instructions) VALUES (?, ?, ?, ?)";
        String updateOrderTotalSQL = "UPDATE Orders SET total_amount = total_amount + ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement psItem = null;
        PreparedStatement psOrder = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Transaction

            // Deduct stock
            if (!inventoryDAO.hasSufficientStock(item.getItem().getId(), item.getQuantity())) {
                throw new SQLException("Insufficient stock for " + item.getItem().getName());
            }
            inventoryDAO.deductStockForOrderItem(conn, item.getItem().getId(), item.getQuantity());

            // Save item
            psItem = conn.prepareStatement(insertItemSQL);
            psItem.setInt(1, orderId);
            psItem.setInt(2, item.getItem().getId());
            psItem.setInt(3, item.getQuantity());
            psItem.setString(4, item.getSpecialInstructions());
            psItem.executeUpdate();

            // Update order total
            psOrder = conn.prepareStatement(updateOrderTotalSQL);
            psOrder.setDouble(1, item.calculateSubTotal());
            psOrder.setInt(2, orderId);
            psOrder.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("🔥 Error appending order item: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            DatabaseConnection.closeResources(psItem, psOrder, conn);
        }
    }

    /**
     * Finds the active order for a given physical table number.
     * Active orders are in status PENDING, COOKING, or READY.
     * 
     * @param tableNumber table number
     * @return Order object with loaded items list, or null if no active session
     */
    public Order findActiveOrderByTable(int tableNumber) {
        String query = "SELECT o.id, o.customer_id, o.table_id, o.served_by_user_id, o.order_status, o.total_amount, o.timestamp, t.table_number " +
                       "FROM Orders o " +
                       "JOIN Tables t ON o.table_id = t.id " +
                       "WHERE t.table_number = ? AND o.order_status IN ('PENDING', 'COOKING', 'READY')";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Order order = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, tableNumber);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                Integer custId = rs.getInt("customer_id");
                if (rs.wasNull()) custId = null;
                int tableId = rs.getInt("table_id");
                int tNum = rs.getInt("table_number");
                int waiterId = rs.getInt("served_by_user_id");
                Order.Status status = Order.Status.valueOf(rs.getString("order_status").toUpperCase());
                double total = rs.getDouble("total_amount");
                Timestamp ts = rs.getTimestamp("timestamp");

                order = new Order(id, custId, tableId, tNum, waiterId, status, total, ts);
                loadOrderItems(conn, order);
            }
        } catch (SQLException e) {
            System.err.println("🔥 Error searching active order: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
        return order;
    }

    /**
     * Lists all active orders (status: PENDING, COOKING, READY) sorted by timestamp.
     * 
     * @return List of active Orders
     */
    public List<Order> getActiveOrders() {
        List<Order> list = new ArrayList<>();
        String query = "SELECT o.id, o.customer_id, o.table_id, o.served_by_user_id, o.order_status, o.total_amount, o.timestamp, t.table_number " +
                       "FROM Orders o " +
                       "JOIN Tables t ON o.table_id = t.id " +
                       "WHERE o.order_status IN ('PENDING', 'COOKING', 'READY') " +
                       "ORDER BY o.timestamp ASC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                Integer custId = rs.getInt("customer_id");
                if (rs.wasNull()) custId = null;
                int tableId = rs.getInt("table_id");
                int tNum = rs.getInt("table_number");
                int waiterId = rs.getInt("served_by_user_id");
                Order.Status status = Order.Status.valueOf(rs.getString("order_status").toUpperCase());
                double total = rs.getDouble("total_amount");
                Timestamp ts = rs.getTimestamp("timestamp");

                Order order = new Order(id, custId, tableId, tNum, waiterId, status, total, ts);
                loadOrderItems(conn, order);
                list.add(order);
            }
        } catch (SQLException e) {
            System.err.println("🔥 Error listing active orders: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
        return list;
    }

    /**
     * Helper to load order items list into an Order object using an existing connection.
     */
    private void loadOrderItems(Connection conn, Order order) throws SQLException {
        String query = "SELECT oi.id, oi.menu_item_id, oi.quantity, oi.special_instructions, " +
                       "m.item_name, m.price, m.category, m.is_available " +
                       "FROM Order_Items oi " +
                       "JOIN Menu_Items m ON oi.menu_item_id = m.id " +
                       "WHERE oi.order_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, order.getId());
            rs = pstmt.executeQuery();

            order.getOrderedItems().clear(); // Reset list
            while (rs.next()) {
                MenuItem item = new MenuItem(
                        rs.getInt("menu_item_id"),
                        rs.getString("item_name"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getBoolean("is_available")
                );
                OrderItem orderItem = new OrderItem(
                        rs.getInt("id"),
                        order.getId(),
                        item,
                        rs.getInt("quantity"),
                        rs.getString("special_instructions")
                );
                order.getOrderedItems().add(orderItem);
            }
            order.recalculateTotal();
        } finally {
            DatabaseConnection.closeResources(rs, pstmt);
        }
    }

    /**
     * Updates an order status.
     * 
     * @param orderId order id
     * @param status new Status
     * @return true if success
     */
    public boolean updateOrderStatus(int orderId, Order.Status status) {
        String updateOrderSQL = "UPDATE Orders SET order_status = ? WHERE id = ?";
        String updateTableSQL = "UPDATE Tables SET status = 'AVAILABLE' WHERE id = (SELECT table_id FROM Orders WHERE id = ?)";
        Connection conn = null;
        PreparedStatement psOrder = null;
        PreparedStatement psTable = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            psOrder = conn.prepareStatement(updateOrderSQL);
            psOrder.setString(1, status.name());
            psOrder.setInt(2, orderId);
            int affected = psOrder.executeUpdate();

            if (affected > 0 && status == Order.Status.CANCELLED) {
                psTable = conn.prepareStatement(updateTableSQL);
                psTable.setInt(1, orderId);
                psTable.executeUpdate();
            }

            conn.commit();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("🔥 Error updating order status: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            DatabaseConnection.closeResources(psOrder, psTable, conn);
        }
    }

    /**
     * Processes payment settlement for an order.
     * Marks the order status as PAID, records final total, awards/deducts customer loyalty points,
     * and releases the table status back to AVAILABLE.
     */
    public boolean settleBill(int orderId, int tableId, double grandTotal, Integer customerId, 
                              int loyaltyPointsEarned, int loyaltyPointsDeducted) {
        String updateOrderSQL = "UPDATE Orders SET order_status = 'PAID', total_amount = ? WHERE id = ?";
        String updateTableSQL = "UPDATE Tables SET status = 'AVAILABLE' WHERE id = ?";
        String updatePointsSQL = "UPDATE Customers SET loyalty_points = loyalty_points + ? - ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement psOrder = null;
        PreparedStatement psTable = null;
        PreparedStatement psPoints = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Transaction

            // 1. Settle Order
            psOrder = conn.prepareStatement(updateOrderSQL);
            psOrder.setDouble(1, grandTotal);
            psOrder.setInt(2, orderId);
            psOrder.executeUpdate();

            // 2. Settle Customer loyalty points if active
            if (customerId != null) {
                psPoints = conn.prepareStatement(updatePointsSQL);
                psPoints.setInt(1, loyaltyPointsEarned);
                psPoints.setInt(2, loyaltyPointsDeducted);
                psPoints.setInt(3, customerId);
                psPoints.executeUpdate();
            }

            // 3. Free Table
            psTable = conn.prepareStatement(updateTableSQL);
            psTable.setInt(1, tableId);
            psTable.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("🔥 Billing transaction failed. Rolling back. Error: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            DatabaseConnection.closeResources(psOrder, psTable, psPoints, conn);
        }
    }

    // ==========================================
    // Analytics Aggregation Logic
    // ==========================================

    /**
     * Aggregated Query: Top selling menu items sorted by quantities sold.
     * 
     * @param limit maximum number of items
     * @return List of records holding stats
     */
    public List<Map<String, Object>> getTopSellingItems(int limit) {
        List<Map<String, Object>> stats = new ArrayList<>();
        String query = "SELECT m.item_name, SUM(oi.quantity) AS total_sold, SUM(oi.quantity * m.price) AS total_revenue " +
                       "FROM Order_Items oi " +
                       "JOIN Menu_Items m ON oi.menu_item_id = m.id " +
                       "JOIN Orders o ON oi.order_id = o.id " +
                       "WHERE o.order_status = 'PAID' " +
                       "GROUP BY m.id, m.item_name " +
                       "ORDER BY total_sold DESC " +
                       "LIMIT ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("item_name", rs.getString("item_name"));
                map.put("total_sold", rs.getInt("total_sold"));
                map.put("total_revenue", rs.getDouble("total_revenue"));
                stats.add(map);
            }
        } catch (SQLException e) {
            System.err.println("🔥 Error fetching top selling items: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
        return stats;
    }

    /**
     * Aggregated Query: Calculates sum of revenue collected today.
     * 
     * @return daily revenue amount
     */
    public double getDailyTotalRevenue() {
        String query = "SELECT COALESCE(SUM(total_amount), 0.0) AS daily_revenue " +
                       "FROM Orders " +
                       "WHERE order_status = 'PAID' AND DATE(timestamp) = CURRENT_DATE()";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("daily_revenue");
            }
        } catch (SQLException e) {
            System.err.println("🔥 Error calculating daily revenue: " + e.getMessage());
        } finally {
            DatabaseConnection.closeResources(rs, pstmt, conn);
        }
        return 0.0;
    }
}
