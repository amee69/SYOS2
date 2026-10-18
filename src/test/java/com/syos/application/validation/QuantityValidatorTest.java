package com.syos.application.validation;

import com.syos.application.dto.BillItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


 //  tests for QuantityValidator (Chain of Responsibility Pattern).
 //  validate with positive, zero, negative, and exceeding quantities

@DisplayName("QuantityValidator (Chain of Responsibility) Tests")
class QuantityValidatorTest {

    private QuantityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new QuantityValidator();
    }

    @Nested
    @DisplayName("Valid Quantity Tests")
    class ValidQuantityTests {

        @Test
        @DisplayName("Should return null for positive quantity")
        void validate_PositiveQuantity_ReturnsNull() {
            // Arrange
            BillItemRequest request = new BillItemRequest("DRY-001", 5);

            // Act
            String error = validator.validate(request);

            // Assert
            assertNull(error, "Should return null for valid quantity");
        }

        @Test
        @DisplayName("Should return null for quantity of 1")
        void validate_QuantityOne_ReturnsNull() {
            // Arrange
            BillItemRequest request = new BillItemRequest("DRY-001", 1);

            // Act
            String error = validator.validate(request);

            // Assert
            assertNull(error);
        }

        @Test
        @DisplayName("Should return null for quantity at max limit")
        void validate_AtMaxLimit_ReturnsNull() {
            // Arrange: Max is 1000 according to QuantityValidator
            BillItemRequest request = new BillItemRequest("DRY-001", 1000);

            // Act
            String error = validator.validate(request);

            // Assert
            assertNull(error);
        }

        @Test
        @DisplayName("Should return null for typical quantity")
        void validate_TypicalQuantity_ReturnsNull() {
            // Arrange
            BillItemRequest request = new BillItemRequest("BEV-001", 10);

            // Act
            String error = validator.validate(request);

            // Assert
            assertNull(error);
        }
    }

    @Nested
    @DisplayName("Invalid Quantity - Zero and Negative Tests")
    class InvalidZeroNegativeTests {

        @Test
        @DisplayName("Should return error for zero quantity")
        void validate_ZeroQuantity_ReturnsError() {
            // Arrange
            BillItemRequest request = new BillItemRequest("DRY-001", 0);

            // Act
            String error = validator.validate(request);

            // Assert
            assertNotNull(error, "Should return error for zero quantity");
        }

        @Test
        @DisplayName("Should return error for negative quantity")
        void validate_NegativeQuantity_ReturnsError() {
            // Arrange
            BillItemRequest request = new BillItemRequest("DRY-001", -1);

            // Act
            String error = validator.validate(request);

            // Assert
            assertNotNull(error, "Should return error for negative quantity");
        }

        @Test
        @DisplayName("Should return error for large negative quantity")
        void validate_LargeNegativeQuantity_ReturnsError() {
            // Arrange
            BillItemRequest request = new BillItemRequest("DRY-001", -100);

            // Act
            String error = validator.validate(request);

            // Assert
            assertNotNull(error);
        }

        @Test
        @DisplayName("Error message should mention quantity")
        void validate_ZeroQuantity_ErrorMentionsQuantity() {
            // Arrange
            BillItemRequest request = new BillItemRequest("DRY-001", 0);

            // Act
            String error = validator.validate(request);

            // Assert
            assertTrue(error.toLowerCase().contains("quantity") || error.contains("0"));
        }
    }

    @Nested
    @DisplayName("Invalid Quantity - Exceeds Maximum Tests")
    class InvalidExceedsMaxTests {

        @Test
        @DisplayName("Should return error when quantity exceeds max")
        void validate_ExceedsMax_ReturnsError() {
            // Arrange: Max is 1000
            BillItemRequest request = new BillItemRequest("DRY-001", 1001);

            // Act
            String error = validator.validate(request);

            // Assert
            assertNotNull(error, "Should return error when exceeding max");
        }

        @Test
        @DisplayName("Error should mention max limit")
        void validate_ExceedsMax_ErrorMentionsLimit() {
            // Arrange
            BillItemRequest request = new BillItemRequest("DRY-001", 5000);

            // Act
            String error = validator.validate(request);

            // Assert
            assertTrue(error.contains("1000") || error.toLowerCase().contains("maximum"));
        }
    }

    @Nested
    @DisplayName("Chain Behavior Tests")
    class ChainBehaviorTests {

        @Test
        @DisplayName("Should pass to next handler when valid")
        void validate_ValidQuantity_PassesToNext() {
            // Arrange
            MockNextHandler mockNext = new MockNextHandler();
            validator.setNext(mockNext);
            BillItemRequest request = new BillItemRequest("DRY-001", 5);

            // Act
            validator.validate(request);

            // Assert
            assertTrue(mockNext.wasCalled, "Should call next handler");
        }

        @Test
        @DisplayName("Should not pass to next handler when invalid")
        void validate_InvalidQuantity_DoesNotPassToNext() {
            // Arrange
            MockNextHandler mockNext = new MockNextHandler();
            validator.setNext(mockNext);
            BillItemRequest request = new BillItemRequest("DRY-001", 0);

            // Act
            validator.validate(request);

            // Assert
            assertFalse(mockNext.wasCalled, "Should not call next handler on error");
        }
    }


      //mock handler to verify chain behavior

    private static class MockNextHandler extends ValidationHandler {
        boolean wasCalled = false;

        @Override
        public String validate(BillItemRequest request) {
            wasCalled = true;
            return null;
        }
    }
}
