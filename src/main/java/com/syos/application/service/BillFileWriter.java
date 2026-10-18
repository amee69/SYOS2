package com.syos.application.service;

import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;

public class BillFileWriter {

    public void save(Bill bill) {
        try {
            String billsFolder = "/Users/ameer/SYOS2/bills";
            Path billsPath = Paths.get(billsFolder);
            if (!Files.exists(billsPath)) {
                Files.createDirectories(billsPath);
            }

            String timestamp = bill.getBillDate()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s/BILL_%d_%s.txt",
                    billsFolder, bill.getSerialNumber(), timestamp);

            String receipt = buildReceipt(bill);

            Files.writeString(Paths.get(filename), receipt,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Bill saved to: " + filename);

        } catch (Exception e) {
            System.err.println("!! Warning: Could not save bill to file: " + e.getMessage());
        }
    }

    private String buildReceipt(Bill bill) {
        StringBuilder receipt = new StringBuilder();

        // header
        receipt.append("=".repeat(50)).append("\n");
        receipt.append("        SYNEX OUTLET STORE (SYOS)\n");
        receipt.append("           Colombo, Sri Lanka\n");
        receipt.append("=".repeat(50)).append("\n\n");

        // bill info
        receipt.append("Bill #: ").append(bill.getSerialNumber()).append("\n");
        receipt.append("Date  : ").append(bill.getBillDate()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        receipt.append("Type  : ").append(bill.getTransactionType()).append("\n");

        if (bill.getCashierUsername() != null) {
            receipt.append("Cashier: ").append(bill.getCashierUsername()).append("\n");
        }

        // items
        receipt.append("-".repeat(50)).append("\n\n");
        receipt.append(String.format("%-20s %5s %10s %12s\n", "Item", "Qty", "Price", "Total"));
        receipt.append("-".repeat(50)).append("\n");

        for (BillItem item : bill.getBillItems()) {
            receipt.append(String.format("%-20s %5d %10.2f %12.2f\n",
                    truncate(item.getItemName(), 20),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()));
        }

        // totals
        receipt.append("-".repeat(50)).append("\n");
        receipt.append(String.format("%38s %12.2f\n", "Subtotal:", bill.getTotalFullPrice()));

        if (bill.getTotalDiscount().compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("%38s %12.2f\n", "Discount:", bill.getTotalDiscount()));
            if (bill.getDiscountDescription() != null && !bill.getDiscountDescription().isEmpty()) {
                receipt.append("  (").append(bill.getDiscountDescription()).append(")\n");
            }
        }

        receipt.append(String.format("%38s %12.2f\n", "NET TOTAL:", bill.getNetTotal()));
        receipt.append("-".repeat(50)).append("\n");

        // payment
        receipt.append("\nPayment Method: ").append(bill.getPaymentType()).append("\n");
        if (bill.getCashTendered().compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Cash Tendered : LKR %12.2f\n", bill.getCashTendered()));
            receipt.append(String.format("Change        : LKR %12.2f\n", bill.getChangeAmount()));
        }

        // loyalty
        if (bill.getCustomerId() != null) {
            receipt.append("\n").append("-".repeat(50)).append("\n");
            receipt.append("LOYALTY SUMMARY\n");
            receipt.append("-".repeat(50)).append("\n");
            if (bill.getLoyaltyPointsUsed() > 0) {
                receipt.append("Points Redeemed: ").append(bill.getLoyaltyPointsUsed()).append("\n");
            }
            if (bill.getLoyaltyPointsEarned() > 0) {
                receipt.append("Points Earned  : ").append(bill.getLoyaltyPointsEarned()).append("\n");
            }
        }

        // footer
        receipt.append("\n").append("=".repeat(50)).append("\n");
        receipt.append("     Thank you for shopping with us!\n");
        receipt.append("=".repeat(50)).append("\n");

        return receipt.toString();
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}