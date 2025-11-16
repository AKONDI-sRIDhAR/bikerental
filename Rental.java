
public class Rental {
    private int rentalId;
    private Customer customer;
    private Bike bike;
    private long startTimeMillis;
    private boolean isReturned;

    public Rental(int rentalId, Customer customer, Bike bike, long startTimeMillis, boolean isReturned) {
        this.rentalId = rentalId;
        this.customer = customer;
        this.bike = bike;
        this.startTimeMillis = startTimeMillis;
        this.isReturned = isReturned;
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

    public void returnBike(int durationHours) {
        if (isReturned) {
            System.out.println("INFO: This rental has already been finalized.");
            return;
        }

        double totalCost = bike.getHourlyRateRupees() * durationHours;
        
        this.isReturned = true;
        bike.setStatus(BikeStatus.AVAILABLE);

        System.out.println("\n--- RENTAL RECEIPT (ID: " + rentalId + ") ---");
        System.out.println("Customer Name: " + customer.getName());
        System.out.println("Bike Returned: " + bike.getType() + " (ID: " + bike.getBikeId() + ")");
        System.out.println("Hourly Rate: ₹" + String.format("%.2f", bike.getHourlyRateRupees()));
        System.out.println("Total Duration: " + durationHours + " hours");
        System.out.println("FINAL CHARGE: ₹" + String.format("%.2f", totalCost));
        System.out.println("----------------------------------------");
    }
    
    public void displayActiveRentalInfo() {
         System.out.println("  ID: " + rentalId + " | Customer: " + customer.getName() + " | Bike ID: " + bike.getBikeId() + " (" + bike.getType() + ")");
    }
}
