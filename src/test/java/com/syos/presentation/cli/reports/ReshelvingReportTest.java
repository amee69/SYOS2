package com.syos.presentation.cli.reports;

import com.syos.application.service.StockService;
import com.syos.domain.entity.Item;
import com.syos.domain.repository.ItemRepository;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReshelvingReportTest {

    @Mock StockService stockService;
    @Mock ItemRepository itemRepo;

    // get console output
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
    @DisplayName("Happy Path: Suggests reshelving when shelf is low and warehouse has enough")
    void generate_SuggestsReshelving() {
        // reorderLevel = 50 to match old TARGET_SHELF_LEVEL behaviour
        Item mockItem = new Item("D01", "Biscuits", new BigDecimal("100"), "SNACK", 50);

        when(itemRepo.findAll()).thenReturn(List.of(mockItem));
        when(stockService.getStockByLocation("D01", "SHELF")).thenReturn(30);
        when(stockService.getStockByLocation("D01", "STORE")).thenReturn(100);

        ReshelvingReport report = new ReshelvingReport(stockService, itemRepo);
        report.generate();

        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("END OF DAY RESHELVING REPORT"));
        assertTrue(output.contains("D01"));
        assertTrue(output.contains("Biscuits"));
        assertTrue(output.contains("30"));   // shelf
        assertTrue(output.contains("100"));  // warehouse
        assertTrue(output.contains("20"));   // 50 - 30 = 20
        assertTrue(output.contains("TOTAL UNITS TO RESHELVE: 20"));
    }

    @Test
    @DisplayName("Logic Check: Only move what we have if warehouse is low")
    void generate_LimitedByWarehouse() {
        // reorderLevel = 50, shelf = 10, needs 40 but warehouse only has 5
        Item mockItem = new Item("D02", "Choco", new BigDecimal("50"), "SNACK", 50);

        when(itemRepo.findAll()).thenReturn(List.of(mockItem));
        when(stockService.getStockByLocation("D02", "SHELF")).thenReturn(10);
        when(stockService.getStockByLocation("D02", "STORE")).thenReturn(5);

        ReshelvingReport report = new ReshelvingReport(stockService, itemRepo);
        report.generate();

        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("D02"));
        assertTrue(output.contains("5"));
        assertTrue(output.contains("TOTAL UNITS TO RESHELVE: 5"));
    }

    @Test
    @DisplayName("Edge Case: No reshelving if shelves are already full")
    void generate_ShelvesFull() {
        Item mockItem = new Item("D03", "Full Item", new BigDecimal("10"), "MISC", 5);

        // shelf has 60, target is 50 so no need move anything
        when(itemRepo.findAll()).thenReturn(List.of(mockItem));
        when(stockService.getStockByLocation("D03", "SHELF")).thenReturn(60);
        when(stockService.getStockByLocation("D03", "STORE")).thenReturn(100);

        ReshelvingReport report = new ReshelvingReport(stockService, itemRepo);
        report.generate();

        String output = outputStreamCaptor.toString();

        // item should not show up in table
        assertTrue(!output.contains("D03"));
        assertTrue(output.contains("All shelves are adequately stocked"));
    }

    @Test
    @DisplayName("Edge Case: Ignore low shelf if warehouse is empty")
    void generate_WarehouseEmpty() {
        Item mockItem = new Item("D04", "Empty Warehouse Item", new BigDecimal("10"), "MISC", 5);

        // shelf empty (needs 50), but warehouse also 0
        when(itemRepo.findAll()).thenReturn(List.of(mockItem));
        when(stockService.getStockByLocation("D04", "SHELF")).thenReturn(0);
        when(stockService.getStockByLocation("D04", "STORE")).thenReturn(0);

        ReshelvingReport report = new ReshelvingReport(stockService, itemRepo);
        report.generate();

        String output = outputStreamCaptor.toString();

        // cant reshelve if warehouse empty so dont list it
        assertTrue(!output.contains("D04"));
        assertTrue(output.contains("No reshelving required"));
    }
}