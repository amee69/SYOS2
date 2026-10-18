package com.syos.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;



  //Tests: constructor, getters, isExpiringBefore, reduceQuantity

@DisplayName("StockBatch Entity Tests")
class StockBatchTest {

    private StockBatch batch;
    private LocalDate today;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        futureDate = today.plusMonths(3);
        batch = new StockBatch(1, "DRY-001-2026", today, futureDate, 100);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create batch with all fields set")
        void constructor_WithValidInputs_SetsAllFields() {
            // Assert
            assertEquals(1, batch.getBatchId());
            assertEquals("DRY-001-2026", batch.getItemCode());
            assertEquals(today, batch.getPurchaseDate());
            assertEquals(futureDate, batch.getExpiryDate());
            assertEquals(100, batch.getQuantity());
        }
    }

    @Nested
    @DisplayName("isExpiringBefore Tests")
    class IsExpiringBeforeTests {

        @Test
        @DisplayName("Should return true when this batch expires before other")
        void isExpiringBefore_WhenExpiresEarlier_ReturnsTrue() {
            // Arrange
            LocalDate earlierExpiry = today.plusMonths(1);
            LocalDate laterExpiry = today.plusMonths(3);

            StockBatch earlierBatch = new StockBatch(1, "DRY-001", today, earlierExpiry, 50);
            StockBatch laterBatch = new StockBatch(2, "DRY-001", today, laterExpiry, 50);

            // Act & Assert
            assertTrue(earlierBatch.isExpiringBefore(laterBatch));
        }

        @Test
        @DisplayName("Should return false when this batch expires after other")
        void isExpiringBefore_WhenExpiresLater_ReturnsFalse() {
            // Arrange
            LocalDate earlierExpiry = today.plusMonths(1);
            LocalDate laterExpiry = today.plusMonths(3);

            StockBatch earlierBatch = new StockBatch(1, "DRY-001", today, earlierExpiry, 50);
            StockBatch laterBatch = new StockBatch(2, "DRY-001", today, laterExpiry, 50);

            // Act & Assert
            assertFalse(laterBatch.isExpiringBefore(earlierBatch));
        }

        @Test
        @DisplayName("Should return false when expiry dates are equal")
        void isExpiringBefore_WhenSameExpiry_ReturnsFalse() {
            // Arrange
            LocalDate sameExpiry = today.plusMonths(2);

            StockBatch batch1 = new StockBatch(1, "DRY-001", today, sameExpiry, 50);
            StockBatch batch2 = new StockBatch(2, "DRY-001", today, sameExpiry, 50);

            // Act & Assert
            assertFalse(batch1.isExpiringBefore(batch2));
        }
    }

    @Nested
    @DisplayName("reduceQuantity Tests")
    class ReduceQuantityTests {

        @Test
        @DisplayName("Should reduce quantity by given amount")
        void reduceQuantity_WithValidAmount_ReducesQuantity() {
            // Arrange
            int initialQuantity = batch.getQuantity(); // 100

            // Act
            batch.reduceQuantity(30);

            // Assert
            assertEquals(initialQuantity - 30, batch.getQuantity());
        }

        @Test
        @DisplayName("Should reduce to zero when reducing full amount")
        void reduceQuantity_FullAmount_LeavesZero() {
            // Act
            batch.reduceQuantity(100);

            // Assert
            assertEquals(0, batch.getQuantity());
        }

        @Test
        @DisplayName("Should not reduce when amount exceeds quantity")
        void reduceQuantity_ExceedsQuantity_DoesNotReduce() {
            // Arrange
            int initialQuantity = batch.getQuantity(); // 100

            // Act
            batch.reduceQuantity(150);

            // Assert - quantity should remain unchanged
            assertEquals(initialQuantity, batch.getQuantity());
        }

        @Test
        @DisplayName("Should handle reducing by zero")
        void reduceQuantity_ByZero_QuantityUnchanged() {
            // Arrange
            int initialQuantity = batch.getQuantity();

            // Act
            batch.reduceQuantity(0);

            // Assert
            assertEquals(initialQuantity, batch.getQuantity());
        }

        @Test
        @DisplayName("Should allow multiple reductions")
        void reduceQuantity_MultipleTimes_AccumulatesReductions() {
            // Arrange - batch has 100

            // Act
            batch.reduceQuantity(20);
            batch.reduceQuantity(30);
            batch.reduceQuantity(25);

            // Assert: 100 - 20 - 30 - 25 = 25
            assertEquals(25, batch.getQuantity());
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getBatchId returns correct ID")
        void getBatchId_ReturnsCorrectValue() {
            assertEquals(1, batch.getBatchId());
        }

        @Test
        @DisplayName("getItemCode returns correct code")
        void getItemCode_ReturnsCorrectValue() {
            assertEquals("DRY-001-2026", batch.getItemCode());
        }

        @Test
        @DisplayName("getPurchaseDate returns correct date")
        void getPurchaseDate_ReturnsCorrectValue() {
            assertEquals(today, batch.getPurchaseDate());
        }

        @Test
        @DisplayName("getExpiryDate returns correct date")
        void getExpiryDate_ReturnsCorrectValue() {
            assertEquals(futureDate, batch.getExpiryDate());
        }

        @Test
        @DisplayName("getQuantity returns correct quantity")
        void getQuantity_ReturnsCorrectValue() {
            assertEquals(100, batch.getQuantity());
        }
    }
}
