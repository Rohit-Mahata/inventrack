package com.inventory.model;

public class Product {

    private int id;
    private String name;
    private String category;
    private int quantity;
    private double price;
    private int lowStockLimit;
    private String syncStatus;

    public Product() {}

    public Product(int id, String name, String category, int quantity, double price, int lowStockLimit, String syncStatus) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.lowStockLimit = lowStockLimit;
        this.syncStatus = syncStatus;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getLowStockLimit() { return lowStockLimit; }
    public void setLowStockLimit(int lowStockLimit) { this.lowStockLimit = lowStockLimit; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name=" + name + ", quantity=" + quantity + ", price=" + price + "}";
    }
}