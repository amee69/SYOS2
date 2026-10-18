package com.syos.domain.entity;

import java.math.BigDecimal;

public class Customer {
    private final int customerId;
    private final String email;
    private final String fullName;
    private final String phone;
    private final String password;
    private int loyaltyPoints;
    private BigDecimal totalPurchases;
    private String customerTier;

    // Constructor 1 (loading from DB - no phone/password needed)
    public Customer(int customerId, String email, String fullName,
                    int loyaltyPoints, BigDecimal totalPurchases, String customerTier) {
        this(customerId, email, fullName, null, null, loyaltyPoints, totalPurchases, customerTier);
    }

    // Constructor 2 (full - for registration)
    public Customer(int customerId, String email, String fullName, String phone, String password,
                    int loyaltyPoints, BigDecimal totalPurchases, String customerTier) {
        this.customerId = customerId;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.password = password;
        this.loyaltyPoints = loyaltyPoints;
        this.totalPurchases = totalPurchases;
        this.customerTier = customerTier;
    }

    // ============ getters ============
    public int getCustomerId() { return customerId; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public BigDecimal getTotalPurchases() { return totalPurchases; }
    public String getCustomerTier() { return customerTier; }


    // ============ loyalty points ============

    public void addLoyaltyPoints(int points) {

        this.loyaltyPoints += points;
    }


    public boolean canRedeemPoints(int points) {

        return this.loyaltyPoints >= points;
    }

    public void redeemLoyaltyPoints(int points) {
        if (!canRedeemPoints(points)) {
            throw new IllegalArgumentException("Insufficient loyalty points. Available: " + loyaltyPoints);
        }
        this.loyaltyPoints -= points;
    }


    // ============ purchase and tiers ============

    public void addPurchase(BigDecimal amount) {
        this.totalPurchases = this.totalPurchases.add(amount);
    }

    public void updateTier(String newTier) {
        this.customerTier = newTier;
    }


    // ============ toer benefgits from db ============


//      get discount percentage from the LoyaltyTier object from db
//      gold tier → returns 10 10%

    public BigDecimal getTierDiscountPercent(LoyaltyTier tier) {
        if (tier == null) {
            return BigDecimal.ZERO;
        }
        return tier.getDiscountPercent();
    }


//      calculate points earned based on purchase amount and tier multiplier from DB
//    logic iz purchase 500 LKR as GOLD (3x) → earns 15 points

    public int calculatePointsEarned(BigDecimal purchaseAmount, LoyaltyTier tier) {

        //  Figure out the multiplier (points per 100)
        int pointsPer100;

        if (tier != null) {
            pointsPer100 = tier.getPointsPer100();
        } else {
            pointsPer100 = 1; // Default value if no tier exists
        }

        //  calculate how many the number of hundreds (250 = 2)
        //  create a BigDecimal for 100 to use in division
        BigDecimal oneHundred = new BigDecimal("100");

        // divide and round down (ignore decimals) (250 = 2)
        BigDecimal hundredsCount = purchaseAmount.divide(oneHundred, 0, BigDecimal.ROUND_DOWN);

        //  convert to integer and multiply
        int numberOfHundreds = hundredsCount.intValue();
        int totalPoints = numberOfHundreds * pointsPer100;

        return totalPoints;
    }

    //loyalty calculation


}