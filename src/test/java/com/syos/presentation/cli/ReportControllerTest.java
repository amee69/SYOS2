package com.syos.presentation.cli;

import com.syos.application.service.StockService;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.StockRepository;
import com.syos.domain.repository.TransactionRepository;
import com.syos.presentation.cli.factory.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Scanner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock StockService stockService;
    @Mock ItemRepository itemRepo;
    @Mock TransactionRepository transactionRepo;
    @Mock StockRepository stockRepo;

    // mock objects for the generators
    private SalesGenerator mockSalesGenerator;
    private InventoryGenerator mockInventoryGenerator;
    private ReorderGenerator mockReorderGenerator;
    private BatchStockGenerator mockBatchGenerator;
    private BillReportGenerator mockBillGenerator;
    private ReshelvingGenerator mockReshelveGenerator;

    @BeforeEach
    void setUp() {
        // init mocks
        mockSalesGenerator = mock(SalesGenerator.class);
        mockInventoryGenerator = mock(InventoryGenerator.class);
        mockReorderGenerator = mock(ReorderGenerator.class);
        mockBatchGenerator = mock(BatchStockGenerator.class);
        mockBillGenerator = mock(BillReportGenerator.class);
        mockReshelveGenerator = mock(ReshelvingGenerator.class);
    }

    // helper to create controller with specific user input
    // and inject the mocks using reflection
    private ReportController createTestController(String userInput) throws Exception {

        // create scanner with the test input
        Scanner mockScanner = new Scanner(userInput);

        // init controller with the scanner
        ReportController controller = new ReportController(
                stockService, itemRepo, transactionRepo, stockRepo, mockScanner
        );

        // Reflection magic to swap real generators with mocks
        // (needed because generators are created inside the constructor)
        Field field = ReportController.class.getDeclaredField("generators");
        field.setAccessible(true);
        Map<String, ReportGenerator> generatorsMap = (Map<String, ReportGenerator>) field.get(controller);

        generatorsMap.put("SALES", mockSalesGenerator);
        generatorsMap.put("INVENTORY", mockInventoryGenerator);
        generatorsMap.put("REORDER", mockReorderGenerator);
        generatorsMap.put("BATCH", mockBatchGenerator);
        generatorsMap.put("BILLS", mockBillGenerator);
        generatorsMap.put("RESHELVE", mockReshelveGenerator);

        return controller;
    }

    @Test
    @DisplayName("Sales Report: Select POS filter (Option 2)")
    void runDailySalesReport_POS() throws Exception {
        // input '2' for POS
        ReportController controller = createTestController("2\n");

        controller.runDailySalesReport();

        // verify filter was set and run called
        verify(mockSalesGenerator).setTransactionType("POS");
        verify(mockSalesGenerator).run();
    }

    @Test
    @DisplayName("Bill Report: Select WEB filter (Option 3)")
    void runBillReport_WEB() throws Exception {
        // input '3' for WEB
        ReportController controller = createTestController("3\n");

        controller.runBillReport();

        verify(mockBillGenerator).setTransactionType("WEB");
        verify(mockBillGenerator).run();
    }

    @Test
    @DisplayName("Inventory Report: Should run directly")
    void runInventoryReport() throws Exception {
        ReportController controller = createTestController("\n");

        controller.runInventoryReport();

        verify(mockInventoryGenerator).run();
    }

    @Test
    @DisplayName("Reorder Report: Should run directly")
    void runReorderReport() throws Exception {
        ReportController controller = createTestController("\n");

        controller.runReorderReport();

        verify(mockReorderGenerator).run();
    }

    @Test
    @DisplayName("Batch Stock Report: Should run directly")
    void runBatchStockReport() throws Exception {
        ReportController controller = createTestController("\n");

        controller.runBatchStockReport();

        verify(mockBatchGenerator).run();
    }

    @Test
    @DisplayName("Reshelving Report: Should run directly")
    void runReshelvingReport() throws Exception {
        ReportController controller = createTestController("\n");

        controller.runReshelvingReport();

        verify(mockReshelveGenerator).run();
    }
}