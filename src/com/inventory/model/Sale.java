package com.inventory.model;

public class Sale {

    private int id;
    private int productId;
    private String productName;
    private int quantity;
    private double total;
    private String saleDate;
    private String syncStatus;

    public Sale() {}

    public Sale(int id, int productId, String productName, int quantity, double total, String saleDate, String syncStatus) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.total = total;
        this.saleDate = saleDate;
        this.syncStatus = syncStatus;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getSaleDate() { return saleDate; }
    public void setSaleDate(String saleDate) { this.saleDate = saleDate; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }

    @Override
    public String toString() {
        return "Sale{id=" + id + ", productName=" + productName + ", quantity=" + quantity + ", total=" + total + "}";
    }
}