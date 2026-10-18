package com.syos.application.dto;

import java.time.LocalDate;

public class StockBatchRequest {
    private final String itemCode;
    private final int quantity;
    private final LocalDate expiryDate;

    public StockBatchRequest(String itemCode, int quantity, LocalDate expiryDate) {
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
    }

    public String getItemCode() { return itemCode; }
    public int getQuantity() { return quantity; }
    public LocalDate getExpiryDate() { return expiryDate; }
}