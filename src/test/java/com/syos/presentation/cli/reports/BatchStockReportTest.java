package com.syos.presentation.cli.reports;

import com.syos.domain.entity.Item;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.StockRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchStockReportTest {

    @Mock StockRepository stockRepo;
    @Mock ItemRepository itemRepo;

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
    @DisplayName("Happy Path: generate() should print batch details")
    void generate_PrintsBatchTable() {
        //  define the code in a variable so it awlays matches
        String CODE = "D01";

        // setup Item using that variable
        Item mockItem = new Item(CODE, "Fresh Milk", new BigDecimal("500"), "DAIRY", 10);

        //  setup Batch (Using the same code)
        StockBatch mockBatch = new StockBatch(
                101,
                CODE, // <--- MUST MATCH ITEM CODE
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31),
                50
        );

        //  mock Repos using the variable
        when(itemRepo.findAll()).thenReturn(List.of(mockItem));

        when(stockRepo.getBatchesByItemCode(CODE)).thenReturn(List.of(mockBatch));

        //  Act
        BatchStockReport report = new BatchStockReport(stockRepo, itemRepo);
        report.generate();

        //  Assert
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains(CODE), "Item Code missing");
        assertTrue(output.contains("Fresh Milk"), "Item Name missing");
        assertTrue(output.contains("50"), "Quantity missing");
        assertTrue(output.contains("2023-12-31"), "Expiry date missing");
    }

    @Test
    @DisplayName("Edge Case: generate() should handle empty warehouse (no batches)")
    void generate_EmptyWarehouse() {
        Item mockItem = new Item("D01", "Milk", new BigDecimal("500"), "DAIRY", 10);

        when(itemRepo.findAll()).thenReturn(List.of(mockItem));
        when(stockRepo.getBatchesByItemCode("D01")).thenReturn(Collections.emptyList());

        BatchStockReport report = new BatchStockReport(stockRepo, itemRepo);
        report.generate();

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("No stock batches in warehouse"));
    }

    @Test
    @DisplayName("Formatting: Should truncate long item names in output")
    void generate_TruncatesLongNames() {
        String longName = "Super Duper Ultra Long Product Name";
        Item mockItem = new Item("X99", longName, BigDecimal.TEN, "MISC", 5);


        StockBatch mockBatch = new StockBatch(
                102, "X99", LocalDate.now(), LocalDate.now(), 10
        );

        when(itemRepo.findAll()).thenReturn(List.of(mockItem));
        when(stockRepo.getBatchesByItemCode("X99")).thenReturn(List.of(mockBatch));

        BatchStockReport report = new BatchStockReport(stockRepo, itemRepo);
        report.generate();

        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("Super Duper Ultra..."), "Should truncate name");
    }
}