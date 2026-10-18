package com.syos.infrastructure.persistence;

import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import com.syos.domain.repository.TransactionRepository;
import com.syos.infrastructure.database.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class PostgresTransactionRepository implements TransactionRepository {

    @Override
    public int getNextSerialNumber() {
        String sql = "SELECT nextval('bill_serial_seq')";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get next serial number", e);
        }

        return 1001;
    }

    @Override //not using, but keep in case..
    public List<Bill> getTransactionsByDate(LocalDate date) {
        List<Bill> dailyBills = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE DATE(bill_date) = ? ORDER BY bill_serial";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Bill bill = buildBillFromResultSet(rs);
                dailyBills.add(bill);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get transactions for date: " + date, e);
        }

        return dailyBills;
    }

//
//     Get itemlevel sales summary for daily Sales Report
//      date The date to get sales for
//      transactionType null for ALL, pos for in-store, web for online
//
    @Override
    public List<ItemSaleSummary> getItemSalesByDate(LocalDate date, String transactionType) {
        List<ItemSaleSummary> sales = new ArrayList<>();

        String sql = """
            SELECT 
                ti.item_code,
                ti.item_name,
                SUM(ti.quantity) as total_qty,
                SUM(ti.total_item_price) as total_revenue
            FROM transaction_items ti
            JOIN transactions t ON ti.bill_serial = t.bill_serial
            
            WHERE DATE(t.bill_date) = ?
            """ + (transactionType != null ? "AND t.transaction_type = ? " : "") + """
            GROUP BY ti.item_code, ti.item_name
            ORDER BY total_revenue DESC
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            if (transactionType != null) {
                stmt.setString(2, transactionType);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                sales.add(new ItemSaleSummary(
                        rs.getString("item_code"),
                        rs.getString("item_name"),
                        rs.getInt("total_qty"),
                        rs.getBigDecimal("total_revenue")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get item sales for date: " + date, e);
        }

        return sales;
    }


//      get all transactions for bill report
//       transactiontype null for ALL, "POS" for in-store, "WEB" for online

    @Override
    public List<Bill> getAllTransactions(String transactionType) {
        List<Bill> allBills = new ArrayList<>();

        String sql = "SELECT * FROM transactions " +
                (transactionType != null ? "WHERE transaction_type = ? " : "") +
                "ORDER BY bill_date DESC, bill_serial DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (transactionType != null) {
                stmt.setString(1, transactionType);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Bill bill = buildBillFromResultSet(rs);
                allBills.add(bill);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all transactions", e);
        }

        return allBills;
    }


      //helper method to build Bill from ResultSet (holds it neatly basucal

    private Bill buildBillFromResultSet(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("bill_date");
        LocalDateTime billDate = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

        return new Bill.Builder()
                .setSerial(rs.getInt("bill_serial"))
                .setBillDate(billDate)
                .setItems(new ArrayList<>())
                .setTotal(rs.getBigDecimal("total_price"))
                .setDiscount(rs.getBigDecimal("total_discount"))
                .setPaymentType(rs.getString("payment_type"))
                .setTransactionType(rs.getString("transaction_type"))
                .setCashierUsername(rs.getString("cashier_username"))
                .setCashTendered(rs.getBigDecimal("cash_tendered"))
                .setChangeAmount(rs.getBigDecimal("change_amount"))
                .build();
    }


    //save to transaction table and transaction_items table
    @Override
    public void saveTransaction(Bill bill) {
        String insertBill = """
            INSERT INTO transactions 
            (bill_serial, bill_date, transaction_type, customer_id, cashier_username,
             total_price, total_discount, discount_description, loyalty_points_used, 
             loyalty_points_earned, net_total, payment_type, cash_tendered, change_amount)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        String insertItems = """
            INSERT INTO transaction_items 
            (bill_serial, item_code, item_name, quantity, unit_price, total_item_price)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false); //incase failing happens

            try (PreparedStatement billStmt = conn.prepareStatement(insertBill);
                 PreparedStatement itemStmt = conn.prepareStatement(insertItems)) {

                // Bill data
                billStmt.setInt(1, bill.getSerialNumber());
                billStmt.setTimestamp(2, Timestamp.valueOf(bill.getBillDate()));
                billStmt.setString(3, bill.getTransactionType());
                billStmt.setObject(4, bill.getCustomerId());
                billStmt.setString(5, bill.getCashierUsername());
                billStmt.setBigDecimal(6, bill.getTotalFullPrice());
                billStmt.setBigDecimal(7, bill.getTotalDiscount());
                billStmt.setString(8, bill.getDiscountDescription());
                billStmt.setInt(9, bill.getLoyaltyPointsUsed());
                billStmt.setInt(10, bill.getLoyaltyPointsEarned());
                billStmt.setBigDecimal(11, bill.getNetTotal());
                billStmt.setString(12, bill.getPaymentType());
                billStmt.setBigDecimal(13, bill.getCashTendered());
                billStmt.setBigDecimal(14, bill.getChangeAmount());

                billStmt.executeUpdate();

                // Bill items
                for (BillItem item : bill.getBillItems()) {
                    itemStmt.setInt(1, bill.getSerialNumber());
                    itemStmt.setString(2, item.getItemCode());
                    itemStmt.setString(3, item.getItemName());
                    itemStmt.setInt(4, item.getQuantity());
                    itemStmt.setBigDecimal(5, item.getUnitPrice());
                    itemStmt.setBigDecimal(6, item.getTotalPrice());
                    itemStmt.addBatch();
                }

                itemStmt.executeBatch();
                conn.commit(); //if okay then commuiit

//            } catch (SQLException e) {
//                conn.rollback();
//                throw new RuntimeException("Failed to save transaction: " + bill.getSerialNumber(), e);
//            }
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("SQL ERROR for bill " + bill.getSerialNumber() + ": " + e.getMessage());
                System.err.println("SQL State: " + e.getSQLState());
                throw new RuntimeException("Failed to save transaction: " + bill.getSerialNumber(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database connection error while saving transaction", e);
        }
    }
}