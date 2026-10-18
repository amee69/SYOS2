package com.syos.domain.entity;

import java.math.BigDecimal;



//  source for item info

public class Item {
    private final String itemCode;
    private final String name;
    private final BigDecimal unitPrice;
    private final String category;
    private final int reorder_threshold;

    public Item(String itemCode, String name, BigDecimal unitPrice, String category, int reorderLevel) {
        this.itemCode = itemCode;
        this.name = name;
        this.unitPrice = unitPrice;
        this.category = category;
        this.reorder_threshold = reorderLevel;
    }

    // Getters
    public String getItemCode() { return itemCode; }
    public String getName() { return name; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getCategory() { return category; }
    public int getReorderLevel() { return reorder_threshold; }
}