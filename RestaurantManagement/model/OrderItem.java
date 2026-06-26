package Projects.RestaurantManagement.model;

/**
 * Represents a single line item within a customer's order.
 * Links a MenuItem to its parent order and tracks quantity and special cooking notes.
 */
public class OrderItem {
    private int id;
    private int orderId;
    private MenuItem item;
    private int quantity;
    private String specialInstructions;

    // Constructor when loading from database
    public OrderItem(int id, int orderId, MenuItem item, int quantity, String specialInstructions) {
        this(item, quantity, specialInstructions);
        this.id = id;
        this.orderId = orderId;
    }

    // Constructor when creating a new OrderItem programmatically
    public OrderItem(MenuItem item, int quantity, String specialInstructions) {
        if (item == null) {
            throw new IllegalArgumentException("Menu Item in OrderItem cannot be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Order quantity must be at least 1.");
        }
        
        this.item = item;
        this.quantity = quantity;
        this.specialInstructions = (specialInstructions == null || specialInstructions.trim().isEmpty()) 
                                   ? "None" 
                                   : specialInstructions.trim();
    }

    public double calculateSubTotal() {
        return item.getPrice() * quantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public MenuItem getItem() {
        return item;
    }

    public void setItem(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("MenuItem cannot be null.");
        }
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        this.quantity = quantity;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = (specialInstructions == null || specialInstructions.trim().isEmpty()) 
                                   ? "None" 
                                   : specialInstructions.trim();
    }
}
