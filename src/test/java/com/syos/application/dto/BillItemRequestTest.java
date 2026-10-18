package com.syos.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Unit tests for BillItemRequest DTO.
  //checking constructor, getters and validation.

@DisplayName("BillItemRequest DTO Tests")
class BillItemRequestTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create request with item code and quantity")
        void constructor_WithValidInputs_CreatesRequest() {
            // make object
            BillItemRequest request = new BillItemRequest("DRY-001-2026", 5);

            // check if save correct
            assertEquals("DRY-001-2026", request.getItemCode());
            assertEquals(5, request.getQuantity());
        }

        @Test
        @DisplayName("Should trim whitespace from item code")
        void constructor_WithSpaces_TrimsCode() {
            // send code with spaces
            BillItemRequest request = new BillItemRequest("  BEV-001  ", 3);

            // check if space removed
            assertEquals("BEV-001", request.getItemCode());
        }
    }

    @Nested
    @DisplayName("Validation Error Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw error if code is null")
        void constructor_NullCode_ThrowsException() {
            // try make null, expect crash
            Exception e = assertThrows(IllegalArgumentException.class, () ->
                    new BillItemRequest(null, 5)
            );
            // check message say null
            assertTrue(e.getMessage().contains("null"));
        }

        @Test
        @DisplayName("Should throw error if code is empty")
        void constructor_EmptyCode_ThrowsException() {
            // try make empty string
            Exception e = assertThrows(IllegalArgumentException.class, () ->
                    new BillItemRequest("", 5)
            );
            // check message
            assertTrue(e.getMessage().contains("empty"));
        }

        @Test
        @DisplayName("Should throw error if code is just spaces")
        void constructor_BlankCode_ThrowsException() {
            // try make only space
            assertThrows(IllegalArgumentException.class, () ->
                    new BillItemRequest("   ", 5)
            );
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getItemCode returns correct value")
        void getItemCode_ReturnsCorrectValue() {
            BillItemRequest request = new BillItemRequest("SNK-001", 7);
            assertEquals("SNK-001", request.getItemCode());
        }

        @Test
        @DisplayName("getQuantity returns correct value")
        void getQuantity_ReturnsCorrectValue() {
            BillItemRequest request = new BillItemRequest("DRY-001", 42);
            assertEquals(42, request.getQuantity());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString format check")
        void toString_ReturnsCorrectFormat() {
            // make object
            BillItemRequest request = new BillItemRequest("ABC-123", 10);

            // get string
            String result = request.toString();

            // check if string have data inside
            assertTrue(result.contains("ABC-123"));
            assertTrue(result.contains("10"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle quantity of 1")
        void constructor_QuantityOne_Works() {
            BillItemRequest request = new BillItemRequest("DRY-001", 1);
            assertEquals(1, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle large quantity")
        void constructor_LargeQuantity_Works() {
            BillItemRequest request = new BillItemRequest("DRY-001", 999);
            assertEquals(999, request.getQuantity());
        }

        @Test
        @DisplayName("Should handle zero quantity (validation elsewhere)")
        void constructor_ZeroQuantity_Creates() {
            // logic check done by validator service, not here
            BillItemRequest request = new BillItemRequest("DRY-001", 0);
            assertEquals(0, request.getQuantity());
        }
    }
}