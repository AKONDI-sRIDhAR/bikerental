import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages the inventory of bikes, customers, and rental transactions with persistent storage using a SQLite database.
 */
public class BikeRentalSystem {

    // Initializing lists directly—no database connection needed
    private List<Bike> inventory = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private List<Rental> rentals = new ArrayList<>();

    private static int nextCustomerId = 1;
    private static int nextRentalId = 1; // Counter for in-memory rentals

    private static final String DB_URL = "jdbc:sqlite:bikerental.db";
    private Connection connection;

    // Constructor - NOW SIMPLER
    public BikeRentalSystem() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            initializeDatabase();
            loadInventory();
            loadCustomers();
            loadRentals();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Database setup error: " + e.getMessage());
            // Exit if we can't connect to the database
            System.exit(1);
        }
    }
    
    // --- Database Initialization ---

    private void initializeDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create Bikes Table
            stmt.execute("CREATE TABLE IF NOT EXISTS bikes (" +
                         "bikeId TEXT PRIMARY KEY," +
                         "type TEXT NOT NULL," +
                         "hourlyRateRupees REAL NOT NULL," +
                         "status TEXT NOT NULL," +
                         "lastMaintenanceDate INTEGER," +
                         "notes TEXT)");

            // Create Customers Table
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                         "customerId INTEGER PRIMARY KEY," +
                         "name TEXT NOT NULL)");

            // Create Rentals Table
            stmt.execute("CREATE TABLE IF NOT EXISTS rentals (" +
                         "rentalId INTEGER PRIMARY KEY," +
                         "customerId INTEGER NOT NULL," +
                         "bikeId TEXT NOT NULL," +
                         "startTimeMillis INTEGER NOT NULL," +
                         "isReturned INTEGER NOT NULL DEFAULT 0," + // 0 for false, 1 for true
                         "FOREIGN KEY (customerId) REFERENCES customers(customerId)," +
                         "FOREIGN KEY (bikeId) REFERENCES bikes(bikeId))");
        }
    }

    // --- Data Loading from Database ---

    private void loadInventory() {
        inventory = new ArrayList<>();
        String sql = "SELECT * FROM bikes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Bike bike = new Bike(
                    rs.getString("bikeId"),
                    rs.getString("type"),
                    rs.getDouble("hourlyRateRupees")
                );
                bike.setStatus(BikeStatus.valueOf(rs.getString("status")));
                bike.setLastMaintenanceDate(new Date(rs.getLong("lastMaintenanceDate")));
                bike.setNotes(rs.getString("notes"));
                inventory.add(bike);
            }
        } catch (SQLException e) {
            System.err.println("Error loading inventory from database: " + e.getMessage());
        }
    }

    private void loadCustomers() {
        customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY customerId";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Customer customer = new Customer(
                    rs.getInt("customerId"),
                    rs.getString("name")
                );
                customers.add(customer);
            }
            // Set the next customer ID based on the last one loaded
            if (!customers.isEmpty()) {
                nextCustomerId = customers.get(customers.size() - 1).getCustomerId() + 1;
            }
        } catch (SQLException e) {
            System.err.println("Error loading customers from database: " + e.getMessage());
        }
    }

    private void loadRentals() {
        rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int customerId = rs.getInt("customerId");
                String bikeId = rs.getString("bikeId");

                Optional<Customer> customerOpt = findCustomer(customerId);
                Optional<Bike> bikeOpt = findBike(bikeId);

                if (customerOpt.isPresent() && bikeOpt.isPresent()) {
                    Rental rental = new Rental(
                        rs.getInt("rentalId"),
                        customerOpt.get(),
                        bikeOpt.get(),
                        rs.getLong("startTimeMillis"),
                        rs.getInt("isReturned") == 1
                    );
                    rentals.add(rental);
                } else {
                     System.err.println("Could not load rental " + rs.getInt("rentalId") + " due to missing customer or bike.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading rentals from database: " + e.getMessage());
        }
    }

    // --- Data Persistence (No longer needed, but keeping the empty method for compatibility) ---
    public void saveData() {
        // Data is now saved directly to the database with each transaction.
    }

    // --- Utility Methods ---

    public boolean isDatabaseEmpty() {
        String sql = "SELECT COUNT(*) FROM bikes";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if database is empty: " + e.getMessage());
        }
        return true; // Assume empty if there's an error
    }
    
    public int getNextCustomerId() {
        return nextCustomerId++;
    }

    public int getNextRentalId() {
        return nextRentalId++;
    }

    // --- Customer Management ---

    public void addCustomer(Customer customer) {
        String sql = "INSERT INTO customers(customerId, name) VALUES(?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, customer.getCustomerId());
            pstmt.setString(2, customer.getName());
            pstmt.executeUpdate();
            // Also add to in-memory list
            customers.add(customer);
        } catch (SQLException e) {
            System.err.println("Error adding customer: " + e.getMessage());
        }
    }

    public Optional<Customer> findCustomer(int customerId) {
        return customers.stream()
            .filter(c -> c.getCustomerId() == customerId)
            .findFirst();
    }

    // --- Bike Inventory Management ---

    public void addBike(Bike bike) {
        String sql = "INSERT INTO bikes(bikeId, type, hourlyRateRupees, status, lastMaintenanceDate, notes) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, bike.getBikeId());
            pstmt.setString(2, bike.getType());
            pstmt.setDouble(3, bike.getHourlyRateRupees());
            pstmt.setString(4, bike.getStatus().name());
            pstmt.setLong(5, bike.getLastMaintenanceDate().getTime());
            pstmt.setString(6, bike.getNotes());
            pstmt.executeUpdate();
            // Also add to in-memory list
            inventory.add(bike);
        } catch (SQLException e) {
            System.err.println("Error adding bike: " + e.getMessage());
        }
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
        String sql = "UPDATE bikes SET status = ? WHERE bikeId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, bikeId);
            pstmt.executeUpdate();
            // Also update in-memory list
            findBike(bikeId).ifPresent(b -> b.setStatus(status));
        } catch (SQLException e) {
            System.err.println("Error updating bike status: " + e.getMessage());
        }
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

        // Create the new rental
        Rental newRental = new Rental(customerOpt.get(), bikeOpt.get());

        String sql = "INSERT INTO rentals(rentalId, customerId, bikeId, startTimeMillis, isReturned) VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newRental.getRentalId());
            pstmt.setInt(2, newRental.getCustomer().getCustomerId());
            pstmt.setString(3, newRental.getBike().getBikeId());
            pstmt.setLong(4, newRental.getStartTimeMillis());
            pstmt.setInt(5, 0); // Not returned
            pstmt.executeUpdate();

            // Update bike status
            updateBikeStatus(bikeId, BikeStatus.RENTED);
            rentals.add(newRental);
            return newRental;
        } catch (SQLException e) {
            System.err.println("Error creating rental: " + e.getMessage());
            // Rollback status change in-memory if DB fails
            bikeOpt.get().setStatus(BikeStatus.AVAILABLE);
            return null;
        }
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

        String sql = "UPDATE rentals SET isReturned = 1 WHERE rentalId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, rental.getRentalId());
            pstmt.executeUpdate();

            // Finalize in-memory object and print receipt
            rental.returnBike(durationHours);

            // Update bike status in DB and memory
            updateBikeStatus(bikeId, BikeStatus.AVAILABLE);

            return true;
        } catch (SQLException e) {
            System.err.println("Error finalizing rental return: " + e.getMessage());
            return false;
        }
    }
}
