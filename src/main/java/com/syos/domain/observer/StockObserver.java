package com.syos.domain.observer;


public interface StockObserver {
     //called when stock falls below reorder level
    void onLowStock(String itemCode, String itemName, int currentStock, int reorderLevel);
      //called when stock is completely depleted
    void onOutOfStock(String itemCode, String itemName);
      //called when stock is replenished after being low
    void onStockReplenished(String itemCode, String itemName, int newStock);

}