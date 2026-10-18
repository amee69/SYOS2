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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReorderReportTest {

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
    @DisplayName("Happy Path: Should list items that are below reorder level")
    void generate_PrintsReorderAlerts() {


        Item lowStockItem = new Item("D01", "Rice", new BigDecimal("100"), "GRAIN", 10);

        //  mockstock is only 5 (Below 10)
        when(itemRepo.findAll()).thenReturn(List.of(lowStockItem));
        when(stockService.getTotalStockCount("D01")).thenReturn(5);

        //get Run
        ReorderReport report = new ReorderReport(stockService, itemRepo);
        report.generate();

        // Assert
        String output = outputStreamCaptor.toString();

        // check Header
        assertTrue(output.contains("REORDER ALERT REPORT"));
        assertTrue(output.contains("Min Level"));


        assertTrue(output.contains("D01"));
        assertTrue(output.contains("Rice"));
        assertTrue(output.contains("5"));
        assertTrue(output.contains("10"));
        assertTrue(output.contains("REORDER NOW"));
    }

    @Test
    @DisplayName("Edge Case: Should say 'All Good' if stock is high enough")
    void generate_NoAlerts() {

        Item highStockItem = new Item("D01", "Rice", new BigDecimal("100"), "GRAIN", 10);


        when(itemRepo.findAll()).thenReturn(List.of(highStockItem));
        when(stockService.getTotalStockCount("D01")).thenReturn(50);


        ReorderReport report = new ReorderReport(stockService, itemRepo);
        report.generate();


        String output = outputStreamCaptor.toString();


        assertTrue(!output.contains("D01"));


        assertTrue(output.contains("All stock levels are Good"));
    }
}