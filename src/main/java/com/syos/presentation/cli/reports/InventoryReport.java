package com.syos.presentation.cli.reports;

import com.syos.application.service.StockService;
import com.syos.domain.entity.Item;
import com.syos.domain.repository.ItemRepository;
import java.util.List;

public class InventoryReport extends AbstractReport {
    private final StockService stockService;
    private final ItemRepository itemRepo;

    public InventoryReport(StockService stockService, ItemRepository itemRepo) {
        this.stockService = stockService;
        this.itemRepo = itemRepo;
    }

    @Override
    protected String generateBody() {
        StringBuilder body = new StringBuilder();
        body.append(String.format("%-10s %-20s %-10s %-10s %-10s\n",
                "Code", "Item Name", "Store", "Shelf", "Web"));
        body.append("-".repeat(70)).append("\n");

        List<Item> items = itemRepo.findAll();
        for (Item item : items) {
            String code = item.getItemCode();
            int store = stockService.getStockByLocation(code, "STORE");
            int shelf = stockService.getStockByLocation(code, "SHELF");
            int web   = stockService.getStockByLocation(code, "WEB");

            body.append(String.format("%-10s %-20s %-10d %-10d %-10d\n",
                    code,
                    truncate(item.getName(), 20), // Fix alignment
                    store, shelf, web));
        }

        return body.toString();
    }

    // Helper to keep table neat
    private String truncate(String str, int width) {
        if (str.length() > width) return str.substring(0, width - 3) + "...";
        return str;
    }

    @Override
    protected String getReportTitle() {
        return "FULL INVENTORY BREAKDOWN";
    }

    @Override
    protected String getFilePrefix() {
        return "Inventory";
    }
}