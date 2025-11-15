/**
 * Represents a customer in the bike rental system.
 */
public class Customer {
    private int customerId;
    private String name;

    // Constructor
    public Customer(int customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }

    // Getters
    public int getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    // Method to display customer information
    public void displayInfo() {
        System.out.println("Customer ID: " + customerId + ", Name: " + name);
    }
}