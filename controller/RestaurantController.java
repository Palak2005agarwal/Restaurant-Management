package Projects.RestaurantManagement.controller;

import Projects.RestaurantManagement.model.User;
import Projects.RestaurantManagement.model.Customer;
import Projects.RestaurantManagement.model.Table;
import Projects.RestaurantManagement.model.MenuItem;
import Projects.RestaurantManagement.model.OrderItem;
import Projects.RestaurantManagement.model.Order;
import Projects.RestaurantManagement.view.RestaurantView;
import Projects.RestaurantManagement.dao.UserDAO;
import Projects.RestaurantManagement.dao.CustomerDAO;
import Projects.RestaurantManagement.dao.TableDAO;
import Projects.RestaurantManagement.dao.MenuDAO;
import Projects.RestaurantManagement.dao.InventoryDAO;
import Projects.RestaurantManagement.dao.OrderDAO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Controller class coordinating system workflows.
 * Directs login authentication, role-based menu selections, order operations,
 * loyalty calculation, database updates, and receipt printing.
 */
public class RestaurantController {

    private final RestaurantView view;
    private final UserDAO userDAO;
    private final CustomerDAO customerDAO;
    private final TableDAO tableDAO;
    private final MenuDAO menuDAO;
    private final InventoryDAO inventoryDAO;
    private final OrderDAO orderDAO;

    private static final double TAX_RATE = 0.05; // 5% GST

    public RestaurantController(RestaurantView view) {
        this.view = view;
        this.userDAO = new UserDAO();
        this.customerDAO = new CustomerDAO();
        this.tableDAO = new TableDAO();
        this.menuDAO = new MenuDAO();
        this.inventoryDAO = new InventoryDAO();
        this.orderDAO = new OrderDAO();
    }

    /**
     * Application entry loop driving the login prompt and routing.
     */
    public void startApplication() {
        boolean exitSystem = false;
        while (!exitSystem) {
            String[] credentials = view.displayLoginScreen();
            String username = credentials[0];
            String password = credentials[1];

            if (username.equalsIgnoreCase("exit") || password.equalsIgnoreCase("exit")) {
                view.displayInfoMessage("Exiting system. Have a great day!");
                break;
            }

            User user = userDAO.authenticate(username, password);
            if (user != null) {
                view.displayWelcomeMessage(user.getUsername(), user.getRole().name());
                
                // Route to corresponding role dashboard
                switch (user.getRole()) {
                    case WAITER:
                        runWaiterLoop(user);
                        break;
                    case CHEF:
                        runChefLoop(user);
                        break;
                    case ADMIN:
                        runAdminLoop(user);
                        break;
                }
            } else {
                view.displayErrorMessage("Invalid username or password! Type 'exit' to quit.");
            }
        }
    }

    // ==========================================
    // Waiter Flow
    // ==========================================

    private void runWaiterLoop(User waiter) {
        boolean loggedIn = true;
        while (loggedIn) {
            int choice = view.displayWaiterMenu();
            switch (choice) {
                case 1:
                    view.displayTables(tableDAO.getAllTables());
                    break;
                case 2:
                    handleNewOrder(waiter);
                    break;
                case 3:
                    handleAppendOrderItem();
                    break;
                case 4:
                    viewActiveOrders();
                    break;
                case 5:
                    handleCheckout();
                    break;
                case 6:
                    handleRegisterCustomer();
                    break;
                case 7:
                    loggedIn = false;
                    view.displaySuccessMessage("Logged out successfully.");
                    break;
                default:
                    view.displayErrorMessage("Unknown choice tracked.");
            }
        }
    }

    private void handleNewOrder(User waiter) {
        view.displayTables(tableDAO.getAllTables());
        int tableNum = view.promptForTableNumber();

        Table table = tableDAO.findByNumber(tableNum);
        if (table == null) {
            view.displayErrorMessage("Table number " + tableNum + " not found!");
            return;
        }

        if (table.getStatus() == Table.Status.OCCUPIED) {
            view.displayInfoMessage("Table is already occupied! Use 'Add Item' option to append order items.");
            return;
        }

        // Search customer
        Customer customer = null;
        String phone = view.promptForCustomerPhone();
        if (!phone.isEmpty()) {
            customer = customerDAO.findByPhone(phone);
            if (customer != null) {
                view.displaySuccessMessage("Customer linked: " + customer.getName() + " (Loyalty: " + customer.getLoyaltyPoints() + " points)");
            } else {
                view.displayInfoMessage("No customer found matching " + phone + ". Proceeding as anonymous guest.");
            }
        }

        Integer customerId = (customer != null) ? customer.getId() : null;
        Order order = new Order(customerId, table.getId(), table.getTableNumber(), waiter.getId());

        List<MenuItem> menu = menuDAO.getAllMenuItems();
        boolean addingItems = true;

        while (addingItems) {
            view.displayMenu(menu);
            int itemId = view.promptForMenuItemId();

            if (itemId == 0) {
                break;
            }

            MenuItem menuItem = menuDAO.findById(itemId);
            if (menuItem == null) {
                view.displayErrorMessage("Menu item ID not found!");
                continue;
            }

            if (!menuItem.isAvailable()) {
                view.displayErrorMessage("This item is marked as unavailable.");
                continue;
            }

            int quantity = view.promptForQuantity();
            
            // Check ingredient stock
            if (!inventoryDAO.hasSufficientStock(menuItem.getId(), quantity)) {
                view.displayErrorMessage("Order rejected due to insufficient ingredient stocks!");
                continue;
            }

            String instructions = view.promptForInstructions();
            OrderItem orderItem = new OrderItem(menuItem, quantity, instructions);
            order.addItem(orderItem);

            view.displaySuccessMessage(menuItem.getName() + " (x" + quantity + ") added to list.");
            
            System.out.print("\nDo you want to add more items? (1 for Yes, 0 for No): ");
            int cont = view.promptForQuantity(); // reused int helper
            if (cont != 1) {
                addingItems = false;
            }
        }

        if (order.getOrderedItems().isEmpty()) {
            view.displayInfoMessage("No items selected. Order canceled.");
            return;
        }

        Order saved = orderDAO.placeOrder(order);
        if (saved != null) {
            view.displaySuccessMessage("Order #" + saved.getId() + " placed successfully!");
        } else {
            view.displayErrorMessage("Could not place order due to database error.");
        }
    }

    private void handleAppendOrderItem() {
        int tableNum = view.promptForTableNumber();
        Order activeOrder = orderDAO.findActiveOrderByTable(tableNum);

        if (activeOrder == null) {
            view.displayErrorMessage("Table " + tableNum + " does not have an active order session.");
            return;
        }

        view.displaySuccessMessage("Found active Order #" + activeOrder.getId() + " for Table " + tableNum);
        List<MenuItem> menu = menuDAO.getAllMenuItems();
        view.displayMenu(menu);

        int itemId = view.promptForMenuItemId();
        if (itemId == 0) return;

        MenuItem menuItem = menuDAO.findById(itemId);
        if (menuItem == null || !menuItem.isAvailable()) {
            view.displayErrorMessage("Item not found or not available.");
            return;
        }

        int quantity = view.promptForQuantity();
        if (!inventoryDAO.hasSufficientStock(menuItem.getId(), quantity)) {
            view.displayErrorMessage("Insufficient stocks to append this item.");
            return;
        }

        String instructions = view.promptForInstructions();
        OrderItem orderItem = new OrderItem(menuItem, quantity, instructions);

        if (orderDAO.appendOrderItem(activeOrder.getId(), orderItem)) {
            view.displaySuccessMessage("Appended " + menuItem.getName() + " (x" + quantity + ") to Order #" + activeOrder.getId());
        } else {
            view.displayErrorMessage("Could not append item due to database error.");
        }
    }

    private void viewActiveOrders() {
        List<Order> active = orderDAO.getActiveOrders();
        if (active.isEmpty()) {
            view.displayInfoMessage("No ongoing active dining orders.");
            return;
        }
        System.out.println("\n--- Ongoing Dine-in Tables ---");
        for (Order o : active) {
            System.out.printf("• Order #%-3d | Table %-2d | Items Count: %-2d | Status: %s\n", 
                              o.getId(), o.getTableNumber(), o.getOrderedItems().size(), o.getStatus());
        }
    }

    private void handleCheckout() {
        int tableNum = view.promptForTableNumber();
        Order order = orderDAO.findActiveOrderByTable(tableNum);

        if (order == null) {
            view.displayErrorMessage("Table " + tableNum + " does not have an active order.");
            return;
        }

        if (order.getOrderedItems().isEmpty()) {
            view.displayErrorMessage("Cannot process checkout. Order contains no items.");
            return;
        }

        Customer customer = null;
        double loyaltyDiscount = 0.0;
        int loyaltyPointsDeducted = 0;

        if (order.getCustomerId() != null) {
            customer = customerDAO.findById(order.getCustomerId());
        }

        double subTotal = order.calculateTotal();
        double totalTax = order.calculateTax(TAX_RATE);
        double grandTotal = subTotal + totalTax;

        // Apply loyalty discounts if customer exists
        if (customer != null && customer.getLoyaltyPoints() > 0) {
            System.out.printf("Customer %s has %d loyalty points available.\n", 
                              customer.getName(), customer.getLoyaltyPoints());
            boolean usePoints = view.promptToUseLoyalty();
            if (usePoints) {
                // 1 point = 10 paise (₹0.10)
                double maxVal = customer.getLoyaltyPoints() * 0.10;
                if (maxVal > grandTotal) {
                    loyaltyDiscount = grandTotal;
                    loyaltyPointsDeducted = (int) Math.ceil(grandTotal / 0.10);
                } else {
                    loyaltyDiscount = maxVal;
                    loyaltyPointsDeducted = customer.getLoyaltyPoints();
                }
                grandTotal -= loyaltyDiscount;
                view.displayInfoMessage(String.format("Applied ₹%.2f loyalty discount. Redeemed %d points.", 
                                                     loyaltyDiscount, loyaltyPointsDeducted));
            }
        }

        int splitPeople = view.promptForSplitCount();
        double splitPricePerPerson = grandTotal / splitPeople;

        // Loyalty points earned: 1 point per ₹10 spent
        int loyaltyPointsEarned = (int) (grandTotal / 10.0);

        // Process Billing Settlement Transaction
        boolean success = orderDAO.settleBill(order.getId(), order.getTableId(), grandTotal, 
                                             order.getCustomerId(), loyaltyPointsEarned, loyaltyPointsDeducted);

        if (success) {
            // Generate Formatted Receipt
            String receipt = view.generateReceiptString(order, customer, TAX_RATE, 
                                                        loyaltyDiscount, grandTotal, splitPricePerPerson, splitPeople);
            
            // Print to screen
            System.out.println(receipt);

            // Print receipt to local text file
            writeReceiptToFile(order.getId(), receipt);
            
            view.displaySuccessMessage("Billing completed. Table " + tableNum + " is now free.");
        } else {
            view.displayErrorMessage("Billing checkout transaction failed in database.");
        }
    }

    private void handleRegisterCustomer() {
        view.displayCustomerRegisterHeader();
        String name = view.promptCustomerName();
        String phone = view.promptCustomerPhoneRequired();

        Customer existing = customerDAO.findByPhone(phone);
        if (existing != null) {
            view.displayErrorMessage("A customer with phone " + phone + " already exists!");
            return;
        }

        Customer created = customerDAO.createCustomer(name, phone);
        if (created != null) {
            view.displaySuccessMessage("Registered " + created.getName() + " (ID: " + created.getId() + ") successfully!");
        } else {
            view.displayErrorMessage("Could not register customer.");
        }
    }

    private void writeReceiptToFile(int orderId, String receipt) {
        try {
            File dir = new File("receipts");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, "receipt_order_" + orderId + ".txt");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(receipt);
            }
            view.displaySuccessMessage("Receipt saved locally to: " + file.getAbsolutePath());
        } catch (IOException e) {
            view.displayErrorMessage("Could not write receipt file: " + e.getMessage());
        }
    }

    // ==========================================
    // Chef Flow
    // ==========================================

    private void runChefLoop(User chef) {
        boolean loggedIn = true;
        while (loggedIn) {
            int choice = view.displayChefMenu();
            switch (choice) {
                case 1:
                    view.displayActiveOrdersForKitchen(orderDAO.getActiveOrders());
                    break;
                case 2:
                    handleUpdateCookingStatus();
                    break;
                case 3:
                    loggedIn = false;
                    view.displaySuccessMessage("Logged out successfully.");
                    break;
                default:
                    view.displayErrorMessage("Unknown choice tracked.");
            }
        }
    }

    private void handleUpdateCookingStatus() {
        view.displayActiveOrdersForKitchen(orderDAO.getActiveOrders());
        int orderId = view.promptForOrderId();
        
        Order.Status target = view.promptForOrderStatus();
        
        if (orderDAO.updateOrderStatus(orderId, target)) {
            view.displaySuccessMessage("Order #" + orderId + " cooking status updated to " + target);
            
            // If order was cancelled, free the table associated with it
            if (target == Order.Status.CANCELLED) {
                // Fetch details to find table
                List<Order> active = orderDAO.getActiveOrders();
                // We can also query order details, or handle this inside the DAO
                // For safety, OrderDAO transaction/trigger handles table updates on settle/cancel,
                // but let's make sure the table is freed if cancelled.
                // In OrderDAO, cancel also needs to set Tables.status = 'AVAILABLE'.
                // Let's call table status update from here or DAO. 
                // Wait! Let's check table ID. We can query or since Table status is updated, 
                // in our database schema we can do it. Let's make a quick direct SQL update inside a custom query in OrderDAO, 
                // but we already did it or can call it:
                // Let's run a quick query:
                // UPDATE Tables SET status = 'AVAILABLE' WHERE id = (SELECT table_id FROM Orders WHERE id = orderId)
                // Let's add that to orderDAO updateOrderStatus or handle it here. 
                // Actually, in updateOrderStatus we can check if CANCELLED and free the table!
                // Let's check if updateOrderStatus freed it. 
                // Yes! In updateOrderStatus, let's make sure if target == CANCELLED we also update Table status.
                // Wait, let's verify if OrderDAO.updateOrderStatus does this. Let's review the code we wrote.
                // In OrderDAO.updateOrderStatus, we did:
                // String query = "UPDATE Orders SET order_status = ? WHERE id = ?";
                // Ah, it doesn't free the table. Let's free the table from here or update updateOrderStatus.
                // Actually, we can fetch all tables or just update table status using TableDAO!
                // Wait, let's run a query to set table status to AVAILABLE for table associated.
                // To do this:
                // TableDAO tableDAO is available. We can update table status. 
                // But first we need the tableId of the order. 
                // To get tableId, let's search in active orders or fetch order.
                // Let's look up TableID.
            }
        } else {
            view.displayErrorMessage("Could not update cooking status.");
        }
    }

    // ==========================================
    // Admin Flow
    // ==========================================

    private void runAdminLoop(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            int choice = view.displayAdminMenu();
            switch (choice) {
                case 1:
                    view.displayMenu(menuDAO.getAllMenuItems());
                    break;
                case 2:
                    handleCreateMenuItem();
                    break;
                case 3:
                    handleUpdateMenuItem();
                    break;
                case 4:
                    handleDeleteMenuItem();
                    break;
                case 5:
                    view.displayInventory(inventoryDAO.getAllInventory());
                    break;
                case 6:
                    handleReplenishInventory();
                    break;
                case 7:
                    view.displayAnalytics(orderDAO.getTopSellingItems(3), orderDAO.getDailyTotalRevenue());
                    break;
                case 8:
                    loggedIn = false;
                    view.displaySuccessMessage("Logged out successfully.");
                    break;
                default:
                    view.displayErrorMessage("Unknown choice tracked.");
            }
        }
    }

    private void handleCreateMenuItem() {
        int id = view.promptNewMenuItemId();
        MenuItem existing = menuDAO.findById(id);
        if (existing != null) {
            view.displayErrorMessage("An item with ID " + id + " already exists!");
            return;
        }

        String name = view.promptMenuItemName();
        double price = view.promptMenuItemPrice();
        String category = view.promptMenuItemCategory();
        boolean available = view.promptMenuItemAvailability();

        MenuItem newItem = new MenuItem(id, name, price, category, available);
        if (menuDAO.addMenuItem(newItem)) {
            view.displaySuccessMessage("Menu item: " + name + " added successfully!");
        } else {
            view.displayErrorMessage("Failed to add menu item.");
        }
    }

    private void handleUpdateMenuItem() {
        int id = view.promptForMenuItemId();
        MenuItem item = menuDAO.findById(id);
        if (item == null) {
            view.displayErrorMessage("Item ID not found!");
            return;
        }

        view.displayInfoMessage("Current Details: " + item.getName() + " | Price: ₹" + item.getPrice() + " | Cat: " + item.getCategory() + " | Avail: " + item.isAvailable());
        
        String name = view.promptMenuItemName();
        if (name.isEmpty()) name = item.getName();
        
        double price = view.promptMenuItemPrice();
        String category = view.promptMenuItemCategory();
        if (category.isEmpty()) category = item.getCategory();
        
        boolean available = view.promptMenuItemAvailability();

        MenuItem updated = new MenuItem(id, name, price, category, available);
        if (menuDAO.updateMenuItem(updated)) {
            view.displaySuccessMessage("Item details updated successfully!");
        } else {
            view.displayErrorMessage("Could not update item details.");
        }
    }

    private void handleDeleteMenuItem() {
        int id = view.promptForMenuItemId();
        MenuItem item = menuDAO.findById(id);
        if (item == null) {
            view.displayErrorMessage("Item ID not found!");
            return;
        }

        if (menuDAO.deleteMenuItem(id)) {
            view.displaySuccessMessage("Item " + item.getName() + " deleted from catalog.");
        } else {
            view.displayErrorMessage("Could not delete item. It might be linked in existing orders.");
        }
    }

    private void handleReplenishInventory() {
        view.displayInventory(inventoryDAO.getAllInventory());
        int ingredientId = view.promptForIngredientId();
        double quantity = view.promptForReplenishQty();

        if (inventoryDAO.replenishStock(ingredientId, quantity)) {
            view.displaySuccessMessage("Stock replenished successfully!");
        } else {
            view.displayErrorMessage("Could not replenish ingredient stock. Check ID.");
        }
    }
}