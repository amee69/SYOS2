package com.syos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Invalid Quantity Exception Tests")
class InvalidQuantityExceptionTest {

    @Test
    @DisplayName("Constructor with quantity should set default message and quantity")
    void constructor_WithQuantity_SetsValues() {
        // Act
        InvalidQuantityException exception = new InvalidQuantityException(-5);

        // Assert
        assertEquals(-5, exception.getQuantity());
        assertTrue(exception.getMessage().contains("-5"));
        assertTrue(exception.getMessage().contains("Invalid quantity"));
    }

    @Test
    @DisplayName("Constructor with message should set custom message and default quantity to 0")
    void constructor_WithMessage_SetsMessage() {
        // Act
        String customMsg = "Quantity cannot be zero";
        InvalidQuantityException exception = new InvalidQuantityException(customMsg);

        // Assert
        assertEquals(customMsg, exception.getMessage());
        assertEquals(0, exception.getQuantity());
    }
}