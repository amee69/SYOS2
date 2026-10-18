package com.syos.presentation.cli.reports;

import com.syos.application.service.StockService;
import com.syos.domain.entity.Item;
import com.syos.domain.repository.ItemRepository;
import java.util.List;

public class ReorderReport extends AbstractReport {
    private final StockService stockService;
    private final ItemRepository itemRepo;
//    private static final int REORDER_THRESHOLD = 50;

    public ReorderReport(StockService stockService, ItemRepository itemRepo) {
        this.stockService = stockService;
        this.itemRepo = itemRepo;
    }

    @Override
    protected String generateBody() {
        StringBuilder body = new StringBuilder();
//        body.append(String.format("%-10s %-20s %-15s %-15s\n",
//                "Code", "Item Name", "Current Stock", "Action"));
//        body.append("-".repeat(70)).append("\n");
        body.append(String.format("%-10s %-20s %-15s %-15s %-15s\n",
                "Code", "Item Name", "Current", "Min Level", "Action"));
        body.append("-".repeat(70)).append("\n");

        List<Item> items = itemRepo.findAll();
        boolean foundAlerts = false;

        for (Item item : items) {
            int totalStock = stockService.getTotalStockCount(item.getItemCode());


            if (totalStock < item.getReorderLevel()) {
                foundAlerts = true;

                String action = (totalStock == 0) ? "CRITICAL!" : "REORDER NOW";

                body.append(String.format("%-10s %-20s %-15d %-15d %-15s\n",
                        item.getItemCode(),
                        truncate(item.getName(), 20),
                        totalStock,
                        item.getReorderLevel(), // Show the target level
                        action));
            }
        }

        if (!foundAlerts) {
            body.append("       All stock levels are Good\n");
        }

        return body.toString();
    }

    private String truncate(String str, int width) {
        if (str.length() > width) return str.substring(0, width - 3) + "...";
        return str;
    }

    @Override
    protected String getReportTitle() {
        return "REORDER ALERT REPORT";
    }

    @Override
    protected String getFilePrefix() {
        return "ReorderAlert";
    }
}