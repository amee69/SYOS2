package com.syos.presentation.cli.reports;

import com.syos.domain.repository.TransactionRepository;
import com.syos.domain.repository.TransactionRepository.ItemSaleSummary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


 // display the items sold (with name and code), total quantity, total revenue for the given day

public class DailySalesReport extends AbstractReport {
    private final TransactionRepository transRepos;
    private final String transactionType;

    public DailySalesReport(TransactionRepository transRepo, String transactionType) {
        this.transRepos = transRepo;
        this.transactionType = transactionType;
    }

    @Override
    protected String generateBody() {
        StringBuilder body = new StringBuilder();

        // Get item-level sales summary with filter
        List<ItemSaleSummary> sales = transRepos.getItemSalesByDate(LocalDate.now(), transactionType);

        // Header
        body.append(String.format("%-10s %-25s %10s %15s\n",
                "Code", "Item Name", "Qty Sold", "Revenue (LKR)"));
        body.append("-".repeat(70)).append("\n");

        if (sales.isEmpty()) {
            body.append("              No sales recorded for today.\n");
        } else {
            BigDecimal grandTotal = BigDecimal.ZERO;
            int totalItems = 0;

            for (ItemSaleSummary sale : sales) {
                body.append(String.format("%-10s %-25s %10d %15.2f\n",
                        sale.itemCode(),
                        truncate(sale.itemName(), 25),
                        sale.totalQuantity(),
                        sale.totalRevenue()));

                grandTotal = grandTotal.add(sale.totalRevenue());
                totalItems += sale.totalQuantity();
            }

            body.append("-".repeat(70)).append("\n");
            body.append(String.format("%-10s %-25s %10d %15.2f\n",
                    "TOTAL", "", totalItems, grandTotal));
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
        String filterLabel = switch (transactionType) {
            case null -> "ALL CHANNELS";
            case "POS" -> "POS ONLY (In-Store)";
            case "WEB" -> "WEB ONLY (Online)";
            default -> transactionType;
        };
        return "DAILY SALES REPORT - " + filterLabel;
    }

    @Override
    protected String getFilePrefix() {
        String suffix = transactionType != null ? "_" + transactionType : "";
        return "DailySales" + suffix;
    }
}