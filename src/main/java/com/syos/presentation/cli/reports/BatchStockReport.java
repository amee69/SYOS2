package com.syos.presentation.cli.reports;

import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.StockRepository;
import com.syos.domain.entity.Item;

import java.util.List;


//  Batch Stock Report
//  Provide detaisls of the current stock batch-wise
//   code, date of purchase, quantity, date of expiry

public class BatchStockReport extends AbstractReport {
    private final StockRepository stockRepo;
    private final ItemRepository itemRepo;

    public BatchStockReport(StockRepository stockRepo, ItemRepository itemRepo) {
        this.stockRepo = stockRepo;
        this.itemRepo = itemRepo;
    }

    @Override
    protected String generateBody() {
        StringBuilder body = new StringBuilder();

        // Header
        body.append(String.format("%-10s %-20s %-12s %-12s %10s\n",
                "Code", "Item Name", "Purchased", "Expires", "Quantity"));
        body.append("-".repeat(70)).append("\n");

        List<Item> items = itemRepo.findAll();
        boolean hasBatches = false;

        for (Item item : items) {
            List<StockBatch> batches = stockRepo.getBatchesByItemCode(item.getItemCode());

            for (StockBatch batch : batches) {
                if (batch.getQuantity() > 0) {
                    hasBatches = true;
                    body.append(String.format("%-10s %-20s %-12s %-12s %10d\n",
                            batch.getItemCode(),
                            truncate(item.getName(), 20),
                            batch.getPurchaseDate(),
                            batch.getExpiryDate(),
                            batch.getQuantity()));
                }
            }
        }

        if (!hasBatches) {
            body.append("              No stock batches in warehouse.\n");
        }

        return body.toString();
    }

    private String truncate(String str, int width) {
        if (str == null) return "";
        if (str.length() > width) return str.substring(0, width - 3) + "...";
        return str;
    }

    @Override
    protected String getReportTitle() {
        return "BATCH-WISE STOCK REPORT (Warehouse)";
    }

    @Override
    protected String getFilePrefix() {
        return "BatchStock";
    }
}