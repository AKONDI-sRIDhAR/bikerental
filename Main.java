import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Main class to run the interactive Bike Rental System application.
 */
public class Main {

    private static final String ADMIN_USERNAME = "root";
    private static final String ADMIN_PASSWORD = "root";

    public static void main(String[] args) {
        BikeRentalSystem system = new BikeRentalSystem();
        Scanner scanner = new Scanner(System.in);

        // Pre-populate some data for testing
        initializeData(system);

        // 1. Authentication Check
        if (!authenticate(scanner)) {
            System.out.println("=================================================");
            System.out.println("❌ ACCESS DENIED. Invalid credentials. Exiting.");
            System.out.println("=================================================");
            return;
        }

        // 2. Main Menu Loop
        boolean running = true;
        while (running) {
            displayMenu();
            try {
                System.out.print("Enter your choice (1-6): ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        handleAddCustomerWithRental(system, scanner);
                        break;
                    case 2:
                        handleAddBike(system, scanner);
                        break;
                    case 3:
                        handleListAvailableBikes(system);
                        break;
                    case 4:
                        handleListRentedBikes(system);
                        break;
                    case 5:
                        handleCheckoutAndReturn(system, scanner);
                        break;
                    case 6:
                        running = false;
                        System.out.println("\n👋 Thank you for using the Bike Rental System! Goodbye.");
                        break;
                    default:
                        System.out.println("\n⚠️ Invalid choice. Please enter a number between 1 and 6.");
                }
            } catch (InputMismatchException e) {
                System.out.println("\n❌ Invalid input. Please enter a valid number for the menu choice.");
                scanner.nextLine(); // Consume the invalid input
            }
        }
        scanner.close();
    }
    
    // --- Helper Methods ---

    private static boolean authenticate(Scanner scanner) {
        System.out.println("=================================================");
        System.out.println("       BIKE RENTAL SYSTEM - ADMIN LOGIN          ");
        System.out.println("=================================================");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        return ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password);
    }

    private static void displayMenu() {
        System.out.println("\n=================================================");
        System.out.println("            BIKE RENTAL SYSTEM MENU              ");
        System.out.println("=================================================");
        System.out.println("1) Add Customer and Assign Bike (Start Rental)");
        System.out.println("2) Add New Bike to Inventory");
        System.out.println("3) See Current Bikes Available");
        System.out.println("4) List Actively Rented Bikes");
        System.out.println("5) Complete Rental & Generate Receipt (Checkout)");
        System.out.println("6) Exit System");
        System.out.println("=================================================");
    }
    
    private static void initializeData(BikeRentalSystem system) {
        // Pre-populate customers
        system.addCustomer(new Customer(system.getNextCustomerId(), "Rahul Sharma"));
        system.addCustomer(new Customer(system.getNextCustomerId(), "Priya Singh"));
        
        // Pre-populate bikes (Rates in Rupees)
        system.addBike(new Bike("R101", "Road Bike", 150.00));
        system.addBike(new Bike("M205", "Mountain Bike", 180.00));
        system.addBike(new Bike("E300", "Electric Scooter", 250.00));
        
        // Start a rental for demonstration
        system.rentBike(1, "R101");
    }

    // --- Menu Handlers ---
    
    private static void handleAddCustomerWithRental(BikeRentalSystem system, Scanner scanner) {
        System.out.println("\n--- 1) ADD CUSTOMER & START RENTAL ---");
        
        // 1. Add Customer
        System.out.print("Enter Customer Name: ");
        String name = scanner.nextLine();
        int newId = system.getNextCustomerId();
        Customer customer = new Customer(newId, name);
        system.addCustomer(customer);
        System.out.println("  ✅ SUCCESS: New Customer " + name + " added with ID: " + newId);
        
        // 2. Attempt to rent a bike immediately
        System.out.print("Do you want to assign a bike to " + name + " now? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            handleListAvailableBikes(system);
            System.out.print("Enter Bike ID to rent: ");
            String bikeId = scanner.nextLine();
            
            Rental rental = system.rentBike(newId, bikeId);
            if (rental != null) {
                System.out.println("  ✅ SUCCESS: Rental started! Rental ID: " + rental.getRentalId());
            }
        }
    }

    private static void handleAddBike(BikeRentalSystem system, Scanner scanner) {
        System.out.println("\n--- 2) ADD NEW BIKE TO INVENTORY ---");
        try {
            System.out.print("Enter Bike ID (e.g., K999): ");
            String id = scanner.nextLine();
            System.out.print("Enter Bike Type (e.g., City Cruiser): ");
            String type = scanner.nextLine();
            System.out.print("Enter Hourly Rate in Rupees (e.g., 150.00): ₹");
            double rate = scanner.nextDouble();
            scanner.nextLine(); // Consume newline

            Bike newBike = new Bike(id, type, rate);
            system.addBike(newBike);
            System.out.println("  ✅ SUCCESS: Bike " + id + " (" + type + ") added to inventory.");
        } catch (InputMismatchException e) {
            System.out.println("  ❌ ERROR: Invalid input for rate. Please use a number.");
            scanner.nextLine(); // Consume invalid input
        }
    }
    
    private static void handleListAvailableBikes(BikeRentalSystem system) {
        System.out.println("\n--- 3) CURRENTLY AVAILABLE BIKES ---");
        system.listAvailableBikes();
    }

    private static void handleListRentedBikes(BikeRentalSystem system) {
        System.out.println("\n--- 4) CURRENTLY ACTIVELY RENTED BIKES ---");
        List<Rental> activeRentals = system.getCurrentlyRentedBikes();
        
        if (activeRentals.isEmpty()) {
            System.out.println("  (No bikes are currently out for rental.)");
        } else {
            for (Rental rental : activeRentals) {
                rental.displayActiveRentalInfo();
            }
        }
    }

    private static void handleCheckoutAndReturn(BikeRentalSystem system, Scanner scanner) {
        System.out.println("\n--- 5) COMPLETE RENTAL & RECEIPT ---");
        handleListRentedBikes(system); // Show which bikes are out
        
        try {
            System.out.print("Enter the Bike ID being returned: ");
            String bikeId = scanner.nextLine();
            
            System.out.print("Enter the rental duration in WHOLE HOURS: ");
            int duration = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            system.checkoutAndReturnBike(bikeId, duration);
            
        } catch (InputMismatchException e) {
            System.out.println("  ❌ ERROR: Invalid input. Duration must be a whole number.");
            scanner.nextLine(); // Consume invalid input
        }
    }
}