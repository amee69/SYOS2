package com.syos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment Exception Tests")
class PaymentExceptionTest {

    @Test
    @DisplayName("Constructor with message should set message and leave amounts null")
    void constructor_WithMessage_SetsDetails() {
        // Act
        PaymentException exception = new PaymentException("Payment service down");

        // Assert
        assertEquals("Payment service down", exception.getMessage());
        assertNull(exception.getShortfall(), "Shortfall should be null for message-only constructor");
    }

    @Test
    @DisplayName("Constructor with amounts should format message correctly")
    void constructor_WithAmounts_FormatsMessage() {
        // Arrange
        BigDecimal required = new BigDecimal("1000.00");
        BigDecimal provided = new BigDecimal("800.00");

        // Act
        PaymentException exception = new PaymentException(required, provided);

        // Assert
        // We verify the string format used: "required LKR %.2f, provided LKR %.2f"
        String expectedMsg = "Insufficient payment: required LKR 1000.00, provided LKR 800.00";
        assertEquals(expectedMsg, exception.getMessage());
    }

    @Test
    @DisplayName("getShortfall should calculate the difference correctly")
    void getShortfall_CalculatesDifference() {
        // Arrange
        BigDecimal required = new BigDecimal("100.00");
        BigDecimal provided = new BigDecimal("80.00");
        PaymentException exception = new PaymentException(required, provided);

        // Act
        BigDecimal shortfall = exception.getShortfall();

        // Assert
        // 100 - 80 = 20
        assertEquals(new BigDecimal("20.00"), shortfall);
    }
}