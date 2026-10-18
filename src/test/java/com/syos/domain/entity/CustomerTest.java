package com.syos.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Customer Entity Tests (Simplified)")
class CustomerTest {

    private Customer customer;

    @BeforeEach
    void setUp() {
        // start with a blank canvas for every test
        customer = new Customer(1, "test@test.com", "User", "0777", "pass", 100, BigDecimal.ZERO, "BRONZE");
    }

    // loyalty poinrs

    @Test
    @DisplayName("Loyalty Points: Add, Accumulate, and Validate Redemption")
    void testLoyaltyLifecycle() {
        //  before State
        assertEquals(100, customer.getLoyaltyPoints());

        //  add Points
        customer.addLoyaltyPoints(50);
        assertEquals(150, customer.getLoyaltyPoints());

        //  check if redeemable
        assertTrue(customer.canRedeemPoints(150));
        assertFalse(customer.canRedeemPoints(151)); // Too much

        //  Redeem
        customer.redeemLoyaltyPoints(50);
        assertEquals(100, customer.getLoyaltyPoints());
    }

    @Test
    @DisplayName("Redemption: Should throw error if points insufficient")
    void testRedemptionFailure() {
        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            customer.redeemLoyaltyPoints(200); // Has 100, wants 200
        });
        assertTrue(e.getMessage().contains("100")); // Check message contains current balance
    }

    // ==================== TIER DISCOUNTS ====================

    @Test
    @DisplayName("Tier Discounts: Bronze (0%), Silver (5%), Gold (10%)")
    void testTierDiscounts() {
        //  bornze Check
        LoyaltyTier bronze = new LoyaltyTier("BRONZE", BigDecimal.ZERO, 1, new BigDecimal("0"));
        assertEquals(0, customer.getTierDiscountPercent(bronze).intValue());

        //  silver Check
        LoyaltyTier silver = new LoyaltyTier("SILVER", BigDecimal.ZERO, 2, new BigDecimal("5"));
        // temporarily upgrade customer for test
        customer.updateTier("SILVER");
        assertEquals(5, customer.getTierDiscountPercent(silver).intValue());

        //  gold Check
        LoyaltyTier gold = new LoyaltyTier("GOLD", BigDecimal.ZERO, 3, new BigDecimal("10"));
        customer.updateTier("GOLD");
        assertEquals(10, customer.getTierDiscountPercent(gold).intValue());
    }

    // points earning calculations

    @Test
    @DisplayName("Points Earning: Calculates correctly based on Tier multiplier")
    void testPointsCalculation() {
        BigDecimal spend500 = new BigDecimal("500");

        //  bronze (1x)
        LoyaltyTier bronze = new LoyaltyTier("BRONZE", BigDecimal.ZERO, 1, BigDecimal.ZERO);
        assertEquals(5, customer.calculatePointsEarned(spend500, bronze));

        //  silver (2x)
        LoyaltyTier silver = new LoyaltyTier("SILVER", BigDecimal.ZERO, 2, BigDecimal.ZERO);
        assertEquals(10, customer.calculatePointsEarned(spend500, silver));

        //  gold (3x)
        LoyaltyTier gold = new LoyaltyTier("GOLD", BigDecimal.ZERO, 3, BigDecimal.ZERO);
        assertEquals(15, customer.calculatePointsEarned(spend500, gold));
    }

    @Test
    @DisplayName("Points Earning: Rounds down and handles small amounts")
    void testPointsEdgeCases() {
        LoyaltyTier standard = new LoyaltyTier("BRONZE", BigDecimal.ZERO, 1, BigDecimal.ZERO);

        // 150 LKR -> 1 point
        assertEquals(1, customer.calculatePointsEarned(new BigDecimal("150"), standard));

        // 99 LKR -> 0 points
        assertEquals(0, customer.calculatePointsEarned(new BigDecimal("99"), standard));

        // Null Tier -> Default 1x
        assertEquals(5, customer.calculatePointsEarned(new BigDecimal("500"), null));
    }

    //purchases and updates

    @Test
    @DisplayName("Purchase Tracking: Accumulates total")
    void testPurchaseTracking() {
        customer.addPurchase(new BigDecimal("100"));
        customer.addPurchase(new BigDecimal("200"));

        assertEquals(new BigDecimal("300"), customer.getTotalPurchases());
    }

    @Test
    @DisplayName("Update Tier: Changes the status correctly")
    void testUpdateTier() {
        customer.updateTier("GOLD");
        assertEquals("GOLD", customer.getCustomerTier());
    }
}