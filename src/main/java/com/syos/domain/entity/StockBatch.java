package com.syos.domain.entity;

import java.time.LocalDate;

public class StockBatch {
    private final int batchId;
    private final String itemCode;
    private final LocalDate purchaseDate;
    private final LocalDate expiryDate;
    private int quantity;

    public StockBatch(int batchId, String itemCode, LocalDate purchaseDate, LocalDate expiryDate, int quantity) {
        this.batchId = batchId;
        this.itemCode = itemCode;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
    }


    public boolean isExpiringBefore(StockBatch other) {
        return this.expiryDate.isBefore(other.getExpiryDate());
    }

    public void reduceQuantity(int amount) {
        if (amount <= this.quantity) {
            this.quantity -= amount;
        }
    }

    public int getBatchId() { return batchId; }
    public String getItemCode() { return itemCode; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public int getQuantity() { return quantity; }
}