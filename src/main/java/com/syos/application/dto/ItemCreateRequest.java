package com.syos.application.dto;

import java.math.BigDecimal;

public class ItemCreateRequest {
    private final String name;
    private final BigDecimal price;
    private final String categoryCode;
    private final int reorderThreshold;

    public ItemCreateRequest(String name, BigDecimal price, String categoryCode, int reorderThreshold) {
        this.name = name;
        this.price = price;
        this.categoryCode = categoryCode;
        this.reorderThreshold = reorderThreshold;
    }

    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public String getCategoryCode() { return categoryCode; }
    public int getReorderThreshold() { return reorderThreshold; }
}