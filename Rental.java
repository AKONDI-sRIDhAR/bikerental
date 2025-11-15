/**
 * Represents a single bike rental transaction.
 */
public class Rental {
    private static int nextRentalId = 1001; // Static counter for unique IDs
    private int rentalId;
    private Customer customer;
    private Bike bike;
    private long startTimeMillis; // Time when rental started
    private boolean isReturned;

    // Constructor: Starts the rental, records the time, and marks bike unavailable
    public Rental(Customer customer, Bike bike) {
        this.rentalId = nextRentalId++;
        this.customer = customer;
        this.bike = bike;
        this.startTimeMillis = System.currentTimeMillis();
        this.isReturned = false;
        
        bike.setAvailable(false); 
    }

    // Getters
    public int getRentalId() {
        return rentalId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Bike getBike() {
        return bike;
    }

    public boolean isReturned() {
        return isReturned;
    }

    // Method to calculate cost and finalize the rental upon return
    public void returnBike(int durationHours) {
        if (isReturned) {
            System.out.println("INFO: This rental has already been finalized.");
            return;
        }

        double totalCost = bike.getHourlyRateRupees() * durationHours;
        
        // Finalize state
        this.isReturned = true;
        bike.setAvailable(true); // Return bike to inventory

        // Print the final receipt
        System.out.println("\n--- RENTAL RECEIPT (ID: " + rentalId + ") ---");
        System.out.println("Customer Name: " + customer.getName());
        System.out.println("Bike Returned: " + bike.getType() + " (ID: " + bike.getBikeId() + ")");
        System.out.println("Hourly Rate: ₹" + String.format("%.2f", bike.getHourlyRateRupees()));
        System.out.println("Total Duration: " + durationHours + " hours");
        System.out.println("FINAL CHARGE: ₹" + String.format("%.2f", totalCost));
        System.out.println("----------------------------------------");
    }
    
    // Display summary for active rentals
    public void displayActiveRentalInfo() {
         System.out.println("  ID: " + rentalId + " | Customer: " + customer.getName() + " | Bike ID: " + bike.getBikeId() + " (" + bike.getType() + ")");
    }
}