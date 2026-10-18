package com.syos.domain.repository;

import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository {
    void saveTransaction(Bill bill);
    int getNextSerialNumber();

    // existing
    List<Bill> getTransactionsByDate(LocalDate date);

    // get item-level sales for a date (for daily sales report)
    List<ItemSaleSummary> getItemSalesByDate(LocalDate date, String transactionType);

    // fet all transactions (for Bill Report)
    List<Bill> getAllTransactions(String transactionType);

    // inner class for item sales summary
    record ItemSaleSummary(
            String itemCode,
            String itemName,
            int totalQuantity,
            java.math.BigDecimal totalRevenue
    ) {} // to be used in postgress implementation
}