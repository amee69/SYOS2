package com.syos.domain.entity;

import java.math.BigDecimal;

public class BillItem {
    private final String itemCode;
    private final String itemName;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal totalPrice;

    public BillItem(String itemCode, String itemName, int quantity, BigDecimal unitPrice) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity)); //stores total after logic
    }

    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
}