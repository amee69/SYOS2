package com.syos.domain.entity;

import java.math.BigDecimal;


 //   loyalty tier configuration from db.

public class LoyaltyTier {
    private final String tierName;
    private final BigDecimal minPurchases;
    private final int pointsPer100;
    private final BigDecimal discountPercent;

    public LoyaltyTier(String tierName, BigDecimal minPurchases, int pointsPer100, BigDecimal discountPercent) {
        this.tierName = tierName;
        this.minPurchases = minPurchases;
        this.pointsPer100 = pointsPer100;
        this.discountPercent = discountPercent;
    }

    public String getTierName() { return tierName; }
    public BigDecimal getMinPurchases() { return minPurchases; } //used before(check)
    public int getPointsPer100() { return pointsPer100; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
}