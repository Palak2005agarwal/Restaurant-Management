package Projects.RestaurantManagement;

import Projects.RestaurantManagement.view.RestaurantView;
import Projects.RestaurantManagement.controller.RestaurantController;

/**
 * Main entry point of the Console-Based Restaurant Management System.
 * Bootstraps the MVC view, controller, and database interactions.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("🚀 Initializing Database-Driven Menu & Billing System...");

        // Initialize MVC View (Handles console rendering and scanner parsing)
        RestaurantView view = new RestaurantView();

        // Initialize MVC Controller (Injects view, coordinates DAOs and business flows)
        RestaurantController controller = new RestaurantController(view);

        System.out.println("✅ System Bootstrapped Successfully.");

        // Launch the application driver
        controller.startApplication();
    }
}