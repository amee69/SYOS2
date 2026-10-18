package com.syos.presentation.cli.reports;

import com.syos.domain.entity.Bill;
import com.syos.domain.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;


 // Bill Report all customer billss

public class BillReport extends AbstractReport {
    private final TransactionRepository transRepo;
    private final String transactionType;  //  null = ALL, POS, or WEB
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public BillReport(TransactionRepository transRepo, String transactionType) {
        this.transRepo = transRepo;
        this.transactionType = transactionType;
    }

    @Override
    protected String generateBody() {
        StringBuilder body = new StringBuilder();

        // Header
        body.append(String.format("%-8s %-18s %-6s %-12s %12s %12s\n",
                "Bill #", "Date/Time", "Type", "Cashier", "Total", "Net"));
        body.append("-".repeat(70)).append("\n");

        // Get transactions with filter
        List<Bill> bills = transRepo.getAllTransactions(transactionType);

        if (bills.isEmpty()) {
            body.append("              No transactions found.\n");
        } else {
            BigDecimal grandTotal = BigDecimal.ZERO;

            for (Bill bill : bills) {
                String dateStr = bill.getBillDate().format(DATE_FMT);
                String cashier = bill.getCashierUsername();
                if (cashier == null || cashier.isEmpty()) cashier = "-";

                body.append(String.format("%-8d %-18s %-6s %-12s %12.2f %12.2f\n",
                        bill.getSerialNumber(),
                        dateStr,
                        bill.getTransactionType(),
                        truncate(cashier, 12),
                        bill.getTotalFullPrice(),
                        bill.getNetTotal()));

                grandTotal = grandTotal.add(bill.getNetTotal());
            }

            body.append("-".repeat(70)).append("\n");
            body.append(String.format("TOTAL TRANSACTIONS: %d %40s %12.2f\n",
                    bills.size(), "TOTAL REVENUE:", grandTotal));
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
            case null -> "ALL TRANSACTIONS";
            case "POS" -> "POS TRANSACTIONS ONLY";
            case "WEB" -> "WEB TRANSACTIONS ONLY";
            default -> transactionType;
        };
        return "BILL REPORT - " + filterLabel;
    }

    @Override
    protected String getFilePrefix() {
        String suffix = transactionType != null ? "_" + transactionType : "";
        return "BillReport" + suffix;
    }
}