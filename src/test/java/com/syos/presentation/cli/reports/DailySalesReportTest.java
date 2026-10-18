package com.syos.presentation.cli.reports;

import com.syos.domain.repository.TransactionRepository;
import com.syos.domain.repository.TransactionRepository.ItemSaleSummary;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailySalesReportTest {

    @Mock TransactionRepository transRepo;

    // capture console output
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
    @DisplayName("Happy Path: generate() should print sales summary table")
    void generate_PrintsSalesTable() {
        // setup Data creating
        //  record format (itemCode, itemName, totalQty, totalRevenue)
        ItemSaleSummary mockSummary = new ItemSaleSummary(
                "Coke",
                "Coca Cola 1L",
                10,
                new BigDecimal("2000.00")
        );

        // Mock Repository
        // Match any date (LocalDate.now()), but specific type "POS"
        when(transRepo.getItemSalesByDate(any(LocalDate.class), eq("POS")))
                .thenReturn(List.of(mockSummary));

        //  get report
        DailySalesReport report = new DailySalesReport(transRepo, "POS");

        //  Act
        report.generate();

        //  Assert
        String output = outputStreamCaptor.toString();

        // checking titel and heading
        assertTrue(output.contains("DAILY SALES REPORT - POS ONLY"));
        assertTrue(output.contains("Revenue (LKR)"));

        // Check Data Row
        assertTrue(output.contains("Coke"));
        assertTrue(output.contains("Coca Cola 1L"));
        assertTrue(output.contains("10"));       // Qty
        assertTrue(output.contains("2000.00"));  // Revenue

        // check if total is present
        assertTrue(output.contains("TOTAL"));
        assertTrue(output.contains("2000.00"));
    }

    @Test
    @DisplayName("Edge Case: generate() should handle zero sales")
    void generate_NoSales() {
        //  mock Empty Return
        when(transRepo.getItemSalesByDate(any(LocalDate.class), eq(null)))
                .thenReturn(Collections.emptyList());

        //  get Report (Type null = ALL)
        DailySalesReport report = new DailySalesReport(transRepo, null);

        //  Act
        report.generate();

        //  Assert
        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("ALL CHANNELS"));
        assertTrue(output.contains("No sales recorded for today"));
        // shouldnt show totals
        assertTrue(!output.contains("TOTAL"));
    }



    @Test
    @DisplayName("Formatting: Should truncate long item names")
    void generate_TruncatesLongNames() {
        // setup Long Name
        String longName = "Very Long Product Name That Needs Cutting";
        ItemSaleSummary mockSummary = new ItemSaleSummary(
                "X01", longName, 1, BigDecimal.TEN
        );

        when(transRepo.getItemSalesByDate(any(LocalDate.class), eq("WEB")))
                .thenReturn(List.of(mockSummary));

        //  Init Report
        DailySalesReport report = new DailySalesReport(transRepo, "WEB");

        //  Act
        report.generate();

        // Assert
        String output = outputStreamCaptor.toString();
        // should do t ht substring(0, 25-3) + "..." -> First 22 chars + "..."
        // "Very Long Product Name T..."
        assertTrue(output.contains("Very Long Product Name..."));
    }
}