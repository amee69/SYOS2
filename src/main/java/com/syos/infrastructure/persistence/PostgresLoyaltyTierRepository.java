package com.syos.infrastructure.persistence;

import com.syos.domain.entity.LoyaltyTier;
import com.syos.domain.repository.LoyaltyTierRepository;
import com.syos.infrastructure.database.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class PostgresLoyaltyTierRepository implements LoyaltyTierRepository {

    @Override
    public List<LoyaltyTier> findAllOrderedByMinPurchasesDesc() {
        String sql = "SELECT tier_name, min_purchases, points_per_100, discount_percent " +
                "FROM loyalty_tiers ORDER BY min_purchases DESC";

        List<LoyaltyTier> tiers = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tiers.add(mapToLoyaltyTier(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch loyalty tiers", e);
        }

        return tiers;
    }

    @Override
    public Optional<LoyaltyTier> findTierByPurchaseAmount(BigDecimal totalPurchases) {
        // Get the highest tier where min_purchases <= totalPurchases
        String sql = "SELECT tier_name, min_purchases, points_per_100, discount_percent " +
                "FROM loyalty_tiers " +
                "WHERE min_purchases <= ? " +
                "ORDER BY min_purchases DESC " +
                "LIMIT 1";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, totalPurchases);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToLoyaltyTier(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tier for purchase amount: " + totalPurchases, e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<LoyaltyTier> findByName(String tierName) {
        String sql = "SELECT tier_name, min_purchases, points_per_100, discount_percent " +
                "FROM loyalty_tiers WHERE tier_name = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tierName.toUpperCase());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapToLoyaltyTier(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find tier: " + tierName, e);
        }

        return Optional.empty();
    }

    private LoyaltyTier mapToLoyaltyTier(ResultSet rs) throws SQLException {
        return new LoyaltyTier(
                rs.getString("tier_name"),
                rs.getBigDecimal("min_purchases"),
                rs.getInt("points_per_100"),
                rs.getBigDecimal("discount_percent")
        );
    }
}