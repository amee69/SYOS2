package com.syos.infrastructure.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


  //unit tests for CashPayment (Strategy Pattern).
 // Tests: process, getPaymentType, getCashTendered, getChangeDue

@DisplayName("CashPayment Strategy Tests")
class CashPaymentTest {

    @Nested
    @DisplayName("process() Method Tests")
    class ProcessTests {

        @Test
        @DisplayName("Should return true when exact amount tendered")
        void process_WithExactAmount_ReturnsTrue() {
            CashPayment payment = new CashPayment(new BigDecimal("500.00"));
            boolean result = payment.process(new BigDecimal("500.00"));
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return true when more than required tendered")
        void process_WithExcessAmount_ReturnsTrue() {
            CashPayment payment = new CashPayment(new BigDecimal("1000.00"));
            boolean result = payment.process(new BigDecimal("750.00"));
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false when insufficient amount tendered")
        void process_WithInsufficientAmount_ReturnsFalse() {
            CashPayment payment = new CashPayment(new BigDecimal("400.00"));
            boolean result = payment.process(new BigDecimal("500.00"));
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return true for zero total amount")
        void process_WithZeroTotal_ReturnsTrue() {
            CashPayment payment = new CashPayment(new BigDecimal("100.00"));
            boolean result = payment.process(BigDecimal.ZERO);
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("getChangeDue() Method Tests")
    class ChangeDueTests {

        @Test
        @DisplayName("Should return zero change for exact payment")
        void getChangeDue_WithExactPayment_ReturnsZero() {
            // Arrange
            CashPayment payment = new CashPayment(new BigDecimal("500.00"));
            payment.process(new BigDecimal("500.00"));

            // Act & Assert
            // compareTo returns 0 if values are mathematically equal (ignoring 0 vs 0.00)
            assertEquals(0, BigDecimal.ZERO.compareTo(payment.getChangeDue()),
                    "Change should be mathematically zero");
        }

        @Test
        @DisplayName("Should calculate correct change for overpayment")
        void getChangeDue_WithOverpayment_ReturnsCorrectChange() {
            // Arrange: tendered 1000, total 750 = change 250
            CashPayment payment = new CashPayment(new BigDecimal("1000.00"));
            payment.process(new BigDecimal("750.00"));

            // Act & Assert
            assertEquals(new BigDecimal("250.00"), payment.getChangeDue());
        }

        @Test
        @DisplayName("Should return zero before processing")
        void getChangeDue_BeforeProcessing_ReturnsZero() {
            // Arrange
            CashPayment payment = new CashPayment(new BigDecimal("1000.00"));

            // Act & Assert (process not called)
            // fixedd compareTo covers cases where default might be 0.00
            assertEquals(0, BigDecimal.ZERO.compareTo(payment.getChangeDue()));
        }

        @Test
        @DisplayName("Should handle decimal amounts correctly")
        void getChangeDue_WithDecimalAmounts_CalculatesCorrectly() {
            // Arrange: tendered 100, total 67.50 = change 32.50
            CashPayment payment = new CashPayment(new BigDecimal("100.00"));
            payment.process(new BigDecimal("67.50"));

            // Act & Assert
            assertEquals(new BigDecimal("32.50"), payment.getChangeDue());
        }

        @Test
        @DisplayName("Should handle small change amounts")
        void getChangeDue_SmallChange_CalculatesCorrectly() {
            // Arrange: tendered 100, total 99.99 = change 0.01
            CashPayment payment = new CashPayment(new BigDecimal("100.00"));
            payment.process(new BigDecimal("99.99"));

            // Act & Assert
            assertEquals(new BigDecimal("0.01"), payment.getChangeDue());
        }
    }

    @Nested
    @DisplayName("getPaymentType() Method Tests")
    class PaymentTypeTests {

        @Test
        @DisplayName("Should return CASH as payment type")
        void getPaymentType_ReturnsCash() {
            CashPayment payment = new CashPayment(BigDecimal.TEN);
            assertEquals("CASH", payment.getPaymentType());
        }
    }

    @Nested
    @DisplayName("getCashTendered() Method Tests")
    class CashTenderedTests {

        @Test
        @DisplayName("Should return the tendered amount")
        void getCashTendered_ReturnsCorrectAmount() {
            BigDecimal tendered = new BigDecimal("1500.00");
            CashPayment payment = new CashPayment(tendered);
            assertEquals(tendered, payment.getCashTendered());
        }

        @Test
        @DisplayName("Should return tendered amount regardless of processing")
        void getCashTendered_BeforeAndAfterProcessing_ReturnsSameAmount() {
            BigDecimal tendered = new BigDecimal("500.00");
            CashPayment payment = new CashPayment(tendered);

            // Before processing
            assertEquals(tendered, payment.getCashTendered());

            // After processing
            payment.process(new BigDecimal("300.00"));
            assertEquals(tendered, payment.getCashTendered());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very large amounts")
        void process_LargeAmounts_CalculatesCorrectly() {
            CashPayment payment = new CashPayment(new BigDecimal("1000000.00"));
            payment.process(new BigDecimal("999999.99"));

            assertEquals(new BigDecimal("0.01"), payment.getChangeDue());
        }

        @Test
        @DisplayName("Should handle very small tendered amount")
        void process_SmallTenderedAmount_WorksCorrectly() {
            CashPayment payment = new CashPayment(new BigDecimal("0.50"));

            boolean result = payment.process(new BigDecimal("0.25"));

            assertTrue(result);
            assertEquals(new BigDecimal("0.25"), payment.getChangeDue());
        }
    }
}