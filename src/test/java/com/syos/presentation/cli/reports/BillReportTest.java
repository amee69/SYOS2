package com.syos.presentation.cli.reports;

import com.syos.domain.entity.Bill;
import com.syos.domain.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillReportTest {

    @Mock TransactionRepository transRepo;

    //  capture the console output
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream standardOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    @DisplayName("Happy Path: generate() should print bill details and totals")
    void generate_PrintsTransactionTable() {
        //  setup Data use builder
        Bill mockBill = new Bill.Builder()
                .setSerial(101)
                .setBillDate(LocalDateTime.of(2023, 10, 5, 14, 30))
                .setTransactionType("POS")
                .setCashierUsername("alice")
                .setTotal(new BigDecimal("1000.00"))
                .setDiscount(BigDecimal.ZERO)
                .setItems(Collections.emptyList())
                .build();

        //  Mock Repo
        when(transRepo.getAllTransactions("POS")).thenReturn(List.of(mockBill));


        BillReport report = new BillReport(transRepo, "POS");

        //  Act
        report.generate();

        //  Assert
        String output = outputStreamCaptor.toString();

        // verify Header & Title
        assertTrue(output.contains("BILL REPORT - POS TRANSACTIONS ONLY"), "Title should match filter");
        assertTrue(output.contains("Bill #"), "Table header missing");

        // verify Data Row
        assertTrue(output.contains("101"));          // Serial
        assertTrue(output.contains("2023-10-05"));   // Date
        assertTrue(output.contains("alice"));        // Cashier
        assertTrue(output.contains("1000.00"));      // Amount

        // verify Footer (Total Revenue)
        assertTrue(output.contains("TOTAL REVENUE:"));
        assertTrue(output.contains("1000.00"));
    }

    @Test
    @DisplayName("Edge Case: generate() should handle empty transaction list")
    void generate_EmptyList() {
        //  Mock Empty Return
        when(transRepo.getAllTransactions(null)).thenReturn(Collections.emptyList());

        //  Init Report (Filter = null means ALL)
        BillReport report = new BillReport(transRepo, null);

        //  Act
        report.generate();

        //  Assert
        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("BILL REPORT - ALL TRANSACTIONS"));
        assertTrue(output.contains("No transactions found"));
        // ensure it didnt print the footer
        assertTrue(!output.contains("TOTAL REVENUE"));
    }

    @Test
    @DisplayName("Verification: Should pass the correct filter to Repository")
    void generate_PassesFilterToRepo() {
        //  Setup
        when(transRepo.getAllTransactions("WEB")).thenReturn(Collections.emptyList());
        BillReport report = new BillReport(transRepo, "WEB");

        //  Act
        report.generate();

        //  Assert Verify the repository was called with "WEB", not null or "POS"
        verify(transRepo).getAllTransactions("WEB");
    }
}