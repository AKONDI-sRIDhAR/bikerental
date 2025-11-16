import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.io.File;

/**
 * Main class to run the interactive Bike Rental System application.
 */
public class Main {

    private static final String ADMIN_USERNAME = "root";
    private static final String ADMIN_PASSWORD = "root";

    public static void main(String[] args) {
        BikeRentalSystem system = new BikeRentalSystem();

        // Initialize with default data only if the database is empty
        if (system.isDatabaseEmpty()) {
            System.out.println("Database is empty. Initializing with default data...");
            initializeData(system);
        }

        System.out.println("=================================================");
        System.out.println("      BIKE RENTAL SYSTEM - Initialization        ");
        System.out.println("=================================================");
        System.out.println("System is ready.");

    }

    // --- Helper Methods ---

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
}
