import java.util.Date;
import java.io.Serializable;

public class Bike implements Serializable {
    private static final long serialVersionUID = 1L; 
    
    private String bikeId;
    private String model; 
    private double hourlyRate;
    private BikeStatus status;
    private Date lastMaintenanceDate;
    private String notes;

    // Minimal Constructor
    public Bike(String bikeId, String model, double hourlyRate) {
        this.bikeId = bikeId;
        this.model = model;
        this.hourlyRate = hourlyRate;
        this.status = BikeStatus.AVAILABLE; 
        this.lastMaintenanceDate = new Date(); 
        this.notes = "";
    }

    // Getters
    public String getBikeId() { return bikeId; }
    public String getModel() { return model; } 
    public double getHourlyRate() { return hourlyRate; } 
    public BikeStatus getStatus() { return status; }
    public Date getLastMaintenanceDate() { return lastMaintenanceDate; }
    public String getNotes() { return notes; }

    // Setters
    public void setStatus(BikeStatus status) { this.status = status; }
    public void setLastMaintenanceDate(Date lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }
    public void setNotes(String notes) { this.notes = notes; }

    // Display
    public void displayInfo() {
        System.out.printf("| %-8s | %-25s | %-12.2f rs / hr |\n", bikeId, model, hourlyRate);
    }
}