import java.io.Serializable;

public class Customer implements Serializable {
    private static final long serialVersionUID = 1L; 
    
    private int customerId;
    private String name;

    public Customer(int customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }
}