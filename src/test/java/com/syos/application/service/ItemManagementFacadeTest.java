package com.syos.application.service;

import com.syos.application.dto.ItemCreateRequest;
import com.syos.domain.entity.Category;
import com.syos.domain.entity.Item;
import com.syos.domain.repository.CategoryRepository;
import com.syos.domain.repository.ItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemManagementFacade (Facade Pattern) Tests")
class ItemManagementFacadeTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ItemManagementFacade facade;

    @Nested
    @DisplayName("Category Management")
    class CategoryTests {

        @Test
        @DisplayName("getCategories should delegate to repository")
        void getCategories_DelegatesToRepo() {
            // Arrange
            List<Category> mockCategories = Arrays.asList(
                    new Category("BEV", "Beverages","beverege good"),
                    new Category("SNC", "Snacks","snacks good")
            );
            when(categoryRepository.findAll()).thenReturn(mockCategories);

            // Act
            List<Category> result = facade.getCategories();

            // Assert
            assertEquals(2, result.size());
            assertEquals("BEV", result.get(0).getCode());
            verify(categoryRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Item Creation & Code Generation")
    class CreateItemTests {

        @Test
        @DisplayName("Should generate code ending in 001 for first item in category")
        void createItem_FirstItem_GeneratesSequence001() {
            // Arrange
            String category = "BEV";
            when(categoryRepository.existsByCode(category)).thenReturn(true);
            when(itemRepository.findAll()).thenReturn(Collections.emptyList());

            // Act - use DTO now
            ItemCreateRequest request = new ItemCreateRequest("Cola", new BigDecimal("150.00"), category, 10);
            Item result = facade.createItem(request);

            // Assert
            String currentYear = String.valueOf(LocalDate.now().getYear());
            String expectedCode = "BEV-001-" + currentYear;

            assertEquals(expectedCode, result.getItemCode());
            verify(itemRepository).save(any(Item.class));
        }

        @Test
        @DisplayName("Should increment sequence correctly if items exist")
        void createItem_ExistingItems_IncrementsSequence() {
            // Arrange
            String category = "SNC";
            when(categoryRepository.existsByCode(category)).thenReturn(true);

            // Already have 2 items in SNC category
            List<Item> existingItems = Arrays.asList(
                    new Item("SNC-001-2025", "Chips", BigDecimal.TEN, "SNC", 5),
                    new Item("SNC-002-2025", "Nuts", BigDecimal.TEN, "SNC", 5),
                    new Item("BEV-001-2025", "Water", BigDecimal.TEN, "BEV", 5)
            );
            when(itemRepository.findAll()).thenReturn(existingItems);

            // Act - use DTO now
            ItemCreateRequest request = new ItemCreateRequest("Biscuits", new BigDecimal("100.00"), category, 20);
            Item result = facade.createItem(request);

            // Assert - next sequence should be 3
            String currentYear = String.valueOf(LocalDate.now().getYear());
            String expectedCode = "SNC-003-" + currentYear;

            assertEquals(expectedCode, result.getItemCode());
        }

        @Test
        @DisplayName("Should throw exception if category is invalid")
        void createItem_InvalidCategory_ThrowsException() {
            // Arrange
            when(categoryRepository.existsByCode("INVALID")).thenReturn(false);

            // Act & Assert - use DTO now
            ItemCreateRequest request = new ItemCreateRequest("Name", BigDecimal.TEN, "INVALID", 10);

            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    facade.createItem(request)
            );

            assertEquals("Invalid category code: INVALID", exception.getMessage());
            verify(itemRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should save correct item details to repository")
        void createItem_SavesToRepository() {
            // Arrange
            when(categoryRepository.existsByCode("BEV")).thenReturn(true);
            when(itemRepository.findAll()).thenReturn(Collections.emptyList());

            // Act - use DTO now
            ItemCreateRequest request = new ItemCreateRequest("Pepsi", new BigDecimal("200.00"), "BEV", 50);
            facade.createItem(request);

            // Assert - capture what was saved
            ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
            verify(itemRepository).save(itemCaptor.capture());

            Item savedItem = itemCaptor.getValue();
            assertEquals("Pepsi", savedItem.getName());
            assertEquals(new BigDecimal("200.00"), savedItem.getUnitPrice());
            assertEquals("BEV", savedItem.getCategory());
            assertEquals(50, savedItem.getReorderLevel());
        }


        @Nested
        @DisplayName("Item Deletion")
        class DeleteItemTests {

            @Test
            @DisplayName("Should successfully delete item when it exists")
            void deleteItem_ExistingItem_DeletesSuccessfully() {
                // Arrange
                String itemCode = "BEV-001-2026";
                when(itemRepository.existsByCode(itemCode)).thenReturn(true);

                // Act
                facade.deleteItem(itemCode);

                // Assert
                verify(itemRepository, times(1)).existsByCode(itemCode);
                verify(itemRepository, times(1)).delete(itemCode);
            }

            @Test
            @DisplayName("Should throw exception when item to delete does not exist")
            void deleteItem_NonExistentItem_ThrowsException() {
                // Arrange
                String itemCode = "NON-EXISTENT";
                when(itemRepository.existsByCode(itemCode)).thenReturn(false);

                // Act & Assert
                Exception exception = assertThrows(IllegalArgumentException.class, () ->
                        facade.deleteItem(itemCode)
                );

                assertEquals("Item with code 'NON-EXISTENT' does not exist.", exception.getMessage());

                // ensure delete was never called because the check failed
                verify(itemRepository, never()).delete(anyString());
            }
        }
    }
}