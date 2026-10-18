package com.syos.infrastructure.persistence;

import com.syos.domain.entity.StockBatch;
import com.syos.domain.repository.StockRepository;
import com.syos.infrastructure.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class PostgresStockRepository implements StockRepository {

    @Override
    public void addBatch(StockBatch batch) {
        String sql = "INSERT INTO stock_batches (item_code, purchase_date, expiry_date, quantity) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, batch.getItemCode());
            stmt.setDate(2, Date.valueOf(batch.getPurchaseDate()));
            stmt.setDate(3, Date.valueOf(batch.getExpiryDate()));
            stmt.setInt(4, batch.getQuantity());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add stock batch for item: " + batch.getItemCode(), e);
        }
    }

    @Override
    public List<StockBatch> getBatchesByItemCode(String itemCode) {
        List<StockBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM stock_batches WHERE item_code = ? AND quantity > 0 ORDER BY expiry_date ASC, purchase_date ASC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                batches.add(new StockBatch(
                        rs.getInt("batch_id"),
                        rs.getString("item_code"),
                        rs.getDate("purchase_date").toLocalDate(),
                        rs.getDate("expiry_date").toLocalDate(),
                        rs.getInt("quantity")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve batches for item: " + itemCode, e);
        }

        return batches;
    }

    @Override
    public void updateStockLevel(String itemCode, int quantity, String stockType) {
        String sql = "INSERT INTO inventory (item_code, stock_type, quantity) VALUES (?, ?, ?) " +
                "ON CONFLICT (item_code, stock_type) " +
                "DO UPDATE SET quantity = inventory.quantity + EXCLUDED.quantity";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            stmt.setString(2, stockType);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update stock level for item: " + itemCode, e);
        }
    }

    @Override
    public void reduceStock(String itemCode, int quantity, String stockType) {
        String sql = "UPDATE inventory SET quantity = quantity - ? " +
                "WHERE item_code = ? AND stock_type = ? AND quantity >= ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setString(2, itemCode);
            stmt.setString(3, stockType);
            stmt.setInt(4, quantity);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new com.syos.domain.exception.InsufficientStockException(itemCode, quantity, 0);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error reducing stock for item: " + itemCode, e);
        }
    }

    @Override
    public int getTotalQuantityFromAllSources(String itemCode) {
        String sql = "SELECT " +
                "(SELECT COALESCE(SUM(quantity), 0) FROM stock_batches WHERE item_code = ?) + " +
                "(SELECT COALESCE(SUM(quantity), 0) FROM inventory WHERE item_code = ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            stmt.setString(2, itemCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to calculate total inventory for item: " + itemCode, e);
        }

        return 0;
    }

    @Override
    public void updateBatchQuantity(int batchId, int decrementAmount) {
        String sql = "UPDATE stock_batches SET quantity = quantity - ? WHERE batch_id = ? AND quantity >= ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, decrementAmount);
            stmt.setInt(2, batchId);
            stmt.setInt(3, decrementAmount);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Insufficient warehouse inventory in batch " + batchId);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Stock allocation failed for batch: " + batchId, e);
        }
    }

    @Override
    public int getQuantityByLocation(String itemCode, String stockType) {
        String sql;
        if ("STORE".equalsIgnoreCase(stockType)) {
            sql = "SELECT COALESCE(SUM(quantity), 0) FROM stock_batches WHERE item_code = ? AND quantity > 0";
        } else {
            sql = "SELECT quantity FROM inventory WHERE item_code = ? AND stock_type = ?";
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            if (!"STORE".equalsIgnoreCase(stockType)) {
                stmt.setString(2, stockType.toUpperCase());
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch stock levels for item: " + itemCode, e);
        }

        return 0;
    }
}