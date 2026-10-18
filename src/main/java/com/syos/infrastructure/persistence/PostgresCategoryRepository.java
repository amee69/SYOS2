package com.syos.infrastructure.persistence;

import com.syos.domain.entity.Category;
import com.syos.domain.repository.CategoryRepository;
import com.syos.infrastructure.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


  //PostgreSQL implementation of CategoryRepository.

public class PostgresCategoryRepository implements CategoryRepository {

    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY category_name";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(new Category(
                        rs.getString("category_code"),
                        rs.getString("category_name"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {

            throw new RuntimeException("Error fetching categories: ", e);
        }
        return categories;
    }

    @Override
    public Optional<Category> findByCode(String code) {
        String sql = "SELECT * FROM categories WHERE category_code = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(new Category(
                        rs.getString("category_code"),
                        rs.getString("category_name"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching category: ", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByCode(String code) {
        String sql = "SELECT COUNT(*) FROM categories WHERE category_code = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking category: ", e);
        }
        return false;
    }
}