import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages the inventory of bikes, customers, and rental transactions with persistent storage.
 */
public class BikeRentalSystem {
    private List<Bike> inventory;
    private List<Customer> customers;
    private List<Rental> rentals;
    private static int nextCustomerId = 1;

    private static final String INVENTORY_FILE = "inventory.json";
    private static final String CUSTOMERS_FILE = "customers.json";
    private static final String RENTALS_FILE = "rentals.json";
    private Gson gson;

    // Constructor
    public BikeRentalSystem() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadInventory();
        loadCustomers();
        loadRentals();
    }
    
    // --- Data Persistence ---

    private void saveInventory() {
        try (FileWriter writer = new FileWriter(INVENTORY_FILE)) {
            gson.toJson(inventory, writer);
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
        }
    }

    private void loadInventory() {
        try (FileReader reader = new FileReader(INVENTORY_FILE)) {
            Type type = new TypeToken<ArrayList<Bike>>() {}.getType();
            inventory = gson.fromJson(reader, type);
            if (inventory == null) {
                inventory = new ArrayList<>();
            }
        } catch (IOException e) {
            inventory = new ArrayList<>();
        }
    }

    private void saveCustomers() {
        try (FileWriter writer = new FileWriter(CUSTOMERS_FILE)) {
            gson.toJson(customers, writer);
        } catch (IOException e) {
            System.err.println("Error saving customers: " + e.getMessage());
        }
    }

    private void loadCustomers() {
        try (FileReader reader = new FileReader(CUSTOMERS_FILE)) {
            Type type = new TypeToken<ArrayList<Customer>>() {}.getType();
            customers = gson.fromJson(reader, type);
            if (customers == null) {
                customers = new ArrayList<>();
            } else if (!customers.isEmpty()) {
                nextCustomerId = customers.stream().mapToInt(Customer::getCustomerId).max().orElse(0) + 1;
            }
        } catch (IOException e) {
            customers = new ArrayList<>();
        }
    }

    private void saveRentals() {
        List<RentalData> rentalData = rentals.stream()
            .map(r -> new RentalData(r.getRentalId(), r.getCustomer().getCustomerId(), r.getBike().getBikeId(), r.getStartTimeMillis(), r.isReturned()))
            .collect(Collectors.toList());
        try (FileWriter writer = new FileWriter(RENTALS_FILE)) {
            gson.toJson(rentalData, writer);
        } catch (IOException e) {
            System.err.println("Error saving rentals: " + e.getMessage());
        }
    }

    private void loadRentals() {
        try (FileReader reader = new FileReader(RENTALS_FILE)) {
            Type type = new TypeToken<ArrayList<RentalData>>() {}.getType();
            List<RentalData> rentalData = gson.fromJson(reader, type);
            rentals = new ArrayList<>();
            if (rentalData != null) {
                for (RentalData data : rentalData) {
                    Optional<Customer> customerOpt = findCustomer(data.customerId);
                    Optional<Bike> bikeOpt = findBike(data.bikeId);
                    if (customerOpt.isPresent() && bikeOpt.isPresent()) {
                        Rental rental = new Rental(data.rentalId, customerOpt.get(), bikeOpt.get(), data.startTimeMillis, data.isReturned);
                        rentals.add(rental);
                    }
                }
            }
        } catch (IOException e) {
            rentals = new ArrayList<>();
        }
    }

    public void saveData() {
        saveInventory();
        saveCustomers();
        saveRentals();
    }

    // --- Utility Methods ---
    
    public int getNextCustomerId() {
        return nextCustomerId++;
    }

    // --- Customer Management ---

    public void addCustomer(Customer customer) {
        customers.add(customer);
        saveCustomers();
    }

    public Optional<Customer> findCustomer(int customerId) {
        return customers.stream()
            .filter(c -> c.getCustomerId() == customerId)
            .findFirst();
    }

    // --- Bike Inventory Management ---

    public void addBike(Bike bike) {
        inventory.add(bike);
        saveInventory();
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

    public void sendBikeToRepair(String bikeId) {
        Optional<Bike> bikeOpt = findBike(bikeId);
        if (bikeOpt.isPresent()) {
            Bike bike = bikeOpt.get();
            if (bike.getStatus() == BikeStatus.AVAILABLE) {
                bike.setStatus(BikeStatus.IN_REPAIR);
                saveInventory();
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
                bike.setStatus(BikeStatus.AVAILABLE);
                saveInventory();
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

        // Create the new rental
        Rental newRental = new Rental(customerOpt.get(), bikeOpt.get());
        rentals.add(newRental);
        saveInventory();
        saveRentals();
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
        rental.returnBike(durationHours);
        saveInventory();
        saveRentals();
        
        return true;
    }
}
