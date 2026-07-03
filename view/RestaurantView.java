package Projects.RestaurantManagement.view;

import Projects.RestaurantManagement.model.MenuItem;
import Projects.RestaurantManagement.model.OrderItem;
import Projects.RestaurantManagement.model.Order;
import Projects.RestaurantManagement.model.Customer;
import Projects.RestaurantManagement.model.Table;
import Projects.RestaurantManagement.dao.InventoryDAO.Ingredient;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Handles all console UI rendering, dashboards, and safe scanner inputs.
 * Implements beautiful ASCII borders and line-based integer/float parsers.
 */
public class RestaurantView {
    private final Scanner sc = new Scanner(System.in);

    // ==========================================
    // Core Helper Input Prompts (Zero-Buffer Skipping)
    // ==========================================

    private String readLine() {
        return sc.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(readLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid entry! Please type a valid integer.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(readLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid entry! Please type a valid decimal number.");
            }
        }
    }

    // ==========================================
    // Authentication & Dashboard Menus
    // ==========================================

    public String[] displayLoginScreen() {
        System.out.println("\n+-------------------------------------------------+");
        System.out.println("|        THE CRISPY FORK - LOGIN PORTAL           |");
        System.out.println("+-------------------------------------------------+");
        System.out.print("👤 Username: ");
        String username = readLine();
        System.out.print("🔑 Password: ");
        String password = readLine();
        System.out.println("+-------------------------------------------------+");
        return new String[]{username, password};
    }

    public void displayWelcomeMessage(String username, String role) {
        System.out.printf("\n🔓 Access Granted! Welcome back, %s [%s]\n", username, role);
    }

    public int displayWaiterMenu() {
        System.out.println("\n+-------------------------------------------------+");
        System.out.println("|           WAITER / RECEPTION DASHBOARD          |");
        System.out.println("+-------------------------------------------------+");
        System.out.println("|  1. View Seating Tables Availability             |");
        System.out.println("|  2. Select Table & Place New Order              |");
        System.out.println("|  3. Add Item to Existing Order                  |");
        System.out.println("|  4. View Ongoing Active Orders                  |");
        System.out.println("|  5. Settle Payment / Checkout & Print Receipt   |");
        System.out.println("|  6. Register New Customer                       |");
        System.out.println("|  7. Logout                                      |");
        System.out.println("+-------------------------------------------------+");
        return readInt("👉 Select option (1-7): ");
    }

    public int displayChefMenu() {
        System.out.println("\n+-------------------------------------------------+");
        System.out.println("|                KITCHEN DASHBOARD                |");
        System.out.println("+-------------------------------------------------+");
        System.out.println("|  1. View Active Pending Orders                  |");
        System.out.println("|  2. Update Cooking Status                       |");
        System.out.println("|  3. Logout                                      |");
        System.out.println("+-------------------------------------------------+");
        return readInt("👉 Select option (1-3): ");
    }

    public int displayAdminMenu() {
        System.out.println("\n+-------------------------------------------------+");
        System.out.println("|             ADMINISTRATOR DASHBOARD             |");
        System.out.println("+-------------------------------------------------+");
        System.out.println("|  1. View Food Catalog Menu                      |");
        System.out.println("|  2. Add New Menu Item                           |");
        System.out.println("|  3. Update Menu Item Details                    |");
        System.out.println("|  4. Delete Menu Item                            |");
        System.out.println("|  5. Monitor Raw Ingredient Stocks               |");
        System.out.println("|  6. Replenish Ingredient Inventory              |");
        System.out.println("|  7. View Financial Sales & Metrics              |");
        System.out.println("|  8. Logout                                      |");
        System.out.println("+-------------------------------------------------+");
        return readInt("👉 Select option (1-8): ");
    }

    // ==========================================
    // Waiter Prompts
    // ==========================================

    public void displayTables(List<Table> tables) {
        System.out.println("\n====================== SEATING ROOM TABLES ======================");
        System.out.printf("| %-5s | %-12s | %-8s | %-15s |\n", "ID", "Table Number", "Capacity", "Status");
        System.out.println("-----------------------------------------------------------------");
        for (Table t : tables) {
            String color = t.getStatus() == Table.Status.AVAILABLE ? "🟢" : "🔴";
            System.out.printf("| %-5d | Table %-6d | %-8d | %-15s %s |\n", 
                              t.getId(), t.getTableNumber(), t.getCapacity(), t.getStatus(), color);
        }
        System.out.println("=================================================================");
    }

    public int promptForTableNumber() {
        return readInt("🔢 Enter Table Number: ");
    }

    public int promptForMenuItemId() {
        return readInt("🍔 Enter Menu Item ID (or 0 to complete/exit): ");
    }

    public int promptForQuantity() {
        while (true) {
            int qty = readInt("📦 Enter Quantity: ");
            if (qty > 0) return qty;
            System.out.println("❌ Quantity must be at least 1.");
        }
    }

    public String promptForInstructions() {
        System.out.print("✏️ Special Instructions (Press Enter to skip): ");
        String instr = readLine();
        return instr.isEmpty() ? "None" : instr;
    }

    public int promptForSplitCount() {
        while (true) {
            int split = readInt("👥 Split bill among how many guests? (Enter 1 if no split): ");
            if (split > 0) return split;
            System.out.println("❌ Must split among at least 1 person.");
        }
    }

    public String promptForCustomerPhone() {
        System.out.print("📞 Enter Customer Phone Number (Press Enter to skip customer lookup): ");
        return readLine();
    }

    public boolean promptToUseLoyalty() {
        System.out.print("💳 Use loyalty points for discount? (y/n): ");
        String choice = readLine().toLowerCase();
        return choice.equals("y") || choice.equals("yes");
    }

    public void displayCustomerRegisterHeader() {
        System.out.println("\n+-------------------------------------------------+");
        System.out.println("|           NEW CUSTOMER REGISTRATION             |");
        System.out.println("+-------------------------------------------------+");
    }

    public String promptCustomerName() {
        System.out.print("👤 Enter Customer Name: ");
        return readLine();
    }

    public String promptCustomerPhoneRequired() {
        while (true) {
            System.out.print("📞 Enter Phone Number: ");
            String phone = readLine();
            if (!phone.isEmpty()) return phone;
            System.out.println("❌ Phone number is required.");
        }
    }

    // ==========================================
    // Chef Prompts
    // ==========================================

    public void displayActiveOrdersForKitchen(List<Order> orders) {
        System.out.println("\n======================== KITCHEN QUEUE ========================");
        if (orders.isEmpty()) {
            System.out.println("🍳 No active pending or cooking orders inside the queue.");
        } else {
            for (Order o : orders) {
                System.out.printf("\n📋 ORDER ID: %d | TABLE: %d | STATUS: [%s] | TIME: %s\n", 
                                  o.getId(), o.getTableNumber(), o.getStatus(), o.getTimestamp());
                System.out.println("-----------------------------------------------------------------");
                for (OrderItem oi : o.getOrderedItems()) {
                    System.out.printf("  - %-20s x%d  [Instructions: %s]\n", 
                                      oi.getItem().getName(), oi.getQuantity(), oi.getSpecialInstructions());
                }
                System.out.println("-----------------------------------------------------------------");
            }
        }
        System.out.println("=================================================================");
    }

    public int promptForOrderId() {
        return readInt("📋 Enter Order ID: ");
    }

    public Order.Status promptForOrderStatus() {
        System.out.println("Choose Target Cooking Status:");
        System.out.println("1. COOKING");
        System.out.println("2. READY");
        System.out.println("3. CANCELLED");
        while (true) {
            int ch = readInt("👉 Choice (1-3): ");
            switch (ch) {
                case 1: return Order.Status.COOKING;
                case 2: return Order.Status.READY;
                case 3: return Order.Status.CANCELLED;
                default: System.out.println("❌ Selection invalid!");
            }
        }
    }

    // ==========================================
    // Admin CRUD & Analytics Prompts
    // ==========================================

    public void displayMenu(List<MenuItem> menu) {
        if (menu == null || menu.isEmpty()) {
            System.out.println("⚠️ The menu catalog is empty.");
            return;
        }

        System.out.println("\n=========================== THE CRISPY FORK MENU ===========================");
        System.out.printf("| %-5s | %-25s | %-12s | %-12s | %-9s |\n", "ID", "Item Name", "Category", "Price", "Available");
        System.out.println("-----------------------------------------------------------------------------");

        String currentCategory = "";
        for (MenuItem item : menu) {
            if (!item.getCategory().equalsIgnoreCase(currentCategory)) {
                currentCategory = item.getCategory();
                System.out.printf("| --- %-67s |\n", currentCategory.toUpperCase());
            }
            String avail = item.isAvailable() ? "Yes 🟢" : "No 🔴";
            System.out.printf("| %-5d | %-25s | %-12s | ₹%-10.2f | %-9s |\n", 
                              item.getId(), item.getName(), item.getCategory(), item.getPrice(), avail);
        }
        System.out.println("=============================================================================");
    }

    public int promptNewMenuItemId() {
        return readInt("🍔 Enter unique Menu Item ID (e.g. 401): ");
    }

    public String promptMenuItemName() {
        System.out.print("🍔 Enter Item Name: ");
        return readLine();
    }

    public double promptMenuItemPrice() {
        return readDouble("💰 Enter Price (₹): ");
    }

    public String promptMenuItemCategory() {
        System.out.print("🥗 Enter Category (Starters/Main Course/Desserts/Beverages): ");
        return readLine();
    }

    public boolean promptMenuItemAvailability() {
        System.out.print("🟢 Is this item available now? (y/n): ");
        String ans = readLine().toLowerCase();
        return ans.equals("y") || ans.equals("yes");
    }

    public void displayInventory(List<Ingredient> ingredients) {
        System.out.println("\n======================== RAW INGREDIENT INVENTORY ========================");
        System.out.printf("| %-5s | %-25s | %-15s | %-8s |\n", "ID", "Ingredient Name", "Stock Quantity", "Unit");
        System.out.println("---------------------------------------------------------------------------");
        for (Ingredient i : ingredients) {
            String alert = i.getQuantity() < 500 ? "⚠️ LOW" : "OK";
            System.out.printf("| %-5d | %-25s | %-15.2f | %-8s | %-5s |\n",
                              i.getId(), i.getName(), i.getQuantity(), i.getUnit(), alert);
        }
        System.out.println("===========================================================================");
    }

    public int promptForIngredientId() {
        return readInt("🪵 Enter Ingredient ID: ");
    }

    public double promptForReplenishQty() {
        while (true) {
            double qty = readDouble("📈 Enter Quantity to Add: ");
            if (qty > 0) return qty;
            System.out.println("❌ Replenishment quantity must be positive.");
        }
    }

    public void displayAnalytics(List<Map<String, Object>> topItems, double dailyRevenue) {
        System.out.println("\n=============================================");
        System.out.println("          FINANCIAL ANALYTICS ENGINE         ");
        System.out.println("=============================================");
        System.out.printf("💰 TODAY'S TOTAL REVENUE: ₹%,.2f\n", dailyRevenue);
        System.out.println("---------------------------------------------");
        System.out.println("🔥 TOP 3 SELLING ITEMS");
        System.out.printf("%-20s | %-10s | %-12s\n", "Item Name", "Qty Sold", "Revenue");
        System.out.println("---------------------------------------------");
        for (Map<String, Object> item : topItems) {
            System.out.printf("%-20s | %-10d | ₹%-11.2f\n", 
                              item.get("item_name"), 
                              item.get("total_sold"), 
                              item.get("total_revenue"));
        }
        System.out.println("=============================================");
    }

    // ==========================================
    // Receipt Printing and Text Formatting Helper
    // ==========================================

    /**
     * Formats the final sale receipt in a clean layout returned as a String.
     * This String is printed to console and written to file.
     */
    public String generateReceiptString(Order order, Customer customer, double taxRate, 
                                        double loyaltyDiscount, double grandTotal, double splitPricePerPerson, int splits) {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================\n");
        sb.append(String.format("%33s\n", "THE CRISPY FORK RESTAURANT"));
        sb.append(String.format("%28s %d\n", "TABLE NO:", order.getTableNumber()));
        sb.append(String.format("%31s %d\n", "ORDER ID:", order.getId()));
        if (customer != null) {
            sb.append(String.format("Customer: %-25s Phone: %-12s\n", customer.getName(), customer.getPhone()));
        }
        sb.append("=================================================\n");
        sb.append(String.format("%-20s %-5s %-10s %-10s\n", "Item Name", "Qty", "Price", "Total"));
        sb.append("-------------------------------------------------\n");

        for (OrderItem orderedItem : order.getOrderedItems()) {
            sb.append(String.format("%-20s %-5d ₹%-9.2f ₹%-10.2f\n",
                    orderedItem.getItem().getName(),
                    orderedItem.getQuantity(),
                    orderedItem.getItem().getPrice(),
                    orderedItem.calculateSubTotal()
            ));

            if (!orderedItem.getSpecialInstructions().equalsIgnoreCase("None")) {
                sb.append(String.format("  ↳ *Notes: %s*\n", orderedItem.getSpecialInstructions()));
            }
        }

        sb.append("-------------------------------------------------\n");
        sb.append(String.format("%-38s ₹%-10.2f\n", "Sub-Total:", order.calculateTotal()));
        sb.append(String.format("%-38s ₹%-10.2f\n", "Tax (" + (taxRate * 100) + "%):", order.calculateTax(taxRate)));
        if (loyaltyDiscount > 0) {
            sb.append(String.format("%-38s -₹%-10.2f\n", "Loyalty Reward Discount:", loyaltyDiscount));
        }
        sb.append("-------------------------------------------------\n");
        sb.append(String.format("**%-36s ₹%-10.2f**\n", "GRAND TOTAL:", grandTotal));
        sb.append("=================================================\n");

        if (splits > 1) {
            sb.append(String.format("Split Amount (%d guests): ₹%.2f per person\n", splits, splitPricePerPerson));
            sb.append("=================================================\n");
        }
        sb.append(String.format("%31s\n", "THANK YOU! VISIT AGAIN"));
        sb.append("=================================================\n");

        return sb.toString();
    }

    // Displays feedback/messages
    public void displayErrorMessage(String message) {
        System.out.println("\n🔥 [SYSTEM ERROR]: " + message);
    }

    public void displaySuccessMessage(String message) {
        System.out.println("\n✅ [SUCCESS]: " + message);
    }

    public void displayInfoMessage(String message) {
        System.out.println("\nℹ️ [INFO]: " + message);
    }
}
