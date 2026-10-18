package com.syos.application.service;

import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BillFileWriter Tests")
class BillFileWriterTest {

    private BillFileWriter billFileWriter;

    @BeforeEach
    void setUp() {
        billFileWriter = new BillFileWriter();
    }

    @Test
    @DisplayName("Should save bill to file without throwing exception")
    void save_ValidBill_CreatesFile() {
        Bill bill = new Bill.Builder()
                .setSerial(101)
                .setBillDate(LocalDateTime.now())
                .setTransactionType("POS")
                .setCashierUsername("cashier1")
                .setTotal(new BigDecimal("500.00"))
                .setDiscount(BigDecimal.ZERO)
                .setItems(List.of(new BillItem("D01", "Milk", 2, new BigDecimal("250.00"))))
                .build();

        // should not throw
        assertDoesNotThrow(() -> billFileWriter.save(bill));
    }

    @Test
    @DisplayName("Should handle bill with discount and loyalty points")
    void save_BillWithDiscount_CreatesFile() {
        Bill bill = new Bill.Builder()
                .setSerial(102)
                .setBillDate(LocalDateTime.now())
                .setTransactionType("WEB")
                .setCashierUsername("WEB-SYSTEM")
                .setTotal(new BigDecimal("1000.00"))
                .setDiscount(new BigDecimal("100.00"))
                .setDiscountDescription("10% Loyalty Discount")
                .setCustomerId(1)
                .setLoyaltyPointsUsed(50)
                .setLoyaltyPointsEarned(9)
                .setItems(List.of(new BillItem("D01", "Rice", 1, new BigDecimal("1000.00"))))
                .build();

        assertDoesNotThrow(() -> billFileWriter.save(bill));
    }

    @Test
    @DisplayName("Should handle null cashier gracefully")
    void save_NullCashier_NoException() {
        Bill bill = new Bill.Builder()
                .setSerial(103)
                .setBillDate(LocalDateTime.now())
                .setTransactionType("WEB")
                .setTotal(new BigDecimal("200.00"))
                .setDiscount(BigDecimal.ZERO)
                .setItems(List.of(new BillItem("D01", "Bread", 1, new BigDecimal("200.00"))))
                .build();

        assertDoesNotThrow(() -> billFileWriter.save(bill));
    }
}