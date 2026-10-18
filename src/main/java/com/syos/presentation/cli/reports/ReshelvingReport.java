package com.syos.presentation.cli.reports;

import com.syos.application.service.StockService;
import com.syos.domain.entity.Item;
import com.syos.domain.repository.ItemRepository;

import java.util.List;


//  template Method: extends AbstractReport
//  factory Method: created by ReshelvingGenerator

public class ReshelvingReport extends AbstractReport {

    private final StockService stockService;
    private final ItemRepository itemRepo;

    // Target stock level for shelf
    //private static final int TARGET_SHELF_LEVEL = 50;

    public ReshelvingReport(StockService stockService, ItemRepository itemRepo) {
        this.stockService = stockService;
        this.itemRepo = itemRepo;
    }


     //  method is called by AbstractReport.generate()
      // builds the main content of the report.

    @Override
    protected String generateBody() {
        StringBuilder body = new StringBuilder();

        // Print table header
        body.append(String.format("%-10s %-20s %12s %12s %12s\n",
                "Code", "Item Name", "Shelf Qty", "Warehouse", "To Reshelve"));
        body.append("-".repeat(70)).append("\n");

        // Get all items from database
        List<Item> items = itemRepo.findAll();

        boolean foundItemsToReshelve = false;
        int totalUnitsToReshelve = 0;

        // Check each item
        for (Item item : items) {
            String code = item.getItemCode();

            // Get current stock levels
            int shelfStock = stockService.getStockByLocation(code, "SHELF");
            int warehouseStock = stockService.getStockByLocation(code, "STORE");

            // Check if shelf needs restocking
            if (shelfStock < item.getReorderLevel() && warehouseStock > 0) {
                foundItemsToReshelve = true;

                int needed = item.getReorderLevel() - shelfStock;
                int toReshelve = Math.min(needed, warehouseStock);

                totalUnitsToReshelve += toReshelve;

                // Add row to report
                body.append(String.format("%-10s %-20s %12d %12d %12d\n",
                        code,
                        truncate(item.getName(), 20),
                        shelfStock,
                        warehouseStock,
                        toReshelve));
            }
        }

        // If nothing needs reshelving
        if (!foundItemsToReshelve) {
            body.append("\n         All shelves are adequately stocked!\n");
            body.append("           No reshelving required at this time.\n\n");
        } else {
            // Print summary
            body.append("-".repeat(70)).append("\n");
            body.append(String.format("TOTAL UNITS TO RESHELVE: %d\n", totalUnitsToReshelve));
            body.append("\n Action: Move the above quantities from Warehouse to Shelf\n");
        }

        return body.toString();
    }


      //Helper method to truncate long strings

    private String truncate(String str, int width) {
        if (str == null) return "";
        if (str.length() > width) return str.substring(0, width - 3) + "...";
        return str;
    }


     // Report title shown in header

    @Override
    protected String getReportTitle() {
        return "END OF DAY RESHELVING REPORT";
    }


      //Filename prefix when saving to file

    @Override
    protected String getFilePrefix() {
        return "Reshelving";
    }
}