package com.syos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Item Not Found Exception Tests")
class ItemNotFoundExceptionTest {

    @Test
    @DisplayName("Constructor with item code should set default message")
    void constructor_WithItemCode_SetsDefaultMessage() {
        // Act
        String code = "ABC-123";
        ItemNotFoundException exception = new ItemNotFoundException(code);

        // Assert
        assertEquals(code, exception.getItemCode());
        assertEquals("Item not found with code: ABC-123", exception.getMessage());
    }

    @Test
    @DisplayName("Constructor with custom message should set message and code")
    void constructor_WithCustomMessage_SetsValues() {
        // Act
        String msg = "Custom error occurred";
        String code = "XYZ-999";
        ItemNotFoundException exception = new ItemNotFoundException(msg, code);

        // Assert
        assertEquals(code, exception.getItemCode());
        assertEquals(msg, exception.getMessage());
    }
}