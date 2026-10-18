package com.syos.presentation.cli;

import com.syos.application.dto.ItemCreateRequest;
import com.syos.application.dto.StockBatchRequest;
import com.syos.application.service.ItemManagementFacade;
import com.syos.application.service.StockService;
import com.syos.domain.entity.Category;
import com.syos.domain.entity.Item;
import com.syos.domain.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Scanner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Warehouse Controller UI Tests")
class WarehouseControllerTest {

    @Mock private StockService stockService;
    @Mock private ItemRepository itemRepository;
    @Mock private ItemManagementFacade itemFacade;

    private WarehouseController controller;

    // helper to inject the fake user input
    private void setupController(String userInput) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(userInput.getBytes(StandardCharsets.UTF_8));
        Scanner mockScanner = new Scanner(inputStream);
        controller = new WarehouseController(stockService, mockScanner, itemRepository, itemFacade);
    }

    @Test
    @DisplayName("addNewItem: Should create item when inputs are valid")
    void addNewItem_Success() {
        //  Select Category "1" -> Name "Milk" -> Price "500" -> Threshold "10" -> Confirm "y"
        String input = "1\nMilk\n500.00\n10\ny\n";
        setupController(input);

        // Mock: facade returns one category "Dairy"
        Category mockCat = new Category("DAIRY", "Dairy Products", "D01");
        when(itemFacade.getCategories()).thenReturn(Collections.singletonList(mockCat));

        // Mock: facade successfully creates an item
        Item mockItem = new Item("D01-001", "Milk", new BigDecimal("500.00"), "Dairy", 10);
        when(itemFacade.createItem(any(ItemCreateRequest.class))).thenReturn(mockItem);

        // Act
        controller.addNewItem();

        // Assert
        verify(itemFacade).createItem(any(ItemCreateRequest.class));
    }

    @Test
    @DisplayName("manageArrivals: Should add stock batch when inputs are valid")
    void manageArrivals_Success() {
        //  Item "DRY-001" -> Qty "100" -> Expiry "2030-01-01"
        String input = "DRY-001\n100\n2030-01-01\n";
        setupController(input);

        // Mock: item exists
        Item mockItem = new Item("DRY-001", "Rice", BigDecimal.TEN, "Food", 50);
        when(itemRepository.findByCode("DRY-001")).thenReturn(Optional.of(mockItem));

        // Act
        controller.manageArrivals();

        // Assert
        verify(stockService).addBatch(any(StockBatchRequest.class));
    }

    @Test
    @DisplayName("manageArrivals: Should warn user if expiry is close (within 30 days)")
    void manageArrivals_ExpiryWarning() {
        //  Item -> Qty -> Expiry (Tomorrow) -> Confirm "y"
        String tomorrow = LocalDate.now().plusDays(1).toString();
        String input = "DRY-001\n100\n" + tomorrow + "\ny\n";

        setupController(input);

        when(itemRepository.findByCode("DRY-001")).thenReturn(Optional.of(new Item("DRY-001", "Rice", BigDecimal.TEN, "Food", 50)));

        // Act
        controller.manageArrivals();

        // Assert - even with warning, if user typed 'y', it should save
        verify(stockService).addBatch(any(StockBatchRequest.class));
    }

    @Test
    @DisplayName("manageDistribution: Should move stock from STORE to SHELF")
    void manageDistribution_Success() {
        //  Item "DRY-001" -> Qty "10" -> Selection "1" (SHELF)
        String input = "DRY-001\n10\n1\n";
        setupController(input);

        when(itemRepository.findByCode("DRY-001")).thenReturn(Optional.of(new Item("DRY-001", "Rice", BigDecimal.TEN, "Food", 50)));
        when(stockService.getStockByLocation("DRY-001", "STORE")).thenReturn(50);
        when(stockService.getStockByLocation("DRY-001", "SHELF")).thenReturn(0, 10);

        // Act
        controller.manageDistribution();

        // Assert
        verify(stockService).moveStockToLocation("DRY-001", 10, "SHELF");
    }

    @Test
    @DisplayName("manageDistribution: Should stop if warehouse has no stock")
    void manageDistribution_NoStock_Stops() {
        //  Item "DRY-001"
        String input = "DRY-001\n";
        setupController(input);

        when(itemRepository.findByCode("DRY-001")).thenReturn(Optional.of(new Item("DRY-001", "Rice", BigDecimal.TEN, "Food", 50)));
        when(stockService.getStockByLocation("DRY-001", "STORE")).thenReturn(0);

        // Act
        controller.manageDistribution();

        // Assert
        verify(stockService, never()).moveStockToLocation(anyString(), anyInt(), anyString());
    }
}