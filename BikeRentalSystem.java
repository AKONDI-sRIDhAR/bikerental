import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages the inventory of bikes, customers, and rental transactions.
 */
public class BikeRentalSystem {
    private List<Bike> inventory;
    private List<Customer> customers;
    private List<Rental> rentals;
    private static int nextCustomerId = 1;

    // Constructor
    public BikeRentalSystem() {
        this.inventory = new ArrayList<>();
        this.customers = new ArrayList<>();
        this.rentals = new ArrayList<>();
    }
    
    // --- Utility Methods ---
    
    public int getNextCustomerId() {
        return nextCustomerId++;
    }

    // --- Customer Management ---

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public Optional<Customer> findCustomer(int customerId) {
        return customers.stream()
            .filter(c -> c.getCustomerId() == customerId)
            .findFirst();
    }

    // --- Bike Inventory Management ---

    public void addBike(Bike bike) {
        inventory.add(bike);
    }

    public Optional<Bike> findAvailableBike(String bikeId) {
        return inventory.stream()
            .filter(b -> b.getBikeId().equalsIgnoreCase(bikeId) && b.isAvailable())
            .findFirst();
    }
    
    public void listAvailableBikes() {
        List<Bike> available = inventory.stream()
                                        .filter(Bike::isAvailable)
                                        .collect(Collectors.toList());
        
        if (available.isEmpty()) {
            System.out.println("  (No bikes currently available for rent.)");
        } else {
            for (Bike bike : available) {
                bike.displayInfo();
            }
        }
    }

    // --- Rental Management ---

    public Rental rentBike(int customerId, String bikeId) {
        Optional<Customer> customerOpt = findCustomer(customerId);
        Optional<Bike> bikeOpt = findAvailableBike(bikeId);

        if (customerOpt.isEmpty()) {
            System.out.println("  ❌ ERROR: Customer ID " + customerId + " not found.");
            return null;
        }

        if (bikeOpt.isEmpty()) {
            System.out.println("  ❌ ERROR: Bike ID " + bikeId + " is not available or does not exist.");
            return null;
        }

        // Create the new rental
        Rental newRental = new Rental(customerOpt.get(), bikeOpt.get());
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
    
    // Function to handle checkout, payment, and inventory update (Option 5)
    public boolean checkoutAndReturnBike(String bikeId, int durationHours) {
        if (durationHours <= 0) {
             System.out.println("  ❌ ERROR: Rental duration must be greater than zero hours.");
             return false;
        }
        
        // 1. Find the active rental by the bike ID
        Optional<Rental> rentalOpt = findActiveRentalByBikeId(bikeId);
        
        if (rentalOpt.isEmpty()) {
            System.out.println("  ❌ ERROR: Bike ID " + bikeId + " is either not found or is not actively rented.");
            return false;
        }
        
        Rental rental = rentalOpt.get();
        
        // 2. Finalize the rental (calculates cost, marks bike available)
        rental.returnBike(durationHours);
        
        return true;
    }
}