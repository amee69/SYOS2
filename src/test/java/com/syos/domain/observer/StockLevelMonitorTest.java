package com.syos.domain.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;



 //Tests: addObserver, removeObserver, checkStockLevel, notifyReplenishment

@ExtendWith(MockitoExtension.class)
@DisplayName("StockLevelMonitor (Observer Pattern) Tests")
class StockLevelMonitorTest {

    @Mock
    private StockObserver mockObserver1;

    @Mock
    private StockObserver mockObserver2;

    private StockLevelMonitor monitor;

    @BeforeEach
    void setUp() {
        monitor = new StockLevelMonitor();
    } //clean slate setuyp

    @Nested
    @DisplayName("addObserver() Tests")
    class AddObserverTests {

        @Test
        @DisplayName("Should register observer that receives notifications")
        void addObserver_RegistersObserver_ReceivesNotifications() {
            // Arrange
            monitor.addObserver(mockObserver1);

            // Act - trigger low stock
            monitor.checkStockLevel("DRY-001", "Rice", 10, 50);

            // Assert
            verify(mockObserver1).onLowStock("DRY-001", "Rice", 10, 50);
        }

        @Test
        @DisplayName("Should not add null observer")
        void addObserver_NullObserver_DoesNothing() {
            // Act
            monitor.addObserver(null);

            // Assert - no exception thrown, verify by triggering notification
            monitor.checkStockLevel("DRY-001", "Rice", 10, 50);
            // If null was added, this would cause NullPointerException
        }

        @Test
        @DisplayName("Should not add duplicate observer")
        void addObserver_DuplicateObserver_AddedOnlyOnce() {
            // Arrange
            monitor.addObserver(mockObserver1);
            monitor.addObserver(mockObserver1); // Add same observer again

            // Act
            monitor.checkStockLevel("DRY-001", "Rice", 10, 50);

            // Assert - should only be notified once
            verify(mockObserver1, times(1)).onLowStock(anyString(), anyString(), anyInt(), anyInt());
        //should give coorect number of invocations
        }
    }

    @Nested
    @DisplayName("removeObserver() Tests")
    class RemoveObserverTests {

        @Test
        @DisplayName("Should remove observer that no longer receives notifications")
        void removeObserver_RemovesObserver_NoLongerReceivesNotifications() {
            // Arrange
            monitor.addObserver(mockObserver1);
            monitor.removeObserver(mockObserver1);

            // Act
            monitor.checkStockLevel("DRY-001", "Rice", 10, 50);

            // Assert
            verify(mockObserver1, never()).onLowStock(anyString(), anyString(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("Should handle removing non-existent observer")
        void removeObserver_NonExistentObserver_DoesNothing() {
            // Act - should not throw exception
            monitor.removeObserver(mockObserver1);

            // Assert - no exception
        }
    }

    @Nested
    @DisplayName("checkStockLevel() - Low Stock Tests")
    class LowStockTests {

        @Test
        @DisplayName("Should notify when stock is below threshold")
        void checkStockLevel_BelowThreshold_NotifiesLowStock() {
            // Arrange
            monitor.addObserver(mockObserver1);

            // Act - stock (10) < threshold (50)
            monitor.checkStockLevel("DRY-001", "Rice", 10, 50);

            // Assert
            verify(mockObserver1).onLowStock("DRY-001", "Rice", 10, 50);
        }

        @Test
        @DisplayName("Should not notify when stock equals threshold")
        void checkStockLevel_AtThreshold_DoesNotNotify() {
            // Arrange
            monitor.addObserver(mockObserver1);

            // Act - stock (50) = threshold (50)
            monitor.checkStockLevel("DRY-001", "Rice", 50, 50);

            // Assert
            verify(mockObserver1, never()).onLowStock(anyString(), anyString(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("Should not notify when stock is above threshold")
        void checkStockLevel_AboveThreshold_DoesNotNotify() {
            // Arrange
            monitor.addObserver(mockObserver1);

            // Act - stock (100) > threshold (50)
            monitor.checkStockLevel("DRY-001", "Rice", 100, 50);

            // Assert
            verify(mockObserver1, never()).onLowStock(anyString(), anyString(), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("checkStockLevel() - Out of Stock Tests")
    class OutOfStockTests {

        @Test
        @DisplayName("Should notify out of stock when quantity is zero")
        void checkStockLevel_ZeroStock_NotifiesOutOfStock() {
            // Arrange
            monitor.addObserver(mockObserver1);

            // Act
            monitor.checkStockLevel("DRY-001", "Rice", 0, 50);

            // Assert
            verify(mockObserver1).onOutOfStock("DRY-001", "Rice");
        }

        @Test
        @DisplayName("Should not call onLowStock when out of stock")
        void checkStockLevel_ZeroStock_DoesNotCallLowStock() {
            // Arrange
            monitor.addObserver(mockObserver1);

            // Act
            monitor.checkStockLevel("DRY-001", "Rice", 0, 50);

            // Assert - should call onOutOfStock, not onLowStock
            verify(mockObserver1).onOutOfStock("DRY-001", "Rice");
            verify(mockObserver1, never()).onLowStock(anyString(), anyString(), anyInt(), anyInt());
        }
    }

    @Nested
    @DisplayName("notifyReplenishment() Tests")
    class ReplenishmentTests {

        @Test
        @DisplayName("Should notify all observers on replenishment")
        void notifyReplenishment_NotifiesAllObservers() {
            // Arrange
            monitor.addObserver(mockObserver1);
            monitor.addObserver(mockObserver2);

            // Act
            monitor.notifyReplenishment("DRY-001", "Rice", 100);

            // Assert
            verify(mockObserver1).onStockReplenished("DRY-001", "Rice", 100);
            verify(mockObserver2).onStockReplenished("DRY-001", "Rice", 100);
        }
    }

    @Nested
    @DisplayName("Multiple Observers Tests")
    class MultipleObserversTests {

        @Test
        @DisplayName("Should notify all registered observers")
        void checkStockLevel_MultipleObservers_NotifiesAll() {
            // Arrange
            monitor.addObserver(mockObserver1);
            monitor.addObserver(mockObserver2);

            // Act
            monitor.checkStockLevel("DRY-001", "Rice", 10, 50);

            // Assert
            verify(mockObserver1).onLowStock("DRY-001", "Rice", 10, 50);
            verify(mockObserver2).onLowStock("DRY-001", "Rice", 10, 50);
        }

        @Test
        @DisplayName("Removed observer should not receive notification")
        void checkStockLevel_OneRemoved_OnlyRemainingNotified() {
            // Arrange
            monitor.addObserver(mockObserver1);
            monitor.addObserver(mockObserver2);
            monitor.removeObserver(mockObserver1);

            // Act
            monitor.checkStockLevel("DRY-001", "Rice", 10, 50);

            // Assert
            verify(mockObserver1, never()).onLowStock(anyString(), anyString(), anyInt(), anyInt());
            verify(mockObserver2).onLowStock("DRY-001", "Rice", 10, 50);
        }
    }
}
