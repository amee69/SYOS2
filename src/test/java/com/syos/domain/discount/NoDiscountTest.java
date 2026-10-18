package com.syos.domain.discount;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;



 // Tests: calculateDiscount, getDiscountDescription

@DisplayName("NoDiscount (Decorator Base) Tests")
class NoDiscountTest {

    @Nested
    @DisplayName("calculateDiscount() Method Tests")
    class CalculateDiscountTests {

        @Test
        @DisplayName("Should return zero discount for any amount")
        void calculateDiscount_WithAnyAmount_ReturnsZero() {
            // Arrange
            NoDiscount noDiscount = new NoDiscount();

            // Act & Assert
            assertEquals(BigDecimal.ZERO, noDiscount.calculateDiscount(new BigDecimal("1000.00")));
        }

        @Test
        @DisplayName("Should return zero for zero amount")
        void calculateDiscount_WithZeroAmount_ReturnsZero() {
            // Arrange
            NoDiscount noDiscount = new NoDiscount();

            // Act & Assert
            assertEquals(BigDecimal.ZERO, noDiscount.calculateDiscount(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Should return zero for very small amount")
        void calculateDiscount_WithSmallAmount_ReturnsZero() {
            // Arrange
            NoDiscount noDiscount = new NoDiscount();

            // Act & Assert
            assertEquals(BigDecimal.ZERO, noDiscount.calculateDiscount(new BigDecimal("0.01")));
        }

        @Test
        @DisplayName("Should return zero for very large amount")
        void calculateDiscount_WithLargeAmount_ReturnsZero() {
            // Arrange
            NoDiscount noDiscount = new NoDiscount();

            // Act & Assert
            assertEquals(BigDecimal.ZERO, noDiscount.calculateDiscount(new BigDecimal("999999.99")));
        }
    }

    @Nested
    @DisplayName("getDiscountDescription() Method Tests")
    class DiscountDescriptionTests {

        @Test
        @DisplayName("Should return appropriate no-discount message")
        void getDiscountDescription_ReturnsNoDiscountMessage() {
            // Arrange
            NoDiscount noDiscount = new NoDiscount();

            // Act
            String description = noDiscount.getDiscountDescription();

            // Assert
            assertNotNull(description);
            assertTrue(description.toLowerCase().contains("no discount") ||
                    description.toLowerCase().contains("none") ||
                    description.isEmpty());
        }
    }
}
