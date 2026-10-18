package com.syos.presentation.cli.observer;


 // observer Pattern

import com.syos.domain.observer.StockObserver;

//ui things
public class ConsoleStockAlertObserver implements StockObserver {

    @Override
    public void onLowStock(String itemCode, String itemName, int currentStock, int reorderLevel) {
        System.out.println();
        System.out.println("═".repeat(60));
        System.out.println("  STOCK WARNING: LOW STOCK DETECTED");
        System.out.println("═".repeat(60));
        System.out.println("  Item Code    : " + itemCode);
        System.out.println("  Item Name    : " + itemName);
        System.out.println("  Current Stock: " + currentStock + " units");
        System.out.println("  Reorder Level: " + reorderLevel + " units");
        System.out.println("  Action       : Please reorder this item soon!");
        System.out.println("═".repeat(60));
        System.out.println();
    }

    @Override
    public void onOutOfStock(String itemCode, String itemName) {
        System.out.println();
        System.out.println("╔" + "═".repeat(58) + "╗");
        System.out.println("║   CRITICAL: OUT OF STOCK                                ║");
        System.out.println("╠" + "═".repeat(58) + "╣");
        System.out.println("║  Item Code: " + padRight(itemCode, 44) + "║");
        System.out.println("║  Item Name: " + padRight(itemName, 44) + "║");
        System.out.println("║  Status   : COMPLETELY OUT OF STOCK                       ║");
        System.out.println("║  Action   : IMMEDIATE REORDER REQUIRED!                   ║");
        System.out.println("╚" + "═".repeat(58) + "╝");
        System.out.println();
    }

    @Override
    public void onStockReplenished(String itemCode, String itemName, int newStock) {
        System.out.println();
        System.out.println();
        System.out.println("✓ DISTRIBUTION COMPLETE : stock replenished");
        System.out.println("  Item: " + itemName + " (" + itemCode + ")");
        System.out.println("  Target Location Stock: " + newStock + " units");
        System.out.println();
    }

    private String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) {
            return text.substring(0, length);
        }
        return text + " ".repeat(length - text.length());
    }

}