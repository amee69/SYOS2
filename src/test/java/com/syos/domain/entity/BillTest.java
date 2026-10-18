package com.syos.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Bill Entity Tests")
class BillTest {

    //  mock BillItem because Bill holds a List of them.
    //  don't care what's inside BillItem,  just need the object to exist.
    @Mock
    BillItem mockBillItem;

    @Nested
    @DisplayName("Builder Construction Tests")
    class BuilderTests {

        @Test
        @DisplayName("Happy Path: Should build Bill with all fields set")
        void build_WithAllFields_CreatesCorrectBill() {
            // ARRANGE
            int serial = 101;
            LocalDateTime now = LocalDateTime.now();
            List<BillItem> items = Collections.singletonList(mockBillItem);
            BigDecimal total = new BigDecimal("500.00");
            BigDecimal discount = new BigDecimal("50.00");
            BigDecimal cashTendered = new BigDecimal("1000.00");
            BigDecimal change = new BigDecimal("500.00");

            //  ACT
            Bill bill = new Bill.Builder()
                    .setSerial(serial)
                    .setBillDate(now)
                    .setItems(items)
                    .setTotal(total)
                    .setDiscount(discount)
                    .setPaymentType("CARD")
                    .setTransactionType("ONLINE")
                    .setCustomerId(55)
                    .setCashierUsername("admin")
                    .setDiscountDescription("Loyalty Promo")
                    .setLoyaltyPointsUsed(10)
                    .setLoyaltyPointsEarned(5)
                    .setCashTendered(cashTendered) // New Field
                    .setChangeAmount(change)       // New Field
                    .build();

            //  ASSERT
            assertEquals(serial, bill.getSerialNumber());
            assertEquals(now, bill.getBillDate());
            assertEquals(items, bill.getBillItems());
            assertEquals(total, bill.getTotalFullPrice());
            assertEquals(discount, bill.getTotalDiscount());
            assertEquals("CARD", bill.getPaymentType());
            assertEquals("ONLINE", bill.getTransactionType());
            assertEquals(55, bill.getCustomerId());
            assertEquals("admin", bill.getCashierUsername());
            assertEquals("Loyalty Promo", bill.getDiscountDescription());
            assertEquals(10, bill.getLoyaltyPointsUsed());
            assertEquals(5, bill.getLoyaltyPointsEarned());
            assertEquals(cashTendered, bill.getCashTendered());
            assertEquals(change, bill.getChangeAmount());
        }

        @Test
        @DisplayName("Should use default values when optional fields are missing")
        void build_WithDefaults_SetsDefaultValues() {
            // Arrange
            List<BillItem> items = Collections.singletonList(mockBillItem);
            BigDecimal total = new BigDecimal("100.00");

            // Act (Only setting REQUIRED fields) since builder pattern perk
            Bill bill = new Bill.Builder()
                    .setItems(items)
                    .setTotal(total)
                    .build();

            // Assert (Check the defaults defined in Builder class)
            assertEquals("CASH", bill.getPaymentType());
            assertEquals("POS", bill.getTransactionType());
            assertEquals(BigDecimal.ZERO, bill.getTotalDiscount());
            assertEquals(BigDecimal.ZERO, bill.getCashTendered());
            assertEquals(BigDecimal.ZERO, bill.getChangeAmount());
            assertNotNull(bill.getBillDate(), "Date should default to now()");
        }
    }

    @Nested
    @DisplayName("Logic & Calculation Tests")
    class LogicTests {

        @Test
        @DisplayName("getNetTotal should subtract discount from total")
        void getNetTotal_CalculatesCorrectly() {
            // Arrange
            BigDecimal total = new BigDecimal("100.00");
            BigDecimal discount = new BigDecimal("20.00");
            BigDecimal expectedNet = new BigDecimal("80.00");

            // Act
            Bill bill = new Bill.Builder()
                    .setItems(Collections.singletonList(mockBillItem))
                    .setTotal(total)
                    .setDiscount(discount)
                    .build();

            // Assert
            assertEquals(expectedNet, bill.getNetTotal());
        }

        @Test
        @DisplayName("getNetTotal should equal total if discount is zero")
        void getNetTotal_WithZeroDiscount_EqualsTotal() {
            // Arrange
            BigDecimal total = new BigDecimal("100.00");

            // Act
            Bill bill = new Bill.Builder()
                    .setItems(Collections.singletonList(mockBillItem))
                    .setTotal(total)
                    .build(); // Default discount is ZERO

            // Assert
            assertEquals(total, bill.getNetTotal());
        }
    }

    @Nested
    @DisplayName("Validation (Edge Case) Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception if BillItems are null")
        void build_NullItems_ThrowsException() {
            // Arrange
            Bill.Builder builder = new Bill.Builder()
                    .setTotal(BigDecimal.TEN);
            //  intentionally not call setItems()

            // Act & Assert
            Exception exception = assertThrows(IllegalStateException.class, builder::build);
            assertEquals("Bill items cannot be null.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception if TotalAmount is null")
        void build_NullTotal_ThrowsException() {
            // Arrange
            Bill.Builder builder = new Bill.Builder()
                    .setItems(Collections.singletonList(mockBillItem));
            //  intentionally not call setTotal()

            // Act & Assert
            Exception exception = assertThrows(IllegalStateException.class, builder::build);
            assertEquals("Total amount is required.", exception.getMessage());
        }
    }
}