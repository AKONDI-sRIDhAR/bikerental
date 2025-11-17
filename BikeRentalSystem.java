import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.*; 

/**
 * Manages the inventory of bikes, customers, and rental transactions, 
 * persisted using Java Serialization.
 */
public class BikeRentalSystem {
    
    private static final String DATA_FILE = "bikerental_data.ser"; 
    
    private List<Bike> inventory = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private List<Rental> rentals = new ArrayList<>();
    
    private int nextCustomerId = 1;
    private int nextRentalId = 1;

    public BikeRentalSystem() {
        loadData();
    }
    
    // --- Persistence Methods ---

    private void loadData() {
        File file = new File(DATA_FILE);
        if (file.exists() && file.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
                
                inventory = (List<Bike>) ois.readObject();
                customers = (List<Customer>) ois.readObject();
                rentals = (List<Rental>) ois.readObject();
                
                nextCustomerId = ois.readInt();
                nextRentalId = ois.readInt();
                
                System.out.println("✅ Loaded data successfully from " + DATA_FILE);

            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading data: " + e.getMessage());
            }
        }
    }
    
    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) { 
            
            oos.writeObject(inventory);
            oos.writeObject(customers);
            oos.writeObject(rentals);
            
            oos.writeInt(nextCustomerId);
            oos.writeInt(nextRentalId);
            
            System.out.println("✅ Data saved successfully to " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    // --- Utility Methods ---

    public boolean isDataEmpty() {
        return inventory.isEmpty() && customers.isEmpty();
    }
    
    public int getNextCustomerId() {
        return nextCustomerId++;
    }
    
    public int getNextRentalId() {
        return nextRentalId++;
    }

    // --- Customer Management (In-Memory) ---

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public List<Customer> getCustomers() {
        return customers;
    }
    
    public Optional<Customer> findCustomer(int customerId) {
        return customers.stream()
            .filter(c -> c.getCustomerId() == customerId)
            .findFirst();
    }
    
    public Optional<Customer> findCustomerByName(String name) {
        return customers.stream()
            .filter(c -> c.getName().equalsIgnoreCase(name))
            .findFirst();
    }

    // --- Bike Inventory Management (In-Memory) ---

    public void addBike(Bike bike) {
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
            System.out.println("----------------------------------------------");
            System.out.printf("| %-8s | %-25s | %-12s |\n", "ID", "Bike Model", "Rent");
            System.out.println("----------------------------------------------");
            for (Bike bike : available) {
                bike.displayInfo();
            }
            System.out.println("----------------------------------------------");
        }
    }

    public void listAllBikes() {
        if (inventory.isEmpty()) {
            System.out.println("  (No bikes in the inventory.)");
        } else {
            System.out.println("----------------------------------------------------------");
            System.out.printf("| %-8s | %-25s | %-12s | %-10s |\n", "ID", "Bike Model", "Rent", "Status");
            System.out.println("----------------------------------------------------------");
            for (Bike bike : inventory) {
                bike.displayInfoWithStatus();
            }
            System.out.println("----------------------------------------------------------");
        }
    }

    private void updateBikeStatus(String bikeId, BikeStatus status) {
        findBike(bikeId).ifPresent(b -> b.setStatus(status));
    }

    public void sendBikeToRepair(String bikeId) {
        Optional<Bike> bikeOpt = findBike(bikeId);
        if (bikeOpt.isPresent()) {
            Bike bike = bikeOpt.get();
            if (bike.getStatus() == BikeStatus.AVAILABLE) {
                updateBikeStatus(bikeId, BikeStatus.IN_REPAIR);
                System.out.println("✅ Bike " + bikeId + " sent to repair.");
            } else {
                System.out.println("❌ Bike " + bikeId + " cannot be sent to repair. Status: " + bike.getStatus());
            }
        } else {
            System.out.println("❌ Bike " + bikeId + " not found.");
        }
    }

    public void returnBikeFromRepair(String bikeId) {
        Optional<Bike> bikeOpt = findBike(bikeId);
        if (bikeOpt.isPresent()) {
            Bike bike = bikeOpt.get();
            if (bike.getStatus() == BikeStatus.IN_REPAIR) {
                updateBikeStatus(bikeId, BikeStatus.AVAILABLE);
                System.out.println("✅ Bike " + bikeId + " returned from repair.");
            } else {
                System.out.println("❌ Bike " + bikeId + " is not in repair. Status: " + bike.getStatus());
            }
        } else {
            System.out.println("❌ Bike " + bikeId + " not found.");
        }
    }
    
    // --- Cost Estimation ---
    
    public Optional<Double> calculateCostEstimate(String bikeId, int durationHours) {
        Optional<Bike> bikeOpt = findBike(bikeId);
        
        if (bikeOpt.isPresent()) {
            double rate = bikeOpt.get().getHourlyRate();
            double estimate = rate * durationHours;
            return Optional.of(estimate);
        }
        return Optional.empty();
    }

    // --- Rental Management (In-Memory) ---

    public Rental rentBike(int customerId, String bikeId) {
        Optional<Customer> customerOpt = findCustomer(customerId);
        Optional<Bike> bikeOpt = findBike(bikeId).filter(b -> b.getStatus() == BikeStatus.AVAILABLE);

        if (customerOpt.isEmpty()) {
            System.out.println("  ❌ ERROR: Customer ID " + customerId + " not found. This should not happen if called correctly.");
            return null;
        }

        if (bikeOpt.isEmpty()) {
            System.out.println("  ❌ ERROR: Bike ID " + bikeId + " is not available or does not exist.");
            return null;
        }

        Rental newRental = new Rental(
            getNextRentalId(),
            customerOpt.get(),
            bikeOpt.get(),
            System.currentTimeMillis(),
            false
        );

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

    public Optional<Rental> findActiveRentalById(int rentalId) {
        return getCurrentlyRentedBikes().stream()
                .filter(r -> r.getRentalId() == rentalId)
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

    public boolean checkoutAndReturnBike(int rentalId, int durationHours) {
        if (durationHours <= 0) {
            System.out.println("  ❌ ERROR: Rental duration must be greater than zero hours.");
            return false;
        }

        Optional<Rental> rentalOpt = findActiveRentalById(rentalId);

        if (rentalOpt.isEmpty()) {
            System.out.println("  ❌ ERROR: Rental ID " + rentalId + " is either not found or not active.");
            return false;
        }

        Rental rental = rentalOpt.get();
        String bikeId = rental.getBike().getBikeId();

        // Finalize in-memory object and print receipt
        rental.returnBike(durationHours);

        // Update bike status in memory
        updateBikeStatus(bikeId, BikeStatus.AVAILABLE);

        return true;
    }
}