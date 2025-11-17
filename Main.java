import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Optional;
import java.util.Date;

/**
 * Main class to run the interactive Bike Rental System application.
 */
public class Main {

    private static final String ADMIN_USERNAME = "root";
    private static final String ADMIN_PASSWORD = "root";
    private static final Scanner scanner = new Scanner(System.in);
    private static BikeRentalSystem system;

    public static void main(String[] args) {
        system = new BikeRentalSystem();

        System.out.println("=================================================");
        System.out.println("      BIKE RENTAL SYSTEM - Initialization        ");
        System.out.println("=================================================");
        
        // Initialize with default data only if the system starts empty (loadData failed or file didn't exist)
        if (system.isDataEmpty()) {
            initializeData(system);
        }
        
        // Start the application with admin login
        if (authenticateAdmin()) {
            System.out.println("✅ Login successful. Entering Main Menu.");
            runMainMenuLoop();
        } else {
            System.out.println("❌ Login failed. Exiting system.");
        }
    }
    
    // --- Authentication ---

    private static boolean authenticateAdmin() {
        System.out.print("\nAdmin Username: ");
        String username = scanner.nextLine();
        System.out.print("Admin Password: ");
        String password = scanner.nextLine();
        return ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password);
    }

    // --- Main Menu Loop ---

    private static void runMainMenuLoop() {
        boolean running = true;
        while (running) {
            displayMainMenu();
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1: // Start a rental
                        handleRentBike();
                        break;
                    case 2: // End a rental
                        handleReturnBike();
                        break;
                    case 3: // List available bikes
                        System.out.println("\n--- Available Bikes ---");
                        system.listAvailableBikes();
                        break;
                    case 4: // Active rentals
                        handleListActiveRentals();
                        break;
                    case 5: // Send a bike to repair
                        handleSendToRepair();
                        break;
                    case 6: // Cost estimation
                        handleCostEstimation();
                        break;
                    case 7: // Add New Bike
                        handleAddBike();
                        break;
                    case 8: // Exit / Logout
                        system.saveData(); // SAVE DATA ON EXIT
                        running = false;
                        System.out.println("Logged out. Thank you for using the Bike Rental System. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number from 1 to 8.");
                }
            } catch (InputMismatchException e) {
                System.out.println("❌ Invalid input. Please enter a number.");
                scanner.nextLine(); // Consume invalid input
            }
        }
    }

    private static void displayMainMenu() {
        System.out.println("\n--- MAIN MENU (Admin/Staff) ---");
        System.out.println("1. Start a Rental");
        System.out.println("2. End a Rental");
        System.out.println("3. List Available Bikes");
        System.out.println("4. View Active Rentals");
        System.out.println("5. Send a Bike to Repair");
        System.out.println("6. Cost Estimation");
        System.out.println("7. Add New Bike");
        System.out.println("8. Exit / Logout");
        System.out.print("Enter choice: ");
    }
    
    // --- Handler Methods ---

    private static void handleRentBike() {
        System.out.println("\n--- Start a Rental ---");
        
        System.out.print("Enter Customer Name (will be registered if new): ");
        String customerName = scanner.nextLine().trim();

        // 1. Find or Create Customer
        Optional<Customer> customerOpt = system.findCustomerByName(customerName);
        Customer customerToRent;

        if (customerOpt.isPresent()) {
            customerToRent = customerOpt.get();
            System.out.println("Found existing customer: " + customerToRent.getName() + " (ID: " + customerToRent.getCustomerId() + ")");
        } else {
            int newId = system.getNextCustomerId();
            customerToRent = new Customer(newId, customerName);
            system.addCustomer(customerToRent);
            System.out.println("✅ Registered new customer: " + customerToRent.getName() + " (ID: " + customerToRent.getCustomerId() + ")");
        }

        system.listAvailableBikes();
        System.out.print("Enter Bike ID to rent: ");
        String bikeId = scanner.nextLine().trim();

        Rental rental = system.rentBike(customerToRent.getCustomerId(), bikeId);
        if (rental != null) {
            System.out.println("✅ Rental successful! Rental ID is: " + rental.getRentalId());
        }
    }

    private static void handleReturnBike() {
        System.out.println("\n--- End a Rental ---");
        List<Rental> activeRentals = system.getCurrentlyRentedBikes();

        if (activeRentals.isEmpty()) {
            System.out.println("  (No bikes are currently rented.)");
            return;
        }

        System.out.println("Currently Active Rentals:");
        activeRentals.forEach(Rental::displayActiveRentalInfo);
        
        System.out.print("Enter Bike ID being returned: ");
        String bikeId = scanner.nextLine().trim();

        System.out.print("Enter rental duration (full hours): ");
        int durationHours = scanner.nextInt();
        scanner.nextLine();

        system.checkoutAndReturnBike(bikeId, durationHours);
    }
    
    private static void handleListActiveRentals() {
        System.out.println("\n--- Active Rentals ---");
        List<Rental> activeRentals = system.getCurrentlyRentedBikes();

        if (activeRentals.isEmpty()) {
            System.out.println("  (No bikes are currently rented.)");
        } else {
            System.out.println("------------------------------------------------------------------");
            System.out.printf("| %-8s | %-12s | %-15s | %-15s |\n", "Rental ID", "Customer ID", "Bike ID", "Start Time");
            System.out.println("------------------------------------------------------------------");
            for (Rental r : activeRentals) {
                // Formatting for display, using Date to show a readable time
                System.out.printf("| %-9d| %-12d | %-15s | %-15s |\n", 
                    r.getRentalId(), 
                    r.getCustomer().getCustomerId(), 
                    r.getBike().getBikeId(), 
                    new Date(r.getStartTimeMillis()) 
                );
            }
            System.out.println("------------------------------------------------------------------");
        }
    }
    
    private static void handleSendToRepair() {
        System.out.println("\n--- Send Bike to Repair ---");
        System.out.print("Enter Bike ID to send to repair: ");
        String bikeId = scanner.nextLine().trim();
        system.sendBikeToRepair(bikeId);
    }
    
    private static void handleCostEstimation() {
        System.out.println("\n--- Cost Estimation ---");
        System.out.print("Enter Bike ID for estimate: ");
        String bikeId = scanner.nextLine().trim();
        
        System.out.print("Enter estimated duration (in hours): ");
        int durationHours = 0;
        try {
            durationHours = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("❌ Invalid input for duration. Please enter a number.");
            scanner.nextLine();
            return;
        }

        Optional<Double> costOpt = system.calculateCostEstimate(bikeId, durationHours);
        
        if (costOpt.isPresent()) {
            System.out.printf("✅ Estimated Cost for Bike %s for %d hours: %.2f rs\n", 
                bikeId, durationHours, costOpt.get());
        } else {
            System.out.println("❌ Bike ID " + bikeId + " not found.");
        }
    }
    
    private static void handleAddBike() {
        System.out.println("\n--- Add New Bike ---");
        System.out.print("Enter New Bike ID (e.g., K99): ");
        String id = scanner.nextLine().trim();
        
        if (system.findBike(id).isPresent()) {
            System.out.println("❌ Error: Bike ID " + id + " already exists.");
            return;
        }
        
        System.out.print("Enter Bike Model (e.g., KTM Duke 390): ");
        String model = scanner.nextLine().trim();
        
        System.out.print("Enter Hourly Rental Rate (e.g., 950.00): ");
        double rate = 0.0;
        try {
            rate = scanner.nextDouble();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("❌ Invalid input for rate. Please enter a number.");
            scanner.nextLine();
            return;
        }
        
        system.addBike(new Bike(id, model, rate));
        System.out.println("✅ Successfully added new bike: " + id + " (" + model + ") at " + rate + " rs/hr.");
    }

    // --- Data Initialization ---

    private static void initializeData(BikeRentalSystem system) {
        // Pre-populate customers
        system.addCustomer(new Customer(system.getNextCustomerId(), "Rahul Sharma")); // ID 1
        system.addCustomer(new Customer(system.getNextCustomerId(), "Priya Singh")); // ID 2
        
        // Pre-populate bikes (Rates in Rupees)
        system.addBike(new Bike("RE1", "royal enfield classic 350", 1000.00));
        system.addBike(new Bike("CBR2", "Honda CBR 250R", 800.00));
        system.addBike(new Bike("P3", "Pulsar 150", 500.00));
        
        // Start a rental for demonstration
        system.rentBike(1, "RE1");
    }
}