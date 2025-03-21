package vttp.batch5.csf.assessment.server.models;

public class Item {
    
    private String id;
    private double price;
    private int quantity;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public Item() {
    }
    public Item(String id, double price, int quantity) {
        this.id = id;
        this.price = price;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Item [id=" + id + ", price=" + price + ", quantity=" + quantity + "]";
    }

}
