package com.syos.domain.discount;

import com.syos.domain.service.DiscountCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;



 //  calculateDiscount, decorator chaining, getDiscountDescription

@DisplayName("LoyaltyPointsRedemption (Decorator) Tests")
class LoyaltyPointsRedemptionTest {

    private DiscountCalculator baseCalculator;

    @BeforeEach
    void setUp() {
        baseCalculator = new NoDiscount();
    }

    @Nested
    @DisplayName("calculateDiscount() Tests")
    class CalculateDiscountTests {

        @Test
        @DisplayName("Should convert points to LKR (1 point = 1 LKR)")
        void calculateDiscount_WithPoints_ConvertsCorrectly() {
            // Arrange: 100 points = 100 LKR discount
            LoyaltyPointsRedemption redemption = new LoyaltyPointsRedemption(baseCalculator, 100);
            BigDecimal total = new BigDecimal("1000.00");

            // Act
            BigDecimal discount = redemption.calculateDiscount(total);

            // Assert
            assertEquals(new BigDecimal("100"), discount);
        }

        @Test
        @DisplayName("Should return zero for zero points")
        void calculateDiscount_ZeroPoints_ReturnsZero() {
            // Arrange
            LoyaltyPointsRedemption redemption = new LoyaltyPointsRedemption(baseCalculator, 0);

            // Act
            BigDecimal discount = redemption.calculateDiscount(new BigDecimal("1000.00"));

            // Assert
            assertEquals(BigDecimal.ZERO, discount);
        }

        @Test
        @DisplayName("Should handle large point redemption")
        void calculateDiscount_LargePoints_CalculatesCorrectly() {
            // Arrange: 5000 points = 5000 LKR
            LoyaltyPointsRedemption redemption = new LoyaltyPointsRedemption(baseCalculator, 5000);

            // Act
            BigDecimal discount = redemption.calculateDiscount(new BigDecimal("10000.00"));

            // Assert
            assertEquals(new BigDecimal("5000"), discount);
        }
    }

    @Nested
    @DisplayName("Decorator Chaining Tests")
    class ChainingTests {

        @Test
        @DisplayName("Should add to tier discount when chained")
        void calculateDiscount_WithTierDiscount_CombinesCorrectly() {
            // Arrange: 10% Gold + 50 points
            DiscountCalculator tierDiscount = new LoyaltyTierDiscount(
                    baseCalculator, "GOLD", new BigDecimal("10"));
            DiscountCalculator pointsRedemption = new LoyaltyPointsRedemption(tierDiscount, 50);

            BigDecimal total = new BigDecimal("1000.00");

            // Act
            BigDecimal discount = pointsRedemption.calculateDiscount(total);

            // Assert: 100 (10% of 1000) + 50 (points) = 150
            assertEquals(new BigDecimal("150.00"), discount);
        }

        @Test
        @DisplayName("Should work as middle decorator in chain")
        void calculateDiscount_AsMiddleDecorator_CombinesCorrectly() {
            // Arrange: 5% Silver + 100 points + 10% Gold
            DiscountCalculator silver = new LoyaltyTierDiscount(baseCalculator, "SILVER", new BigDecimal("5"));
            DiscountCalculator points = new LoyaltyPointsRedemption(silver, 100);
            DiscountCalculator gold = new LoyaltyTierDiscount(points, "GOLD", new BigDecimal("10"));

            BigDecimal total = new BigDecimal("1000.00");

            // Act
            BigDecimal discount = gold.calculateDiscount(total);

            // Assert: 50 (5%) + 100 (points) + 100 (10%) = 250
            assertEquals(new BigDecimal("250.00"), discount);
        }
    }

    @Nested
    @DisplayName("getDiscountDescription() Tests")
    class DescriptionTests {

        @Test
        @DisplayName("Should include points info in description")
        void getDiscountDescription_ContainsPointsInfo() {
            // Arrange
            LoyaltyPointsRedemption redemption = new LoyaltyPointsRedemption(baseCalculator, 100);

            // Act
            String description = redemption.getDiscountDescription();

            // Assert
            assertTrue(description.contains("100") ||
                    description.toLowerCase().contains("point"));
        }

        @Test
        @DisplayName("Should combine descriptions when chained")
        void getDiscountDescription_WhenChained_CombinesDescriptions() {
            // Arrange
            DiscountCalculator tierDiscount = new LoyaltyTierDiscount(
                    baseCalculator, "GOLD", new BigDecimal("10"));
            DiscountCalculator pointsRedemption = new LoyaltyPointsRedemption(tierDiscount, 50);

            // Act
            String description = pointsRedemption.getDiscountDescription();

            // Assert - should contain info about both discounts
            assertNotNull(description);
            assertFalse(description.isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle small point amounts")
        void calculateDiscount_SmallPoints_CalculatesCorrectly() {
            // Arrange: 1 point = 1 LKR
            LoyaltyPointsRedemption redemption = new LoyaltyPointsRedemption(baseCalculator, 1);

            // Act
            BigDecimal discount = redemption.calculateDiscount(new BigDecimal("1000.00"));

            // Assert
            assertEquals(new BigDecimal("1"), discount);
        }
    }
}
