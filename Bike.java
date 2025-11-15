/**
 * Represents a bike available for rent.
 */
public class Bike {
    private String bikeId;
    private String type; // e.g., "Mountain", "Road", "Electric"
    private double hourlyRateRupees; // Rate in Indian Rupees (₹)
    private boolean isAvailable;

    // Constructor
    public Bike(String bikeId, String type, double hourlyRateRupees) {
        this.bikeId = bikeId;
        this.type = type;
        this.hourlyRateRupees = hourlyRateRupees;
        this.isAvailable = true; // Bikes start as available
    }

    // Getters
    public String getBikeId() {
        return bikeId;
    }

    public String getType() {
        return type;
    }

    public double getHourlyRateRupees() {
        return hourlyRateRupees;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    // Setters
    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    // Method to display bike information
    public void displayInfo() {
        String status = isAvailable ? "Available" : "Rented";
        System.out.println("Bike ID: " + bikeId + 
                           ", Type: " + type + 
                           ", Rate: ₹" + String.format("%.2f", hourlyRateRupees) + "/hr" + 
                           ", Status: " + status);
    }
}