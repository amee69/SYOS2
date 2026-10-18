package com.syos.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

 //Tests: constructor, all getters

@DisplayName("Item Entity Tests")
class ItemTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create Item with all fields set correctly")
        void constructor_WithValidInputs_SetsAllFields() {
            // Arrange & Act
            Item item = new Item("DRY-001-2026", "Basmati Rice 5kg",
                    new BigDecimal("1250.00"), "DRY", 50);

            // Assert
            assertEquals("DRY-001-2026", item.getItemCode());
            assertEquals("Basmati Rice 5kg", item.getName());
            assertEquals(new BigDecimal("1250.00"), item.getUnitPrice());
            assertEquals("DRY", item.getCategory());
            assertEquals(50, item.getReorderLevel());
        }

        @Test
        @DisplayName("Should handle different reorder levels")
        void constructor_WithCustomReorderLevel_SetsCorrectly() {
            // Arrange & Act
            Item item = new Item("BEV-001-2026", "Coca Cola 500ml",
                    new BigDecimal("150.00"), "BEV", 100);

            // Assert
            assertEquals(100, item.getReorderLevel());
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        private final Item testItem = new Item("DAI-001-2026", "Full Cream Milk 1L",
                new BigDecimal("320.00"), "DAI", 75);

        @Test
        @DisplayName("getItemCode returns correct code")
        void getItemCode_ReturnsCorrectValue() {
            assertEquals("DAI-001-2026", testItem.getItemCode());
        }

        @Test
        @DisplayName("getName returns correct name")
        void getName_ReturnsCorrectValue() {
            assertEquals("Full Cream Milk 1L", testItem.getName());
        }

        @Test
        @DisplayName("getUnitPrice returns correct price")
        void getUnitPrice_ReturnsCorrectValue() {
            assertEquals(new BigDecimal("320.00"), testItem.getUnitPrice());
        }

        @Test
        @DisplayName("getCategory returns correct category")
        void getCategory_ReturnsCorrectValue() {
            assertEquals("DAI", testItem.getCategory());
        }

        @Test
        @DisplayName("getReorderLevel returns correct threshold")
        void getReorderLevel_ReturnsCorrectValue() {
            assertEquals(75, testItem.getReorderLevel());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle zero unit price")
        void constructor_WithZeroPrice_CreatesItem() {
            Item item = new Item("FREE-001", "Free Sample", BigDecimal.ZERO, "PROMO", 10);
            assertEquals(BigDecimal.ZERO, item.getUnitPrice());
        }

        @Test
        @DisplayName("Should handle zero reorder level")
        void constructor_WithZeroReorderLevel_CreatesItem() {
            Item item = new Item("LTD-001", "Limited Edition", new BigDecimal("999.99"), "LTD", 0);
            assertEquals(0, item.getReorderLevel());
        }

        @Test
        @DisplayName("Should handle high value items")
        void constructor_WithHighPrice_CreatesItem() {
            Item item = new Item("LUX-001", "Premium Watch", new BigDecimal("99999.99"), "LUX", 5);
            assertEquals(new BigDecimal("99999.99"), item.getUnitPrice());
        }
    }
}
