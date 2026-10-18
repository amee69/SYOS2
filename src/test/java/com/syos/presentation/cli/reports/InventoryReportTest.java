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
class InventoryReportTest {

    @Mock StockService stockService;
    @Mock ItemRepository itemRepo;

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
    @DisplayName("Happy Path: generate() should print inventory breakdown by location")
    void generate_PrintsInventoryTable() {

        Item mockItem = new Item("D01", "Rice", new BigDecimal("100"), "GRAIN", 10);

        //  mock Repository
        when(itemRepo.findAll()).thenReturn(List.of(mockItem));

        //  mock Stock Service calls

        when(stockService.getStockByLocation("D01", "STORE")).thenReturn(50);
        when(stockService.getStockByLocation("D01", "SHELF")).thenReturn(20);
        when(stockService.getStockByLocation("D01", "WEB")).thenReturn(5);

        //  get Report
        InventoryReport report = new InventoryReport(stockService, itemRepo);

        //  Act
        report.generate();

        //  Assert
        String output = outputStreamCaptor.toString();

        // Check Header & Title
        assertTrue(output.contains("FULL INVENTORY BREAKDOWN"));
        assertTrue(output.contains("Store"));
        assertTrue(output.contains("Shelf"));
        assertTrue(output.contains("Web"));

       //checking
        assertTrue(output.contains("D01"));    // Code
        assertTrue(output.contains("Rice"));   // Name
        assertTrue(output.contains("50"));     // Store Qty
        assertTrue(output.contains("20"));     // Shelf Qty
        assertTrue(output.contains("5"));      // Web Qty
    }

    @Test
    @DisplayName("Formatting: Should truncate long item names")
    void generate_TruncatesLongNames() {

        String longName = "A Very Long Product Name That Will Be Cut";
        Item mockItem = new Item("code", longName, BigDecimal.TEN, "MISC", 5);

        when(itemRepo.findAll()).thenReturn(List.of(mockItem));
        // mock service to return 0 to avoid NullPointerException if called
        when(stockService.getStockByLocation("code", "STORE")).thenReturn(0);
        when(stockService.getStockByLocation("code", "SHELF")).thenReturn(0);
        when(stockService.getStockByLocation("code", "WEB")).thenReturn(0);

        // run
        InventoryReport report = new InventoryReport(stockService, itemRepo);
        report.generate();

      //asert
        String output = outputStreamCaptor.toString();

        // Truncate logic 20  - 3  = 17 chars

        assertTrue(output.contains("A Very Long Produ..."));
    }

    @Test
    @DisplayName("Edge Case: Empty inventory should print header only (no crash)")
    void generate_EmptyInventory() {
        //  Mock Empty
        when(itemRepo.findAll()).thenReturn(Collections.emptyList());

        // Init  Run
        InventoryReport report = new InventoryReport(stockService, itemRepo);
        report.generate();

        //  Assert
        String output = outputStreamCaptor.toString();

        assertTrue(output.contains("FULL INVENTORY BREAKDOWN"));
        assertTrue(output.contains("Code"));
        // confirm no row is printed
        assertTrue(!output.contains("D01"));
    }
}