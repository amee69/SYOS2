package com.syos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Insufficient Stock Exception Tests")
class InsufficientStockExceptionTest {

    @Test
    @DisplayName("Constructor should format the error message correctly")
    void constructor_FormatsMessage() {
        // Arrange
        String itemCode = "DRY-001";
        int requested = 10;
        int available = 2;

        // Act
        InsufficientStockException exception = new InsufficientStockException(itemCode, requested, available);

        // Assert
        String expectedMessage = "Insufficient stock for item 'DRY-001': requested 10, available 2";

        assertEquals(expectedMessage, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }
}