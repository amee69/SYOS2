package com.syos.application.service;

//import com.syos.application.dto.StockReportDto; //   DTO exiss
import com.syos.application.dto.StockBatchRequest;
import com.syos.domain.entity.Item;
import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.StockRepository;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.observer.StockLevelMonitor;
import com.syos.domain.observer.StockObserver;


import java.util.List;


//  application service for stock management.
//  Integrates with observer pattern for stock alerts.

//  observer Pattern (via StockLevelMonitor)
//  repository Pattern (via StockRepository)?

public class StockService {
    private final StockRepository stockRepository;
    private final ItemRepository itemRepository;
    private final StockLevelMonitor stockMonitor;
    private final java.util.concurrent.locks.ReentrantLock stockMovementLock =
            new java.util.concurrent.locks.ReentrantLock();




    public StockService(StockRepository stockRepository, ItemRepository itemRepository,
                        StockLevelMonitor stockMonitor) {
        this.stockRepository = stockRepository;
        this.itemRepository = itemRepository;
        this.stockMonitor = stockMonitor;
    }

    //  observer gets added here

    public void addStockObserver(StockObserver observer) {

        stockMonitor.addObserver(observer);
    }



    //  CORE STOCK LOGIC

    //retrieve total stock count
    public int getTotalStockCount(String itemCode) {

        return stockRepository.getTotalQuantityFromAllSources(itemCode);
    }


    //retrieve stock for specific location
    public int getStockByLocation(String itemCode, String type) {
        return stockRepository.getQuantityByLocation(itemCode, type);
    }


//       triggers the Observer check.

public void checkLowStock(String itemCode) {

    int totalStock = getTotalStockCount(itemCode);

    // get  item to get the real DB threshold
    Item item = itemRepository.findByCode(itemCode).orElse(null);

    if (item != null) {
        // xompare total stock with threshold
        // This will not trigger a warning until total drops below db thresh
        stockMonitor.checkStockLevel(itemCode, item.getName(), totalStock, item.getReorderLevel());
    }
}



    public void moveStockToLocation(String itemCode, int amountToMove, String targetLocation) {
        stockMovementLock.lock();
        try {
            List<StockBatch> availableBatches = stockRepository.getBatchesByItemCode(itemCode);
            int remainingNeeded = amountToMove;
            int expiredSkipped = 0;

            for (StockBatch batch : availableBatches) {
                if (remainingNeeded <= 0) break;

                if (batch.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                    expiredSkipped += batch.getQuantity();
                    continue;
                }

                int amountToAllocate = Math.min(batch.getQuantity(), remainingNeeded);

                try {
                    stockRepository.updateBatchQuantity(batch.getBatchId(), amountToAllocate);
                    remainingNeeded -= amountToAllocate;
                } catch (Exception e) {
                    System.out.println("+ Inventory Update: Adjusting allocation for " + itemCode);
                }
            }

            int actualMoved = amountToMove - remainingNeeded;

            if (expiredSkipped > 0) {
                System.out.println("Skipped " + expiredSkipped + " expired units. Please dispose of expired stock.");
            }

            if (actualMoved == 0) {
                System.out.println("No stock was moved. All available batches are expired.");
            } else if (remainingNeeded > 0) {
                System.out.println("Only " + actualMoved + " units allocated out of " + amountToMove + " requested.");
                stockRepository.updateStockLevel(itemCode, actualMoved, targetLocation);
                String itemName = getItemName(itemCode);
                int newStock = stockRepository.getQuantityByLocation(itemCode, targetLocation);
                stockMonitor.notifyReplenishment(itemCode, itemName, newStock);
            } else {
                stockRepository.updateStockLevel(itemCode, actualMoved, targetLocation);
                String itemName = getItemName(itemCode);
                int newStock = stockRepository.getQuantityByLocation(itemCode, targetLocation);
                stockMonitor.notifyReplenishment(itemCode, itemName, newStock);
            }
        } finally {
            stockMovementLock.unlock();
        }
    }




     // records new stock intakew.



    public void addBatch(StockBatchRequest request) {
        StockBatch batch = new StockBatch(
                0,
                request.getItemCode(),
                java.time.LocalDate.now(),
                request.getExpiryDate(),
                request.getQuantity()
        );

        stockRepository.addBatch(batch);

        int totalStock = getTotalStockCount(batch.getItemCode());
        String itemName = getItemName(batch.getItemCode());
        stockMonitor.notifyReplenishment(batch.getItemCode(), itemName, totalStock);
    }


     // check if sufficient stock is available before a transaction.



    private String getItemName(String itemCode) {
        if (itemRepository != null) {
            return itemRepository.findByCode(itemCode)
                    .map(Item::getName)
                    .orElse(itemCode);
        }
        return itemCode;
    }


}