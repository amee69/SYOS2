package com.syos.infrastructure.persistence;

import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoggingStockRepositoryProxy Tests")
class LoggingStockRepositoryProxyTest {

    @Mock
    private StockRepository realRepository;

    private LoggingStockRepositoryProxy proxy;

    @BeforeEach
    void setUp() {
        // no reflection needed anymore. The class is now safe.
        proxy = new LoggingStockRepositoryProxy(realRepository);
    }

    @Test
    @DisplayName("Write Operations: Should delegate all modifications to the real repository")
    void shouldDelegateWriteOperations() {
        StockBatch batch = new StockBatch(1, "DRY-001", LocalDate.now(), LocalDate.now().plusMonths(6), 100);

        proxy.addBatch(batch);
        proxy.reduceStock("DRY-001", 10, "SHELF");
        proxy.updateStockLevel("DRY-001", 50, "SHELF");
        proxy.updateBatchQuantity(1, 25);

        verify(realRepository).addBatch(batch);
        verify(realRepository).reduceStock("DRY-001", 10, "SHELF");
        verify(realRepository).updateStockLevel("DRY-001", 50, "SHELF");
        verify(realRepository).updateBatchQuantity(1, 25);
    }

    @Test
    @DisplayName("Read Operations: Should delegate queries and return correct data")
    void shouldDelegateReadOperations() {
        List<StockBatch> mockBatches = Collections.singletonList(new StockBatch(1, "A", LocalDate.now(), LocalDate.now(), 10));
        when(realRepository.getBatchesByItemCode("DRY-001")).thenReturn(mockBatches);
        when(realRepository.getTotalQuantityFromAllSources("DRY-001")).thenReturn(100);
        when(realRepository.getQuantityByLocation("DRY-001", "SHELF")).thenReturn(50);

        assertEquals(mockBatches, proxy.getBatchesByItemCode("DRY-001"));
        assertEquals(100, proxy.getTotalQuantityFromAllSources("DRY-001"));
        assertEquals(50, proxy.getQuantityByLocation("DRY-001", "SHELF"));

        verify(realRepository).getBatchesByItemCode("DRY-001");
        verify(realRepository).getTotalQuantityFromAllSources("DRY-001");
        verify(realRepository).getQuantityByLocation("DRY-001", "SHELF");
    }

    @Test
    @DisplayName("Context Management: Should accept context data safely")
    void shouldManageContextSafely() {
        proxy.setCurrentUser("cashier1");
        proxy.setCurrentBillSerial(12345);
        proxy.clearContext();
        proxy.setCurrentUser(null);
        proxy.setCurrentBillSerial(null);
    }

    @Test
    @DisplayName("Edge Case: Zero Quantity should still reach the real repository")
    void shouldDelegateZeroQuantityOperations() {
        proxy.reduceStock("DRY-001", 0, "SHELF");
        verify(realRepository).reduceStock("DRY-001", 0, "SHELF");
    }
}