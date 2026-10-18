package com.syos.infrastructure.persistence;

import com.syos.domain.entity.LoyaltyTier;
import com.syos.infrastructure.database.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PostgresLoyaltyTierRepositoryTest {

    private PostgresLoyaltyTierRepository repository;
    private MockedStatic<DatabaseManager> dbMock;

    @Mock private DatabaseManager dbManager;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        repository = new PostgresLoyaltyTierRepository();

        // mock static db singleton
        dbMock = mockStatic(DatabaseManager.class);
        dbMock.when(DatabaseManager::getInstance).thenReturn(dbManager);

        // setup chain
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @AfterEach
    void tearDown() {

        dbMock.close();
    }

    @Test
    void findAllOrderedByMinPurchasesDesc_ReturnsList() throws SQLException {
        // one row then stop
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        mockTierData();

        List<LoyaltyTier> tiers = repository.findAllOrderedByMinPurchasesDesc();

        assertEquals(1, tiers.size());
        assertEquals("Gold", tiers.get(0).getTierName());
    }

    @Test
    void findTierByPurchaseAmount_Found() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        mockTierData();

        Optional<LoyaltyTier> result = repository.findTierByPurchaseAmount(new BigDecimal("600"));

        assertTrue(result.isPresent());
        assertEquals("Gold", result.get().getTierName());
    }

    @Test
    void testException_ThrowsRuntime() throws SQLException {
        // force db crash
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB Error"));

        assertThrows(RuntimeException.class, () -> repository.findAllOrderedByMinPurchasesDesc());
    }

    // helper tier rows
    private void mockTierData() throws SQLException {
        when(resultSet.getString("tier_name")).thenReturn("Gold");
        when(resultSet.getBigDecimal("min_purchases")).thenReturn(new BigDecimal("1000"));
        when(resultSet.getInt("points_per_100")).thenReturn(10);
        when(resultSet.getBigDecimal("discount_percent")).thenReturn(new BigDecimal("5.0"));
    }
}