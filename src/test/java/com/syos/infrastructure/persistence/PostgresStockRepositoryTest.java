package com.syos.infrastructure.persistence;

import com.syos.domain.entity.StockBatch;
import com.syos.domain.exception.InsufficientStockException;
import com.syos.infrastructure.database.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostgresStockRepositoryTest {

    @Mock private DatabaseManager mockDbManager;
    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockStatement;
    @Mock private ResultSet mockResultSet;

    private MockedStatic<DatabaseManager> mockedStaticDb;
    private PostgresStockRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        repository = new PostgresStockRepository();

        mockedStaticDb = mockStatic(DatabaseManager.class);
        mockedStaticDb.when(DatabaseManager::getInstance).thenReturn(mockDbManager);


        when(mockDbManager.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    }

    @AfterEach
    void tearDown() {
        mockedStaticDb.close();
    }

    @Test
    void addBatch_Success() throws SQLException {
        StockBatch batch = new StockBatch(0, "ABC-123", LocalDate.now(), LocalDate.now().plusDays(5), 10);

        repository.addBatch(batch);

        verify(mockStatement).executeUpdate();
        verify(mockStatement).setString(1, "ABC-123");
        verify(mockStatement).setInt(4, 10);
    }

    @Test
    void getBatches_ReturnsList() throws SQLException {
        // Specific stubbing for this test (SELECT)
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getInt("batch_id")).thenReturn(1);
        when(mockResultSet.getString("item_code")).thenReturn("DRY-001");
        when(mockResultSet.getDate("purchase_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getDate("expiry_date")).thenReturn(Date.valueOf(LocalDate.now().plusDays(10)));
        when(mockResultSet.getInt("quantity")).thenReturn(50);

        List<StockBatch> batches = repository.getBatchesByItemCode("DRY-001");

        assertEquals(1, batches.size());
        assertEquals("DRY-001", batches.get(0).getItemCode());
    }

    @Test
    void updateStockLevel_Success() throws SQLException {
        repository.updateStockLevel("ABC-123", 5, "SHELF");

        verify(mockStatement).executeUpdate();
    }

    @Test
    void reduceStock_Success() throws SQLException {
        // Return 1 to simulate 1 row updated
        when(mockStatement.executeUpdate()).thenReturn(1);

        repository.reduceStock("ABC-123", 2, "SHELF");

        verify(mockStatement).executeUpdate();
    }

    @Test
    void reduceStock_NotEnough_ThrowsException() throws SQLException {
        // Return 0 to simulate failure (WHERE condition failed)
        when(mockStatement.executeUpdate()).thenReturn(0);

        assertThrows(InsufficientStockException.class, () ->
                repository.reduceStock("ABC-123", 100, "SHELF")
        );
    }

    @Test
    void getTotalQuantity_ReturnsSum() throws SQLException {
        // Specific stubbing for SELECT
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(150);

        int total = repository.getTotalQuantityFromAllSources("DRY-001");

        assertEquals(150, total);
    }

    @Test
    void updateBatchQuantity_Fail() throws SQLException {
        when(mockStatement.executeUpdate()).thenReturn(0);

        Exception e = assertThrows(RuntimeException.class, () ->
                repository.updateBatchQuantity(1, 10)
        );

        assertTrue(e.getMessage().contains("Insufficient warehouse inventory"));
    }

    @Test
    void getQuantityByLocation_Store_Success() throws SQLException {
        // Specific stubbing for SELECT
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(99);

        int qty = repository.getQuantityByLocation("ABC", "STORE");

        assertEquals(99, qty);
    }

    @Test
    void getQuantityByLocation_Shelf_Success() throws SQLException {
        // Specific stubbing for SELECT
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(20);

        int qty = repository.getQuantityByLocation("ABC", "SHELF");

        assertEquals(20, qty);
    }
}