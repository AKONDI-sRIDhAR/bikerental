
public class RentalData {
    public int rentalId;
    public int customerId;
    public String bikeId;
    public long startTimeMillis;
    public boolean isReturned;

    public RentalData(int rentalId, int customerId, String bikeId, long startTimeMillis, boolean isReturned) {
        this.rentalId = rentalId;
        this.customerId = customerId;
        this.bikeId = bikeId;
        this.startTimeMillis = startTimeMillis;
        this.isReturned = isReturned;
    }
}
