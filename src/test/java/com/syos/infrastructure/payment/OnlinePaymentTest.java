package com.syos.infrastructure.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


  //Unit tests for OnlinePayment (Strategy Pattern).
  //Tests: process, getPaymentType, getCashTendered, getChangeDue

@DisplayName("OnlinePayment Strategy Tests")
class OnlinePaymentTest {

    @Nested
    @DisplayName("process() Method Tests")
    class ProcessTests {

        @Test
        @DisplayName("Should return true when exact amount provided")
        void process_WithExactAmount_ReturnsTrue() {
            // Arrange
            OnlinePayment payment = new OnlinePayment(new BigDecimal("500.00"));

            // Act
            boolean result = payment.process(new BigDecimal("500.00"));

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when more than required provided")
        void process_WithExcessAmount_ReturnsTrue() {
            // Arrange
            OnlinePayment payment = new OnlinePayment(new BigDecimal("1000.00"));

            // Act
            boolean result = payment.process(new BigDecimal("500.00"));

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when insufficient amount provided")
        void process_WithInsufficientAmount_ReturnsFalse() {
            // Arrange
            OnlinePayment payment = new OnlinePayment(new BigDecimal("400.00"));

            // Act
            boolean result = payment.process(new BigDecimal("500.00"));

            // Assert
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("getPaymentType() Method Tests")
    class PaymentTypeTests {

        @Test
        @DisplayName("Should return ONLINE as payment type")
        void getPaymentType_ReturnsOnline() {
            // Arrange
            OnlinePayment payment = new OnlinePayment(BigDecimal.TEN);

            // Act & Assert
            assertEquals("ONLINE", payment.getPaymentType());
        }
    }

    @Nested
    @DisplayName("getChangeDue() Method Tests")
    class ChangeDueTests {

        @Test
        @DisplayName("Should always return zero (online payments have no change)")
        void getChangeDue_AlwaysReturnsZero() {
            // Arrange
            OnlinePayment payment = new OnlinePayment(new BigDecimal("1000.00"));
            payment.process(new BigDecimal("500.00"));

            // Act & Assert - Online payments nogive change
            assertEquals(BigDecimal.ZERO, payment.getChangeDue());
        }

        @Test
        @DisplayName("Should return zero before processing")
        void getChangeDue_BeforeProcessing_ReturnsZero() {
            // Arrange
            OnlinePayment payment = new OnlinePayment(new BigDecimal("1000.00"));

            // Act & Assert
            assertEquals(BigDecimal.ZERO, payment.getChangeDue());
        }
    }

    @Nested
    @DisplayName("getCashTendered() Method Tests")
    class CashTenderedTests {

        @Test
        @DisplayName("Should return the payment amount")
        void getCashTendered_ReturnsPaymentAmount() {
            // Arrange
            BigDecimal amount = new BigDecimal("750.00");
            OnlinePayment payment = new OnlinePayment(amount);

            // Act & Assert
            assertEquals(amount, payment.getCashTendered());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle zero total amount")
        void process_WithZeroTotal_ReturnsTrue() {
            // Arrange
            OnlinePayment payment = new OnlinePayment(new BigDecimal("100.00"));

            // Act
            boolean result = payment.process(BigDecimal.ZERO);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle large amounts")
        void process_WithLargeAmount_WorksCorrectly() {
            // Arrange
            OnlinePayment payment = new OnlinePayment(new BigDecimal("999999.99"));

            // Act
            boolean result = payment.process(new BigDecimal("999999.99"));

            // Assert
            assertTrue(result);
        }
    }
}
