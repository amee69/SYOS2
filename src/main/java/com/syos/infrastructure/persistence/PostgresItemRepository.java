package com.syos.infrastructure.persistence;

import com.syos.domain.entity.Item;
import com.syos.domain.repository.ItemRepository;
import com.syos.infrastructure.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class PostgresItemRepository implements ItemRepository {

    @Override
    public Optional<Item> findByCode(String itemCode) {
        String sql = "SELECT * FROM items WHERE item_code = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Item item = new Item(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getBigDecimal("unit_price"),
                        rs.getString("category_code"),
                        rs.getInt("reorder_threshold")
                );
                return Optional.of(item);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching item: " + e);
        }
        return Optional.empty();
    }

    @Override
    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(new Item(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getBigDecimal("unit_price"),
                        rs.getString("category_code"),
                        rs.getInt("reorder_threshold")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public void save(Item item) {
        String sql = "INSERT INTO items (item_code, name, unit_price, category_code, reorder_threshold) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getItemCode());
            stmt.setString(2, item.getName());
            stmt.setBigDecimal(3, item.getUnitPrice());
            stmt.setString(4, item.getCategory());
            stmt.setInt(5, item.getReorderLevel());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving item: " + item.getItemCode(), e);
        }
    }

    @Override
    public boolean existsByCode(String itemCode) {
        String sql = "SELECT COUNT(*) FROM items WHERE item_code = ?";


        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    @Override
    public void delete(String itemCode) {
        String sql = "DELETE FROM items WHERE item_code = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, itemCode);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException("Item code not found.");
            }

        } catch (SQLException e) {
            // SQLState "23503" is code for foreign key vios
            if ("23503".equals(e.getSQLState())) {
                throw new RuntimeException("Cannot delete item: It has existing Stock or Sales records.");
            }
            throw new RuntimeException("Error deleting item: " + e.getMessage(), e);
        }
    }
}