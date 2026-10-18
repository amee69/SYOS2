package com.syos.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


 // Tests: constructor, getters, total price calculation

@DisplayName("BillItem Entity Tests")
class BillItemTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create BillItem with all fields set correctly")
        void constructor_WithValidInputs_SetsAllFields() {
            // Arrange
            String itemCode = "DRY-001-2026";
            String itemName = "Basmati Rice 5kg";
            int quantity = 3;
            BigDecimal unitPrice = new BigDecimal("1250.00");

            // Act
            BillItem item = new BillItem(itemCode, itemName, quantity, unitPrice);

            // Assert
            assertEquals(itemCode, item.getItemCode());
            assertEquals(itemName, item.getItemName());
            assertEquals(quantity, item.getQuantity());
            assertEquals(unitPrice, item.getUnitPrice());
        }
    }

    @Nested
    @DisplayName("Total Price Calculation Tests")
    class TotalPriceTests {

        @Test
        @DisplayName("Should calculate total price as quantity × unit price")
        void getTotalPrice_WithValidInputs_CalculatesCorrectly() {
            // Arrange: 3 items × 100.00 = 300.00
            BillItem item = new BillItem("DRY-001", "Rice", 3, new BigDecimal("100.00"));

            // Act
            BigDecimal total = item.getTotalPrice();

            // Assert
            assertEquals(new BigDecimal("300.00"), total);
        }

        @Test
        @DisplayName("Should return unit price when quantity is 1")
        void getTotalPrice_WithQuantityOne_ReturnsUnitPrice() {
            // Arrange
            BigDecimal unitPrice = new BigDecimal("550.00");
            BillItem item = new BillItem("BEV-001", "Coca Cola", 1, unitPrice);

            // Act
            BigDecimal total = item.getTotalPrice();

            // Assert
            assertEquals(unitPrice, total);
        }

        @Test
        @DisplayName("Should return zero when quantity is zero")
        void getTotalPrice_WithZeroQuantity_ReturnsZero() {
            // Arrange
            BillItem item = new BillItem("DRY-001", "Rice", 0, new BigDecimal("100.00"));

            // Act
            BigDecimal total = item.getTotalPrice();

            // Assert
            assertEquals(new BigDecimal("0.00"), total);
        }

        @Test
        @DisplayName("Should handle large quantities correctly")
        void getTotalPrice_WithLargeQuantity_CalculatesCorrectly() {
            // Arrange: 500 items × 25.50 = 12750.00
            BillItem item = new BillItem("SNK-001", "Chips", 500, new BigDecimal("25.50"));

            // Act
            BigDecimal total = item.getTotalPrice();

            // Assert
            assertEquals(new BigDecimal("12750.00"), total);
        }

        @Test
        @DisplayName("Should handle decimal prices correctly")
        void getTotalPrice_WithDecimalPrice_CalculatesCorrectly() {
            // Arrange: 7 items × 33.33 = 233.31
            BillItem item = new BillItem("DRY-002", "Sugar", 7, new BigDecimal("33.33"));

            // Act
            BigDecimal total = item.getTotalPrice();

            // Assert
            assertEquals(new BigDecimal("233.31"), total);
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getItemCode should return correct item code")
        void getItemCode_ReturnsCorrectValue() {
            // Arrange
            BillItem item = new BillItem("BEV-002-2026", "Pepsi", 2, BigDecimal.TEN);

            // Act & Assert
            assertEquals("BEV-002-2026", item.getItemCode());
        }

        @Test
        @DisplayName("getItemName should return correct item name")
        void getItemName_ReturnsCorrectValue() {
            // Arrange
            BillItem item = new BillItem("DAI-001", "Full Cream Milk 1L", 5, BigDecimal.TEN);

            // Act & Assert
            assertEquals("Full Cream Milk 1L", item.getItemName());
        }

        @Test
        @DisplayName("getQuantity should return correct quantity")
        void getQuantity_ReturnsCorrectValue() {
            // Arrange
            BillItem item = new BillItem("DRY-001", "Rice", 42, BigDecimal.TEN);

            // Act & Assert
            assertEquals(42, item.getQuantity());
        }

        @Test
        @DisplayName("getUnitPrice should return correct unit price")
        void getUnitPrice_ReturnsCorrectValue() {
            // Arrange
            BigDecimal price = new BigDecimal("199.99");
            BillItem item = new BillItem("DRY-001", "Rice", 1, price);

            // Act & Assert
            assertEquals(price, item.getUnitPrice());
        }
    }
}
