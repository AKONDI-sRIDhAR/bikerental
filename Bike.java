
import java.util.Date;

/**
 * Represents a bike available for rent.
 */
public class Bike {
    private String bikeId;
    private String type; // e.g., "Mountain", "Road", "Electric"
    private double hourlyRateRupees; // Rate in Indian Rupees (₹)
    private BikeStatus status;
    private Date lastMaintenanceDate;
    private String notes;

    // Constructor
    public Bike(String bikeId, String type, double hourlyRateRupees) {
        this.bikeId = bikeId;
        this.type = type;
        this.hourlyRateRupees = hourlyRateRupees;
        this.status = BikeStatus.AVAILABLE; // Bikes start as available
        this.lastMaintenanceDate = new Date(); // Default to current date
        this.notes = "";
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

    public BikeStatus getStatus() {
        return status;
    }

    public Date getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public String getNotes() {
        return notes;
    }

    // Setters
    public void setStatus(BikeStatus status) {
        this.status = status;
    }

    public void setLastMaintenanceDate(Date lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Method to display bike information
    public void displayInfo() {
        System.out.println("Bike ID: " + bikeId +
                           ", Type: " + type +
                           ", Rate: ₹" + String.format("%.2f", hourlyRateRupees) + "/hr" +
                           ", Status: " + status);
    }
}
