package com.syos.application.service;

import com.syos.application.dto.BillItemRequest;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


 // Unit tests for ValidationService.

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidationService Tests")
class ValidationServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private StockRepository stockRepository;

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService(itemRepository, stockRepository, "SHELF");
    }

    // valid item test

    @Test
    @DisplayName("Should return null when item is valid")
    void validateItem_ValidItem_ReturnsNull() {
        // Arrange
        String itemCode = "DRY-001";
        int quantity = 5;
        BillItemRequest request = new BillItemRequest(itemCode, quantity);

        // Mock: item exists
        when(itemRepository.existsByCode(itemCode)).thenReturn(true);
        // Mock: stock is sufficient
        when(stockRepository.getQuantityByLocation(itemCode, "SHELF")).thenReturn(100);

        // Act
        String error = validationService.validateItem(request);

        // Assert
        assertNull(error);
    }

    // invalid item code test

    @Test
    @DisplayName("Should return error when item does not exist")
    void validateItem_ItemNotExists_ReturnsError() {
        // Arrange
        String itemCode = "INVALID-001";
        BillItemRequest request = new BillItemRequest(itemCode, 5);

        // Mock: item does NOT exist
        when(itemRepository.existsByCode(itemCode)).thenReturn(false);

        // Act
        String error = validationService.validateItem(request);

        // Assert
        assertNotNull(error);
    }

    // inavlid quantity tests

    @Test
    @DisplayName("Should return error when quantity is zero")
    void validateItem_ZeroQuantity_ReturnsError() {
        // Arrange
        BillItemRequest request = new BillItemRequest("DRY-001", 0);

        // QuantityValidator runs first in chain, so no need to mock repository

        // Act
        String error = validationService.validateItem(request);

        // Assert
        assertNotNull(error);
    }

    @Test
    @DisplayName("Should return error when quantity is negative")
    void validateItem_NegativeQuantity_ReturnsError() {
        // Arrange
        BillItemRequest request = new BillItemRequest("DRY-001", -5);

        // Act
        String error = validationService.validateItem(request);

        // Assert
        assertNotNull(error);
    }

    // insufficient stock test

    @Test
    @DisplayName("Should return error when stock is insufficient")
    void validateItem_InsufficientStock_ReturnsError() {
        // Arrange
        String itemCode = "DRY-001";
        int requestedQuantity = 50;
        int availableStock = 10;
        BillItemRequest request = new BillItemRequest(itemCode, requestedQuantity);

        // Mock: item exists
        when(itemRepository.existsByCode(itemCode)).thenReturn(true);
        // Mock: stock is NOT sufficient
        when(stockRepository.getQuantityByLocation(itemCode, "SHELF")).thenReturn(availableStock);

        // Act
        String error = validationService.validateItem(request);

        // Assert
        assertNotNull(error);
    }

    @Test
    @DisplayName("Should return error when stock is zero")
    void validateItem_ZeroStock_ReturnsError() {
        // Arrange
        String itemCode = "DRY-001";
        BillItemRequest request = new BillItemRequest(itemCode, 5);

        // Mock: item exists
        when(itemRepository.existsByCode(itemCode)).thenReturn(true);
        // Mock: no stock available
        when(stockRepository.getQuantityByLocation(itemCode, "SHELF")).thenReturn(0);

        // Act
        String error = validationService.validateItem(request);

        // Assert
        assertNotNull(error);
    }

    // chain verification test

    @Test
    @DisplayName("Should call item repository to check item exists")
    void validateItem_ValidItem_CallsItemRepository() {
        // Arrange
        String itemCode = "DRY-001";
        BillItemRequest request = new BillItemRequest(itemCode, 5);

        when(itemRepository.existsByCode(itemCode)).thenReturn(true);
        when(stockRepository.getQuantityByLocation(itemCode, "SHELF")).thenReturn(100);

        // Act
        validationService.validateItem(request);

        // Assert: verify repository was called
        verify(itemRepository).existsByCode(itemCode);
    }

    @Test
    @DisplayName("Should call stock repository to check availability")
    void validateItem_ValidItem_CallsStockRepository() {
        // Arrange
        String itemCode = "DRY-001";
        BillItemRequest request = new BillItemRequest(itemCode, 5);

        when(itemRepository.existsByCode(itemCode)).thenReturn(true);
        when(stockRepository.getQuantityByLocation(itemCode, "SHELF")).thenReturn(100);

        // Act
        validationService.validateItem(request);

        // Assert: verify repository was called with correct stock type
        verify(stockRepository).getQuantityByLocation(itemCode, "SHELF");
    }

    @Test
    @DisplayName("Should not check stock if item does not exist")
    void validateItem_ItemNotExists_DoesNotCheckStock() {
        // Arrange
        String itemCode = "INVALID-001";
        BillItemRequest request = new BillItemRequest(itemCode, 5);

        when(itemRepository.existsByCode(itemCode)).thenReturn(false);

        // Act
        validationService.validateItem(request);

        // Assert: stock repository should NOT be called (chain stops early)
        verify(stockRepository, never()).getQuantityByLocation(anyString(), anyString());
    }
}
