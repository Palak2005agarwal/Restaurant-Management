package Projects.RestaurantManagement.model;

/**
 * Represents a physical dining table inside the restaurant.
 * Tracks table capacity and status (AVAILABLE, OCCUPIED, RESERVED).
 */
public class Table {

    public enum Status {
        AVAILABLE,
        OCCUPIED,
        RESERVED
    }

    private final int id;
    private final int tableNumber;
    private final int capacity;
    private Status status;

    public Table(int id, int tableNumber, int capacity, Status status) {
        if (tableNumber <= 0) {
            throw new IllegalArgumentException("Table number must be positive.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Table capacity must be positive.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Table status cannot be null.");
        }
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }
        this.status = status;
    }

    @Override
    public String toString() {
        return "Table{" +
                "id=" + id +
                ", tableNumber=" + tableNumber +
                ", capacity=" + capacity +
                ", status=" + status +
                '}';
    }
}
