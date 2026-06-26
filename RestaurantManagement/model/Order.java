package Projects.RestaurantManagement.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a dining order in the system.
 * Tracks ordered items, preparation status, tables, servers, and payment totals.
 */
public class Order {

    public enum Status {
        PENDING,
        COOKING,
        READY,
        PAID,
        CANCELLED
    }

    private int id;
    private Integer customerId; // Nullable if guest checkout without registry
    private final int tableId;
    private final int tableNumber; // Helper field for easy printing
    private final int servedByUserId;
    private Status status;
    private double totalAmount;
    private final Timestamp timestamp;
    private final List<OrderItem> orderedItems;

    // Constructor when loading from database
    public Order(int id, Integer customerId, int tableId, int tableNumber, int servedByUserId, 
                 Status status, double totalAmount, Timestamp timestamp) {
        this.id = id;
        this.customerId = customerId;
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.servedByUserId = servedByUserId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.timestamp = timestamp;
        this.orderedItems = new ArrayList<>();
    }

    // Constructor when creating a new Order programmatically
    public Order(Integer customerId, int tableId, int tableNumber, int servedByUserId) {
        this.customerId = customerId;
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.servedByUserId = servedByUserId;
        this.status = Status.PENDING;
        this.totalAmount = 0.0;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.orderedItems = new ArrayList<>();
    }

    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add null OrderItem.");
        }
        orderedItems.add(item);
        recalculateTotal();
    }

    public void recalculateTotal() {
        double subTotal = 0.0;
        for (OrderItem item : orderedItems) {
            subTotal += item.calculateSubTotal();
        }
        this.totalAmount = subTotal;
    }

    public double calculateTotal() {
        recalculateTotal();
        return totalAmount;
    }

    public double calculateTax(double taxRate) {
        if (taxRate < 0.0) {
            throw new IllegalArgumentException("Tax rate cannot be negative.");
        }
        return calculateTotal() * taxRate;
    }

    public double calculateSplitBill(int people, double taxRate) {
        if (people <= 0) {
            throw new IllegalArgumentException("Number of people for splitting must be 1 or more.");
        }
        double baseTotal = calculateTotal();
        double taxAmount = baseTotal * taxRate;
        return (baseTotal + taxAmount) / people;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public int getTableId() {
        return tableId;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getServedByUserId() {
        return servedByUserId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Order status cannot be null.");
        }
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public List<OrderItem> getOrderedItems() {
        return orderedItems;
    }
}
