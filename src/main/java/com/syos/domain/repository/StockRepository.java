package com.syos.domain.repository;

import com.syos.domain.entity.StockBatch;
import java.util.List;

public interface StockRepository {
    void addBatch(StockBatch batch);
    List<StockBatch> getBatchesByItemCode(String itemCode);
    void updateStockLevel(String itemCode, int quantity, String stockType);
    int getTotalQuantityFromAllSources(String itemCode);

    // use 'decrement' logic for thread-safe business operations
    //use db to calculate updating logic
    void updateBatchQuantity(int batchId, int decrementAmount);

    //same logic for threadsaftey
    int getQuantityByLocation(String itemCode, String stockType);

    //   billingService related
    void reduceStock(String itemCode, int quantity, String stockType);

    default void setCurrentUser(String username) {}
    default void setCurrentBillSerial(Integer billSerial) {}
    default void clearContext() {}

} //stock type is location



