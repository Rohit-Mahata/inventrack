package com.inventory.model;

public class StockMovement {

    private int id;
    private int productId;
    private String productName;
    private String type;
    private int quantity;
    private String note;
    private String moveDate;

    public StockMovement() {}

    public StockMovement(int id, int productId, String productName, String type, int quantity, String note, String moveDate) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.type = type;
        this.quantity = quantity;
        this.note = note;
        this.moveDate = moveDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getMoveDate() { return moveDate; }
    public void setMoveDate(String moveDate) { this.moveDate = moveDate; }

    @Override
    public String toString() {
        return "StockMovement{id=" + id + ", productName=" + productName + ", type=" + type + ", quantity=" + quantity + "}";
    }
}