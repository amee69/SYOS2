package com.syos.presentation.cli;

import com.syos.domain.service.PaymentStrategy;
import com.syos.infrastructure.payment.CashPayment;
import com.syos.infrastructure.payment.OnlinePayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment Factory Tests")
class PaymentFactoryTest {

    private PaymentFactory paymentFactory;  // ADD

    @BeforeEach
    void setUp() {
        paymentFactory = new PaymentFactory();  // ADD
    }

    private Scanner createMockScanner(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("WEB: Should return OnlinePayment when user confirms (y)")
    void create_Web_Confirmed_ReturnsOnlinePayment() {
        Scanner scanner = createMockScanner("y\n");
        BigDecimal amount = new BigDecimal("100.00");

        PaymentStrategy result = paymentFactory.create("WEB", amount, scanner);  // CHANGED

        assertNotNull(result);
        assertInstanceOf(OnlinePayment.class, result);  // Modernized assertion
    }

    @Test
    @DisplayName("WEB: Should return null when user cancels (n)")
    void create_Web_Cancelled_ReturnsNull() {
        Scanner scanner = createMockScanner("n\n");
        BigDecimal amount = new BigDecimal("100.00");

        PaymentStrategy result = paymentFactory.create("WEB", amount, scanner);  // CHANGED

        assertNull(result);
    }

    @Test
    @DisplayName("POS: Should return CashPayment when valid amount is entered")
    void create_Pos_ValidAmount_ReturnsCashPayment() {
        Scanner scanner = createMockScanner("5000\n");
        BigDecimal billAmount = new BigDecimal("1000.00");

        PaymentStrategy result = paymentFactory.create("POS", billAmount, scanner);  // CHANGED

        assertNotNull(result);
        assertInstanceOf(CashPayment.class, result);  // Modernized assertion
    }

    @Test
    @DisplayName("POS: Should loop until valid amount is entered")
    void create_Pos_InsufficientThenValid_LoopsCorrectly() {
        Scanner scanner = createMockScanner("50\n1000\n");
        BigDecimal billAmount = new BigDecimal("500.00");

        PaymentStrategy result = paymentFactory.create("POS", billAmount, scanner);  // CHANGED

        assertNotNull(result);
        assertInstanceOf(CashPayment.class, result);  // Modernized assertion
    }

    @Test
    @DisplayName("POS: Should return null if user types 'cancel'")
    void create_Pos_UserCancels_ReturnsNull() {
        Scanner scanner = createMockScanner("cancel\n");
        BigDecimal billAmount = new BigDecimal("500.00");

        PaymentStrategy result = paymentFactory.create("POS", billAmount, scanner);  // CHANGED

        assertNull(result);
    }
}