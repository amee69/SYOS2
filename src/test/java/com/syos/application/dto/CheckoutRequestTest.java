package com.syos.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CheckoutRequest (Builder Pattern) Tests")
class CheckoutRequestTest {

    @Nested
    @DisplayName("Builder Basic Tests")
    class BuilderBasicTests {

        @Test
        @DisplayName("Should build request with minimum required fields")
        void build_WithMinimumFields_CreatesRequest() {
            List<BillItemRequest> items = Arrays.asList(new BillItemRequest("DRY-001", 5));

            CheckoutRequest request = CheckoutRequest.builder()
                    .setItems(items)
                    .build();

            assertNotNull(request);
            assertEquals(1, request.getItems().size());
        }

        @Test
        @DisplayName("Should build request with all fields")
        void build_WithAllFields_CreatesCompleteRequest() {
            List<BillItemRequest> items = Arrays.asList(
                    new BillItemRequest("DRY-001", 5),
                    new BillItemRequest("BEV-001", 3)
            );

            CheckoutRequest request = CheckoutRequest.builder()
                    .setItems(items)
                    .setTransactionType("POS")
                    .setCashTendered(new BigDecimal("1000.00"))
                    .setPaymentType("CASH")
                    .setCustomerId(1)
                    .setCashierUsername("cashier1")
                    .setLoyaltyPointsToRedeem(50)
                    .setDiscountDetails(new BigDecimal("100.00"), "Gold 10%")
                    .build();

            assertEquals(2, request.getItems().size());
            assertEquals("POS", request.getTransactionType());
            assertEquals(1, request.getCustomerId());
            assertEquals("cashier1", request.getCashierUsername());
            assertEquals(50, request.getLoyaltyPointsToRedeem());
            assertEquals(new BigDecimal("100.00"), request.getTotalDiscount());
            assertEquals("CASH", request.getPaymentType());
            assertEquals(new BigDecimal("1000.00"), request.getCashTendered());
        }

        @Test
        @DisplayName("Should throw exception when no items provided")
        void build_WithNoItems_ThrowsException() {
            assertThrows(IllegalStateException.class, () -> {
                CheckoutRequest.builder()
                        .setTransactionType("POS")
                        .build();
            });
        }
    }
}