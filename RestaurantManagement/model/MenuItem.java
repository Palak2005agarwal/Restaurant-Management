package Projects.RestaurantManagement.model;

/**
 * Represents a menu item in the catalog.
 * Holds details such as name, category, pricing, and active availability.
 */
public class MenuItem {
    private final int id;
    private final String name;
    private double price;
    private final String category;
    private boolean isAvailable;

    public MenuItem(int id, String name, double price, String category, boolean isAvailable) {
        if (id <= 0) {
            throw new IllegalArgumentException("Menu Item ID must be a positive integer.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Menu Item Name cannot be empty or null.");
        }
        if (price < 0.0) {
            throw new IllegalArgumentException("Menu Item Price cannot be negative.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Menu Item Category cannot be empty or null.");
        }
        
        this.id = id;
        this.name = name.trim();
        this.price = price;
        this.category = category.trim();
        this.isAvailable = isAvailable;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setPrice(double price) {
        if (price < 0.0) {
            throw new IllegalArgumentException("Menu Item Price cannot be negative.");
        }
        this.price = price;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    @Override
    public String toString() {
        return "MenuItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=₹" + String.format("%.2f", price) +
                ", category='" + category + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
