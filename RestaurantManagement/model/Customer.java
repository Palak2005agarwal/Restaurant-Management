package Projects.RestaurantManagement.model;

/**
 * Represents a customer of the restaurant.
 * Manages customer contact information and loyalty reward points.
 */
public class Customer {
    private final int id;
    private final String name;
    private final String phone;
    private int loyaltyPoints;

    public Customer(int id, String name, String phone, int loyaltyPoints) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer Name cannot be null or empty.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer Phone cannot be null or empty.");
        }
        if (loyaltyPoints < 0) {
            throw new IllegalArgumentException("Loyalty points cannot be negative.");
        }
        this.id = id;
        this.name = name.trim();
        this.phone = phone.trim();
        this.loyaltyPoints = loyaltyPoints;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        if (loyaltyPoints < 0) {
            throw new IllegalArgumentException("Loyalty points cannot be negative.");
        }
        this.loyaltyPoints = loyaltyPoints;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", loyaltyPoints=" + loyaltyPoints +
                '}';
    }
}
