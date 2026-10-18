package com.syos.domain.observer;


//  subject interface for the observer pattern.
//  defines methods to manage observers

public interface StockSubject {
    void addObserver(StockObserver observer);
    void removeObserver(StockObserver observer);
//    void notifyObservers();
}