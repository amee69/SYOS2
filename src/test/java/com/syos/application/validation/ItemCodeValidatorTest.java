package com.syos.application.validation;

import com.syos.application.dto.BillItemRequest;
import com.syos.domain.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


 //Unit tests for ItemCodeValidator (Chain of Responsibility Pattern).

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemCodeValidator (Chain of Responsibility) Tests")
class ItemCodeValidatorTest {

    @Mock
    private ItemRepository itemRepository;


    private ItemCodeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ItemCodeValidator(itemRepository);
    }

    @Nested
    @DisplayName("Valid Item Code Tests")
    class ValidItemCodeTests {

        @Test
        @DisplayName("Should return null when item exists")
        void validate_ItemExists_ReturnsNull() {
            // Arrange
            String itemCode = "DRY-001-2026";
            when(itemRepository.existsByCode(itemCode)).thenReturn(true);
            BillItemRequest request = new BillItemRequest(itemCode, 5);

            // Act
            String error = validator.validate(request);

            // Assert
            assertNull(error, "Should return null for existing item");
            verify(itemRepository).existsByCode(itemCode);
        }

        @Test
        @DisplayName("Should check repository exactly once")
        void validate_ItemExists_CallsRepositoryOnce() {
            // Arrange
            when(itemRepository.existsByCode(anyString())).thenReturn(true);
            BillItemRequest request = new BillItemRequest("BEV-001", 3);

            // Act
            validator.validate(request);

            // Assert
            verify(itemRepository, times(1)).existsByCode("BEV-001");
        }
    }

    @Nested
    @DisplayName("Invalid Item Code Tests")
    class InvalidItemCodeTests {

        @Test
        @DisplayName("Should return error when item does not exist")
        void validate_ItemNotExists_ReturnsError() {
            // Arrange
            String itemCode = "INVALID-001";
            when(itemRepository.existsByCode(itemCode)).thenReturn(false);
            BillItemRequest request = new BillItemRequest(itemCode, 5);

            // Act
            String error = validator.validate(request);

            // Assert
            assertNotNull(error, "Should return error for non-existent item");
        }

        @Test
        @DisplayName("Error message should contain item code")
        void validate_ItemNotExists_ErrorContainsItemCode() {
            // Arrange
            String itemCode = "UNKNOWN-123";
            when(itemRepository.existsByCode(itemCode)).thenReturn(false);
            BillItemRequest request = new BillItemRequest(itemCode, 5);

            // Act
            String error = validator.validate(request);

            // Assert
            assertTrue(error.contains(itemCode) || error.toLowerCase().contains("not found"));
        }
    }

    @Nested
    @DisplayName("Chain Behavior Tests")
    class ChainBehaviorTests {

        @Test
        @DisplayName("Should pass to next handler when item exists")
        void validate_ItemExists_PassesToNext() {
            // Arrange
            when(itemRepository.existsByCode("DRY-001")).thenReturn(true);
            MockNextHandler mockNext = new MockNextHandler();
            validator.setNext(mockNext);
            BillItemRequest request = new BillItemRequest("DRY-001", 5);

            // Act
            validator.validate(request);

            // Assert
            assertTrue(mockNext.wasCalled, "Should call next handler");
        }

        @Test
        @DisplayName("Should not pass to next handler when item doesn't exist")
        void validate_ItemNotExists_DoesNotPassToNext() {
            // Arrange
            when(itemRepository.existsByCode("INVALID-001")).thenReturn(false);
            MockNextHandler mockNext = new MockNextHandler();
            validator.setNext(mockNext);
            BillItemRequest request = new BillItemRequest("INVALID-001", 5);

            // Act
            validator.validate(request);

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
