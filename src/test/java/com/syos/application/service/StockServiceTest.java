package com.syos.application.service;

import com.syos.application.dto.StockBatchRequest;
import com.syos.domain.entity.Item;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.observer.StockLevelMonitor;
import com.syos.domain.observer.StockObserver;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService Tests")
class StockServiceTest {

    @Mock private StockRepository stockRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private StockObserver mockObserver;

    private StockService stockService;

    @BeforeEach
    void setUp() {
        // create real monitor and attach mock observer
        StockLevelMonitor stockMonitor = new StockLevelMonitor();
        stockMonitor.addObserver(mockObserver);

        // innject monitor into service
        stockService = new StockService(stockRepository, itemRepository, stockMonitor);
    }

    @Nested
    @DisplayName("checkLowStock()")
    class CheckLowStockTests {

        @Test
        @DisplayName("Triggers observer when stock below threshold")
        void belowThreshold_triggersObserver() {
            Item item = new Item("DRY-001", "Rice", BigDecimal.TEN, "FOOD", 50);
            when(itemRepository.findByCode("DRY-001")).thenReturn(Optional.of(item));
            when(stockRepository.getTotalQuantityFromAllSources("DRY-001")).thenReturn(10);

            stockService.checkLowStock("DRY-001");

            verify(mockObserver).onLowStock("DRY-001", "Rice", 10, 50);
        }

        @Test
        @DisplayName("No alert when stock sufficient")
        void aboveThreshold_noAlert() {
            Item item = new Item("DRY-001", "Rice", BigDecimal.TEN, "FOOD", 50);
            when(itemRepository.findByCode("DRY-001")).thenReturn(Optional.of(item));
            when(stockRepository.getTotalQuantityFromAllSources("DRY-001")).thenReturn(100);

            stockService.checkLowStock("DRY-001");

            verify(mockObserver, never()).onLowStock(anyString(), anyString(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("Handles missing item safely")
        void itemNotFound_noException() {
            when(itemRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());
            when(stockRepository.getTotalQuantityFromAllSources("UNKNOWN")).thenReturn(0);

            stockService.checkLowStock("UNKNOWN");

            verify(mockObserver, never()).onLowStock(anyString(), anyString(), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("moveStockToLocation()")
    class MoveStockTests {

        @Test
        @DisplayName("Moves full amount from valid batch")
        void sufficientStock_movesFullAmount() {
            StockBatch batch = new StockBatch(1, "DRY-001", LocalDate.now(), LocalDate.now().plusMonths(6), 100);
            when(stockRepository.getBatchesByItemCode("DRY-001")).thenReturn(Collections.singletonList(batch));
            when(stockRepository.getQuantityByLocation("DRY-001", "SHELF")).thenReturn(50);

            stockService.moveStockToLocation("DRY-001", 50, "SHELF");

            verify(stockRepository).updateBatchQuantity(1, 50);
            verify(stockRepository).updateStockLevel("DRY-001", 50, "SHELF");
            verify(mockObserver).onStockReplenished(eq("DRY-001"), anyString(), eq(50));
        }

        @Test
        @DisplayName("Skips expired batches")
        void expiredBatches_skipped() {
            StockBatch expired = new StockBatch(1, "MILK-01", LocalDate.now().minusMonths(1), LocalDate.now().minusDays(1), 50);
            StockBatch valid = new StockBatch(2, "MILK-01", LocalDate.now().minusMonths(1), LocalDate.now().plusDays(10), 30);

            when(stockRepository.getBatchesByItemCode("MILK-01")).thenReturn(Arrays.asList(expired, valid));
            when(stockRepository.getQuantityByLocation("MILK-01", "SHELF")).thenReturn(20);

            stockService.moveStockToLocation("MILK-01", 20, "SHELF");

            verify(stockRepository, never()).updateBatchQuantity(eq(1), anyInt());
            verify(stockRepository).updateBatchQuantity(2, 20);
            verify(stockRepository).updateStockLevel("MILK-01", 20, "SHELF");
        }

        @Test
        @DisplayName("Does nothing when all batches expired")
        void allExpired_movesNothing() {
            StockBatch expired = new StockBatch(1, "MILK-01", LocalDate.now(), LocalDate.now().minusDays(5), 10);
            when(stockRepository.getBatchesByItemCode("MILK-01")).thenReturn(Collections.singletonList(expired));

            stockService.moveStockToLocation("MILK-01", 10, "SHELF");

            verify(stockRepository, never()).updateBatchQuantity(anyInt(), anyInt());
            verify(stockRepository, never()).updateStockLevel(anyString(), anyInt(), anyString());
        }
    }

    @Nested
    @DisplayName("addBatch()")
    class AddBatchTests {

        @Test
        @DisplayName("Adds batch and notifies observer")
        void addBatch_notifiesObserver() {
            StockBatchRequest request = new StockBatchRequest("DRY-001", 100, LocalDate.now().plusYears(1));
            when(stockRepository.getTotalQuantityFromAllSources("DRY-001")).thenReturn(100);
            when(itemRepository.findByCode("DRY-001"))
                    .thenReturn(Optional.of(new Item("DRY-001", "Rice", BigDecimal.TEN, "FOOD", 10)));

            stockService.addBatch(request);

            verify(stockRepository).addBatch(any(StockBatch.class));
            verify(mockObserver).onStockReplenished("DRY-001", "Rice", 100);
        }
    }
}