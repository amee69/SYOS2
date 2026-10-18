package com.syos.infrastructure.persistence;

import com.syos.domain.entity.Customer;
import com.syos.domain.repository.CustomerRepository;
import com.syos.infrastructure.database.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;





public class PostgresCustomerRepository implements CustomerRepository {

    @Override
    public Optional<Customer> findByEmail(String email) {
        String sql = "SELECT customer_id, email, full_name, loyalty_points, total_purchases, customer_tier FROM customers WHERE email = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getInt("loyalty_points"),
                        rs.getBigDecimal("total_purchases"),
                        rs.getString("customer_tier")
                );
                return Optional.of(customer);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer by email: " + email, e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Customer> findById(int customerId) {
        String sql = "SELECT customer_id, email, full_name, loyalty_points, total_purchases, customer_tier FROM customers WHERE customer_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getInt("loyalty_points"),
                        rs.getBigDecimal("total_purchases"),
                        rs.getString("customer_tier")
                );
                return Optional.of(customer);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer by ID: " + customerId, e);
        }

        return Optional.empty();
    }

    @Override
    public void updateLoyaltyPoints(int customerId, int newPoints) {
        String sql = "UPDATE customers SET loyalty_points = ? WHERE customer_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newPoints);
            stmt.setInt(2, customerId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update loyalty points for customer: " + customerId, e);
        }
    }

    @Override
    public void updateTotalPurchases(int customerId, BigDecimal newTotal) {
        // Dynamic tier upgrade using loyalty_tiers table
        String sql = """
        UPDATE customers 
        SET total_purchases = ?,
            customer_tier = (
                SELECT tier_name 
                FROM loyalty_tiers 
                WHERE min_purchases <= ? 
                ORDER BY min_purchases DESC 
                LIMIT 1
            )
        WHERE customer_id = ?
    """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, newTotal);
            stmt.setBigDecimal(2, newTotal);
            stmt.setInt(3, customerId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update total purchases for customer: " + customerId, e);
        }
    }


    @Override
    public void save(Customer customer) {
        // Get starting tier from loyalty_tiers table dynamically
        String sql = """
        INSERT INTO customers (email, phone, full_name, password, loyalty_points, total_purchases, customer_tier)
        VALUES (?, ?, ?, ?, ?, ?, 
            (SELECT tier_name FROM loyalty_tiers WHERE min_purchases <= 0 ORDER BY min_purchases DESC LIMIT 1)
        )
    """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getEmail());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getFullName());
            stmt.setString(4, customer.getPassword());
            stmt.setInt(5, 0);
            stmt.setBigDecimal(6, BigDecimal.ZERO);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save customer: " + customer.getEmail(), e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM customers WHERE email = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check email existence: " + email, e);
        }

        return false;
    }

    @Override
    public Optional<Customer> authenticate(String email, String password) {
        String sql = "SELECT * FROM customers WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email.toLowerCase());
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("email"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("password"),
                        rs.getInt("loyalty_points"),
                        rs.getBigDecimal("total_purchases"),
                        rs.getString("customer_tier")
                );
                return Optional.of(customer);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to authenticate customer: " + email, e);
        }

        return Optional.empty();
    }
}