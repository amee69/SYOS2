package com.syos.domain.discount;

import com.syos.domain.service.DiscountCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;


// Tests: calculateDiscount with different tiers, decorator chaining, getDiscountDescription

@DisplayName("LoyaltyTierDiscount (Decorator) Tests")
class LoyaltyTierDiscountTest {

    private DiscountCalculator baseCalculator;

    @BeforeEach
    void setUp() {
        baseCalculator = new NoDiscount();
    }


    @Nested
    @DisplayName("calculateDiscount() for Gold Tier (10%)")
    class GoldTierTests {

        @Test
        @DisplayName("Should calculate 10% discount for Gold tier")
        void calculateDiscount_GoldTier_Returns10Percent() {
            // Arrange
            LoyaltyTierDiscount goldDiscount = new LoyaltyTierDiscount(baseCalculator, "GOLD", new BigDecimal("10"));
            BigDecimal total = new BigDecimal("1000.00");

            // Act
            BigDecimal discount = goldDiscount.calculateDiscount(total);

            // Assert: 10% of 1000 = 100
            assertEquals(new BigDecimal("100.00"), discount);
        }

        @Test
        @DisplayName("Should handle decimal results for Gold tier")
        void calculateDiscount_GoldTierDecimal_RoundsCorrectly() {
            // Arrange
            LoyaltyTierDiscount goldDiscount = new LoyaltyTierDiscount(
                    baseCalculator, "GOLD", new BigDecimal("10"));
            BigDecimal total = new BigDecimal("333.33");

            // Act
            BigDecimal discount = goldDiscount.calculateDiscount(total);

            // Assert: 10% of 333.33 = 33.33 (rounded)
            assertEquals(new BigDecimal("33.33"), discount.setScale(2, RoundingMode.HALF_UP));
        }
    }

    @Nested
    @DisplayName("calculateDiscount() for Silver Tier (5%)")
    class SilverTierTests {

        @Test
        @DisplayName("Should calculate 5% discount for Silver tier")
        void calculateDiscount_SilverTier_Returns5Percent() {
            // Arrange
            LoyaltyTierDiscount silverDiscount = new LoyaltyTierDiscount(
                    baseCalculator, "SILVER", new BigDecimal("5"));
            BigDecimal total = new BigDecimal("1000.00");

            // Act
            BigDecimal discount = silverDiscount.calculateDiscount(total);

            // Assert: 5% of 1000 = 50
            assertEquals(new BigDecimal("50.00"), discount);
        }
    }

    @Nested
    @DisplayName("calculateDiscount() for Bronze Tier (0%)")
    class BronzeTierTests {

        @Test
        @DisplayName("Should return zero discount for Bronze tier")
        void calculateDiscount_BronzeTier_ReturnsZero() {
            // Arrange
            LoyaltyTierDiscount bronzeDiscount = new LoyaltyTierDiscount(
                    baseCalculator, "BRONZE", BigDecimal.ZERO);
            BigDecimal total = new BigDecimal("1000.00");

            // Act
            BigDecimal discount = bronzeDiscount.calculateDiscount(total);

            // Assert
            assertEquals(BigDecimal.ZERO, discount);
        }
    }

    @Nested
    @DisplayName("Decorator Chaining Tests")
    class DecoratorChainingTests {

        @Test
        @DisplayName("Should add discounts when chained")
        void calculateDiscount_WhenChained_AddsDiscounts() {
            // Arrange: First 5% (Silver), then 10% (Gold) = 15% total
            DiscountCalculator silverDiscount = new LoyaltyTierDiscount(baseCalculator, "SILVER", new BigDecimal("5"));
            DiscountCalculator goldDiscount = new LoyaltyTierDiscount(silverDiscount, "GOLD", new BigDecimal("10"));

            BigDecimal total = new BigDecimal("1000.00");

            // Act
            BigDecimal discount = goldDiscount.calculateDiscount(total);

            // Assert: 5% + 10% of 1000 = 150
            assertEquals(new BigDecimal("150.00"), discount);
        }
    }

    @Nested
    @DisplayName("getDiscountDescription() Tests")
    class DescriptionTests {

        @Test
        @DisplayName("Should include tier name in description")
        void getDiscountDescription_ContainsTierName() {
            // Arrange
            LoyaltyTierDiscount goldDiscount = new LoyaltyTierDiscount(
                    baseCalculator, "GOLD", new BigDecimal("10"));

            // Act
            String description = goldDiscount.getDiscountDescription();

            // Assert
            assertTrue(description.toUpperCase().contains("GOLD"));
        }

        @Test
        @DisplayName("Should include percentage in description")
        void getDiscountDescription_ContainsPercentage() {
            // Arrange
            LoyaltyTierDiscount silverDiscount = new LoyaltyTierDiscount(
                    baseCalculator, "SILVER", new BigDecimal("5"));

            // Act
            String description = silverDiscount.getDiscountDescription();

            // Assert
            assertTrue(description.contains("5") || description.contains("five"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle zero total amount")
        void calculateDiscount_ZeroAmount_ReturnsZero() {
            // Arrange
            LoyaltyTierDiscount discount = new LoyaltyTierDiscount(
                    baseCalculator, "GOLD", new BigDecimal("10"));

            // Act
            BigDecimal result = discount.calculateDiscount(BigDecimal.ZERO);

            // Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(result));
        }

        @Test
        @DisplayName("Should handle very large amounts")
        void calculateDiscount_LargeAmount_CalculatesCorrectly() {
            // Arrange
            LoyaltyTierDiscount discount = new LoyaltyTierDiscount(
                    baseCalculator, "GOLD", new BigDecimal("10"));
            BigDecimal largeAmount = new BigDecimal("1000000.00");

            // Act
            BigDecimal result = discount.calculateDiscount(largeAmount);

            // Assert: 10% of 1,000,000 = 100,000
            assertEquals(new BigDecimal("100000.00"), result);
        }


        @Test
        @DisplayName("Should return original description if discount is zero (e.g. Bronze)")
        void getDiscountDescription_ZeroPercent_ReturnsWrappedDescription() {
            // Arrange
            // Base calculator returns "No discount applied"
            LoyaltyTierDiscount bronzeDiscount = new LoyaltyTierDiscount(
                    baseCalculator, "BRONZE", BigDecimal.ZERO);

            // Act
            String description = bronzeDiscount.getDiscountDescription();

            // Assert
            // It should not say "BRONZE Member 0%", it should just pass through the base description
            assertEquals("No discount applied", description);
        }

        @Test
        @DisplayName("Should replace 'No discount applied' completely for first valid discount")
        void getDiscountDescription_FirstValid_ReplacesBaseText() {
            // Arrange
            LoyaltyTierDiscount goldDiscount = new LoyaltyTierDiscount(
                    baseCalculator, "GOLD", new BigDecimal("10"));

            // Act
            String description = goldDiscount.getDiscountDescription();

            // Assert
            // It should be exactly this, NOT "No discount applied + GOLD..."
            assertEquals("GOLD Member 10%", description);
        }

        @Test
        @DisplayName("Should append with ' + ' when chaining valid discounts")
        void getDiscountDescription_Chained_AppendsText() {
            // Arrange
            // Create Silver (First layer)
            LoyaltyTierDiscount silverDiscount = new LoyaltyTierDiscount(
                    baseCalculator, "SILVER", new BigDecimal("5"));

            //  Wrap it with Gold (Second layer)
            LoyaltyTierDiscount combined = new LoyaltyTierDiscount(
                    silverDiscount, "GOLD", new BigDecimal("10"));

            // Act
            String description = combined.getDiscountDescription();

            // Assert
            // Check for (+) joining logic
            assertEquals("SILVER Member 5% + GOLD Member 10%", description);
        }
    }
}
