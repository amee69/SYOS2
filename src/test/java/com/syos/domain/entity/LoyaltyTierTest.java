package com.syos.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


 //Unit tests for LoyaltyTier entity.

@DisplayName("LoyaltyTier Entity Tests")
class LoyaltyTierTest {

    @Test
    @DisplayName("Should create LoyaltyTier with all fields")
    void constructor_WithAllFields_CreatesCorrectly() {
        // Arrange
        String tierName = "GOLD";
        BigDecimal minPurchases = new BigDecimal("50000");
        int pointsPer100 = 3;
        BigDecimal discountPercent = new BigDecimal("10");

        // Act
        LoyaltyTier tier = new LoyaltyTier(tierName, minPurchases, pointsPer100, discountPercent);

        // Assert
        assertEquals("GOLD", tier.getTierName());
        assertEquals(new BigDecimal("50000"), tier.getMinPurchases());
        assertEquals(3, tier.getPointsPer100());
        assertEquals(new BigDecimal("10"), tier.getDiscountPercent());
    }

    @Test
    @DisplayName("Should return correct tier name")
    void getTierName_ReturnsCorrectValue() {
        // Arrange
        LoyaltyTier tier = new LoyaltyTier("SILVER", new BigDecimal("10000"), 2, new BigDecimal("5"));

        // Act
        String tierName = tier.getTierName();

        // Assert
        assertEquals("SILVER", tierName);
    }

    @Test
    @DisplayName("Should return correct min purchases")
    void getMinPurchases_ReturnsCorrectValue() {
        // Arrange
        LoyaltyTier tier = new LoyaltyTier("SILVER", new BigDecimal("10000"), 2, new BigDecimal("5"));

        // Act
        BigDecimal minPurchases = tier.getMinPurchases();

        // Assert
        assertEquals(new BigDecimal("10000"), minPurchases);
    }

    @Test
    @DisplayName("Should return correct points per 100")
    void getPointsPer100_ReturnsCorrectValue() {
        // Arrange
        LoyaltyTier tier = new LoyaltyTier("GOLD", new BigDecimal("50000"), 3, new BigDecimal("10"));

        // Act
        int pointsPer100 = tier.getPointsPer100();

        // Assert
        assertEquals(3, pointsPer100);
    }

    @Test
    @DisplayName("Should return correct discount percent")
    void getDiscountPercent_ReturnsCorrectValue() {
        // Arrange
        LoyaltyTier tier = new LoyaltyTier("GOLD", new BigDecimal("50000"), 3, new BigDecimal("10"));

        // Act
        BigDecimal discountPercent = tier.getDiscountPercent();

        // Assert
        assertEquals(new BigDecimal("10"), discountPercent);
    }

    @Test
    @DisplayName("Bronze tier should have zero discount")
    void bronzeTier_HasZeroDiscount() {
        // Arrange
        LoyaltyTier bronzeTier = new LoyaltyTier("BRONZE", new BigDecimal("0"), 1, new BigDecimal("0"));

        // Act & Assert
        assertEquals(1, bronzeTier.getPointsPer100());
        assertEquals(new BigDecimal("0"), bronzeTier.getDiscountPercent());
    }
}
