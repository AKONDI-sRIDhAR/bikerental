import java.io.Serializable;

/**
 * Represents a single bike rental transaction.
 */
public class Rental implements Serializable {
    private static final long serialVersionUID = 1L; 
    
    private int rentalId;
    private Customer customer;
    private Bike bike;
    private long startTimeMillis; // Time when rental started
    private boolean isReturned;

    // Constructor for creation/loading
    public Rental(int rentalId, Customer customer, Bike bike, long startTimeMillis, boolean isReturned) {
        this.rentalId = rentalId;
        this.customer = customer;
        this.bike = bike;
        this.startTimeMillis = startTimeMillis;
        this.isReturned = isReturned;
        
        // When loaded/created, if not returned, the bike should be marked RENTED
        if (!isReturned) {
            bike.setStatus(BikeStatus.RENTED);
        }
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

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    // Method to calculate cost and finalize the rental upon return
    public void returnBike(int durationHours) {
        if (isReturned) {
            System.out.println("INFO: This rental has already been finalized.");
            return;
        }

        double totalCost = bike.getHourlyRate() * durationHours;
        
        // Finalize state
        this.isReturned = true;
        bike.setStatus(BikeStatus.AVAILABLE); // Return bike to inventory

        // Print the final receipt - Updated format
        System.out.println("\n--- RENTAL RECEIPT (ID: " + rentalId + ") ---");
        System.out.println("Customer Name: " + customer.getName());
        System.out.println("Bike Returned: " + bike.getModel() + " (ID: " + bike.getBikeId() + ")");
        System.out.println("Hourly Rate: " + String.format("%.2f", bike.getHourlyRate()) + " rs / hr");
        System.out.println("Total Duration: " + durationHours + " hours");
        System.out.println("FINAL CHARGE: " + String.format("%.2f", totalCost) + " rs");
        System.out.println("----------------------------------------");
    }
    
    // Display summary for active rentals
    public void displayActiveRentalInfo() {
         System.out.println("  ID: " + rentalId + " | Customer: " + customer.getName() + " (ID: " + customer.getCustomerId() + ") | Bike ID: " + bike.getBikeId() + " (" + bike.getModel() + ")");
    }
}