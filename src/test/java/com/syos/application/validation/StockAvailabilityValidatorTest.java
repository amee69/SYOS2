package com.syos.application.validation;

import com.syos.application.dto.BillItemRequest;
import com.syos.domain.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


  //Unit tests for StockAvailabilityValidator (Chain of Responsibility Pattern).


@ExtendWith(MockitoExtension.class)
@DisplayName("StockAvailabilityValidator (Chain of Responsibility) Tests")
class StockAvailabilityValidatorTest {

    @Mock
    private StockRepository stockRepository;

    private StockAvailabilityValidator shelfValidator;
    private StockAvailabilityValidator webValidator;

    @BeforeEach
    void setUp() {
        shelfValidator = new StockAvailabilityValidator(stockRepository, "SHELF");
        webValidator = new StockAvailabilityValidator(stockRepository, "WEB");
    }

    @Nested
    @DisplayName("Sufficient Stock Tests")
    class SufficientStockTests {

        @Test
        @DisplayName("Should return null when stock is sufficient")
        void validate_SufficientStock_ReturnsNull() {
            // Arrange
            String itemCode = "DRY-001";
            when(stockRepository.getQuantityByLocation(itemCode, "SHELF")).thenReturn(100);
            BillItemRequest request = new BillItemRequest(itemCode, 50);

            // Act
            String error = shelfValidator.validate(request);

            // Assert
            assertNull(error, "Should return null when stock is sufficient");
        }

        @Test
        @DisplayName("Should return null when stock equals requested")
        void validate_ExactStock_ReturnsNull() {
            // Arrange
            String itemCode = "DRY-001";
            when(stockRepository.getQuantityByLocation(itemCode, "SHELF")).thenReturn(50);
            BillItemRequest request = new BillItemRequest(itemCode, 50);

            // Act
            String error = shelfValidator.validate(request);

            // Assert
            assertNull(error, "Should return null when stock equals requested");
        }

        @Test
        @DisplayName("Should return null when stock far exceeds requested")
        void validate_ExcessStock_ReturnsNull() {
            // Arrange
            when(stockRepository.getQuantityByLocation("BEV-001", "SHELF")).thenReturn(1000);
            BillItemRequest request = new BillItemRequest("BEV-001", 5);

            // Act
            String error = shelfValidator.validate(request);

            // Assert
            assertNull(error);
        }
    }

    @Nested
    @DisplayName("Insufficient Stock Tests")
    class InsufficientStockTests {

        @Test
        @DisplayName("Should return error when stock is insufficient")
        void validate_InsufficientStock_ReturnsError() {
            // Arrange
            String itemCode = "DRY-001";
            when(stockRepository.getQuantityByLocation(itemCode, "SHELF")).thenReturn(10);
            BillItemRequest request = new BillItemRequest(itemCode, 50);

            // Act
            String error = shelfValidator.validate(request);

            // Assert
            assertNotNull(error, "Should return error when stock is insufficient");
        }

        @Test
        @DisplayName("Should return error when stock is zero")
        void validate_ZeroStock_ReturnsError() {
            // Arrange
            when(stockRepository.getQuantityByLocation("DRY-001", "SHELF")).thenReturn(0);
            BillItemRequest request = new BillItemRequest("DRY-001", 1);

            // Act
            String error = shelfValidator.validate(request);

            // Assert
            assertNotNull(error, "Should return error when stock is zero");
        }

        @Test
        @DisplayName("Error should mention insufficient or stock")
        void validate_InsufficientStock_ErrorMentionsStock() {
            // Arrange
            when(stockRepository.getQuantityByLocation("DRY-001", "SHELF")).thenReturn(5);
            BillItemRequest request = new BillItemRequest("DRY-001", 10);

            // Act
            String error = shelfValidator.validate(request);

            // Assert
            assertTrue(error.toLowerCase().contains("insufficient") ||
                    error.toLowerCase().contains("stock"));
        }

        @Test
        @DisplayName("Error should contain requested and available quantities")
        void validate_InsufficientStock_ErrorContainsQuantities() {
            // Arrange
            when(stockRepository.getQuantityByLocation("DRY-001", "SHELF")).thenReturn(5);
            BillItemRequest request = new BillItemRequest("DRY-001", 10);

            // Act
            String error = shelfValidator.validate(request);

            // Assert
            assertTrue(error.contains("10") || error.contains("5"));
        }
    }

    @Nested
    @DisplayName("Stock Type Tests")
    class StockTypeTests {

        @Test
        @DisplayName("Should check SHELF stock type for shelf validator")
        void validate_ShelfValidator_ChecksShelfStock() {
            // Arrange
            when(stockRepository.getQuantityByLocation("DRY-001", "SHELF")).thenReturn(100);
            BillItemRequest request = new BillItemRequest("DRY-001", 5);

            // Act
            shelfValidator.validate(request);

            // Assert
            verify(stockRepository).getQuantityByLocation("DRY-001", "SHELF");
        }

        @Test
        @DisplayName("Should check WEB stock type for web validator")
        void validate_WebValidator_ChecksWebStock() {
            // Arrange
            when(stockRepository.getQuantityByLocation("DRY-001", "WEB")).thenReturn(100);
            BillItemRequest request = new BillItemRequest("DRY-001", 5);

            // Act
            webValidator.validate(request);

            // Assert
            verify(stockRepository).getQuantityByLocation("DRY-001", "WEB");
        }

        @Test
        @DisplayName("Shelf and Web validators should check different locations")
        void validate_DifferentValidators_CheckDifferentLocations() {
            // Arrange
            when(stockRepository.getQuantityByLocation("DRY-001", "SHELF")).thenReturn(100);
            when(stockRepository.getQuantityByLocation("DRY-001", "WEB")).thenReturn(50);
            BillItemRequest request = new BillItemRequest("DRY-001", 5);

            // Act
            shelfValidator.validate(request);
            webValidator.validate(request);

            // Assert
            verify(stockRepository).getQuantityByLocation("DRY-001", "SHELF");
            verify(stockRepository).getQuantityByLocation("DRY-001", "WEB");
        }
    }

    @Nested
    @DisplayName("Chain Behavior Tests")
    class ChainBehaviorTests {

        @Test
        @DisplayName("Should pass to next handler when stock is sufficient")
        void validate_SufficientStock_PassesToNext() {
            // Arrange
            when(stockRepository.getQuantityByLocation("DRY-001", "SHELF")).thenReturn(100);
            MockNextHandler mockNext = new MockNextHandler();
            shelfValidator.setNext(mockNext);
            BillItemRequest request = new BillItemRequest("DRY-001", 5);

            // Act
            shelfValidator.validate(request);

            // Assert
            assertTrue(mockNext.wasCalled, "Should call next handler");
        }

        @Test
        @DisplayName("Should not pass to next handler when stock is insufficient")
        void validate_InsufficientStock_DoesNotPassToNext() {
            // Arrange
            when(stockRepository.getQuantityByLocation("DRY-001", "SHELF")).thenReturn(5);
            MockNextHandler mockNext = new MockNextHandler();
            shelfValidator.setNext(mockNext);
            BillItemRequest request = new BillItemRequest("DRY-001", 100);

            // Act
            shelfValidator.validate(request);

            // Assert
            assertFalse(mockNext.wasCalled, "Should not call next handler on error");
        }
    }


     // Mock handler to verify chain behavior

    private static class MockNextHandler extends ValidationHandler {
        boolean wasCalled = false;

        @Override
        public String validate(BillItemRequest request) {
            wasCalled = true;
            return null;
        }
    }
}
