package com.syos.domain.observer;
import java.util.ArrayList;
import java.util.List;


//  stock level monitor: subject in the observer pattern.
//  Monitors stock levels and notifies observers when thresholds are crossed.
//   Open/Closed for new observer can add

//business logic hcnce in domain layer

public class StockLevelMonitor implements StockSubject {

    private final List<StockObserver> observers;//list

//BROADCASTER

    public StockLevelMonitor() {

        this.observers = new ArrayList<>(); //constr with above listed list
    }

    @Override
    public void addObserver(StockObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    //added since observer pattern correctness
    @Override
    public void removeObserver(StockObserver observer) {
        observers.remove(observer);
    }



//-------------------------

     //check stock level with custom reorder threshold
    public void checkStockLevel(String itemCode, String itemName, int currentStock, int reorderLevel) {
        if (currentStock == 0) {
            notifyOutOfStock(itemCode, itemName);
        } else if (currentStock < reorderLevel) {
            notifyLowStock(itemCode, itemName, currentStock, reorderLevel);
            //private mthd, moved to private to make it easier to see
        }
    }




    //below has loops to run since there can be many observers
    public void notifyReplenishment(String itemCode, String itemName, int newStock) {
        for (StockObserver observer : observers) {
            observer.onStockReplenished(itemCode, itemName, newStock);
        }
    }


    //private helpers

    private void notifyLowStock(String itemCode, String itemName, int currentStock, int reorderLevel) {
        for (StockObserver observer : observers) {
            observer.onLowStock(itemCode, itemName, currentStock, reorderLevel);
        }
    }

    private void notifyOutOfStock(String itemCode, String itemName) {
        for (StockObserver observer : observers) {
            observer.onOutOfStock(itemCode, itemName);
        }
    }


}