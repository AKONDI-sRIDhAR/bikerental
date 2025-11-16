
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages the inventory of bikes, customers, and rental transactions with purely in-memory storage.
 * Data will be reset every time the program starts.
 */
public class BikeRentalSystem {

    // Initializing lists directly—no database connection needed
    private List<Bike> inventory = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private List<Rental> rentals = new ArrayList<>();

    private static int nextCustomerId = 1;
    private static int nextRentalId = 1; // Counter for in-memory rentals

    // Removed: private static final String DB_URL...
    // Removed: private Connection connection;

    // Constructor - NOW SIMPLER
    public BikeRentalSystem() {
        System.out.println("System initialized with in-memory storage.");
        // All data loading/initialization moved to the Main class check
    }
    
    // --- Database Methods (REMOVED) ---
    // initializeDatabase(), loadInventory(), loadCustomers(), loadRentals() are removed

    // --- Utility Methods ---

    public boolean isDatabaseEmpty() {
        // Now checks if the in-memory inventory is empty
        return inventory.isEmpty();
    }
    
    public int getNextCustomerId() {
        return nextCustomerId++;
    }

    public int getNextRentalId() {
        return nextRentalId++;
    }

    // --- Customer Management ---

    public void addCustomer(Customer customer) {
        // Only update the in-memory list
        customers.add(customer);
    }

    public Optional<Customer> findCustomer(int customerId) {
        return customers.stream()
            .filter(c -> c.getCustomerId() == customerId)
            .findFirst();
    }

    // --- Bike Inventory Management ---

    public void addBike(Bike bike) {
        // Only update the in-memory list
        inventory.add(bike);
    }

    public Optional<Bike> findBike(String bikeId) {
        return inventory.stream()
            .filter(b -> b.getBikeId().equalsIgnoreCase(bikeId))
            .findFirst();
    }

    public void listAvailableBikes() {
        List<Bike> available = inventory.stream()
                                        .filter(b -> b.getStatus() == BikeStatus.AVAILABLE)
                                        .collect(Collectors.toList());
        
        if (available.isEmpty()) {
            System.out.println("  (No bikes currently available for rent.)");
        } else {
            for (Bike bike : available) {
                bike.displayInfo();
            }
        }
    }

    private void updateBikeStatus(String bikeId, BikeStatus status) {
        // Only update the in-memory list
        findBike(bikeId).ifPresent(b -> b.setStatus(status));
    }

    public void sendBikeToRepair(String bikeId) {
        Optional<Bike> bikeOpt = findBike(bikeId);
        if (bikeOpt.isPresent()) {
            Bike bike = bikeOpt.get();
            if (bike.getStatus() == BikeStatus.AVAILABLE) {
                updateBikeStatus(bikeId, BikeStatus.IN_REPAIR);
                System.out.println("Bike " + bikeId + " sent to repair.");
            } else {
                System.out.println("Bike " + bikeId + " cannot be sent to repair. Status: " + bike.getStatus());
            }
        } else {
            System.out.println("Bike " + bikeId + " not found.");
        }
    }

    public void returnBikeFromRepair(String bikeId) {
        Optional<Bike> bikeOpt = findBike(bikeId);
        if (bikeOpt.isPresent()) {
            Bike bike = bikeOpt.get();
            if (bike.getStatus() == BikeStatus.IN_REPAIR) {
                updateBikeStatus(bikeId, BikeStatus.AVAILABLE);
                System.out.println("Bike " + bikeId + " returned from repair.");
            } else {
                System.out.println("Bike " + bikeId + " is not in repair. Status: " + bike.getStatus());
            }
        } else {
            System.out.println("Bike " + bikeId + " not found.");
        }
    }

    // --- Rental Management ---

    public Rental rentBike(int customerId, String bikeId) {
        Optional<Customer> customerOpt = findCustomer(customerId);
        Optional<Bike> bikeOpt = findBike(bikeId).filter(b -> b.getStatus() == BikeStatus.AVAILABLE);

        if (customerOpt.isEmpty()) {
            System.out.println("  ❌ ERROR: Customer ID " + customerId + " not found.");
            return null;
        }

        if (bikeOpt.isEmpty()) {
            System.out.println("  ❌ ERROR: Bike ID " + bikeId + " is not available or does not exist.");
            return null;
        }

        // Create the new rental using the required arguments
        Rental newRental = new Rental(
            getNextRentalId(),
            customerOpt.get(),
            bikeOpt.get(),
            System.currentTimeMillis(),
            false
        );

        // Update bike status and add to in-memory list
        updateBikeStatus(bikeId, BikeStatus.RENTED);
        rentals.add(newRental);
        return newRental;
    }
    
    public List<Rental> getCurrentlyRentedBikes() {
        return rentals.stream()
                      .filter(r -> !r.isReturned())
                      .collect(Collectors.toList());
    }
    
    public Optional<Rental> findActiveRentalByBikeId(String bikeId) {
        return getCurrentlyRentedBikes().stream()
                .filter(r -> r.getBike().getBikeId().equalsIgnoreCase(bikeId))
                .findFirst();
    }
    
    public boolean checkoutAndReturnBike(String bikeId, int durationHours) {
        if (durationHours <= 0) {
             System.out.println("  ❌ ERROR: Rental duration must be greater than zero hours.");
             return false;
        }
        
        Optional<Rental> rentalOpt = findActiveRentalByBikeId(bikeId);
        
        if (rentalOpt.isEmpty()) {
            System.out.println("  ❌ ERROR: Bike ID " + bikeId + " is either not found or is not actively rented.");
            return false;
        }
        
        Rental rental = rentalOpt.get();

        // Finalize in-memory object and print receipt
        rental.returnBike(durationHours);

        // Update bike status in memory
        updateBikeStatus(bikeId, BikeStatus.AVAILABLE);

        return true;
    }
}
