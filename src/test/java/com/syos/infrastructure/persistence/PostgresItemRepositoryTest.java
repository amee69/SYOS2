package com.syos.infrastructure.persistence;

import com.syos.domain.entity.Item;
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

class PostgresItemRepositoryTest {

    private PostgresItemRepository repository;
    private MockedStatic<DatabaseManager> dbMock;

    @Mock private DatabaseManager dbManager;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private Statement statement;
    @Mock private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        repository = new PostgresItemRepository();

        // mock static db singleton
        dbMock = mockStatic(DatabaseManager.class);
        dbMock.when(DatabaseManager::getInstance).thenReturn(dbManager);

        // setup jdbc chain (prepared stmt + normal stmt)
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.createStatement()).thenReturn(statement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
    }

    @AfterEach
    void tearDown() {
        // cleanup
        dbMock.close();
    }

    @Test
    void findByCode_Found() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        mockItemData();

        Optional<Item> result = repository.findByCode("ITM001");

        assertTrue(result.isPresent());
        assertEquals("ITM001", result.get().getItemCode());
    }

    @Test
    void findAll_ReturnsList() throws SQLException {
        // one row then stop
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        mockItemData();

        List<Item> items = repository.findAll();

        assertEquals(1, items.size());
        assertEquals("Test Item", items.get(0).getName());
    }

    @Test
    void save_Success() throws SQLException {
        Item item = new Item("ITM01", "Bread", new BigDecimal("100"), "CAT1", 5);

        repository.save(item);

        // check it ran
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void existsByCode_True() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1); // count = 1

        assertTrue(repository.existsByCode("ITM01"));
    }

    @Test
    void testException_ThrowsRuntime() throws SQLException {
        // force db crash
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Conn error"));

        assertThrows(RuntimeException.class, () -> repository.findByCode("ITM01"));
    }

    // helper for item rows
    private void mockItemData() throws SQLException {
        when(resultSet.getString("item_code")).thenReturn("ITM001");
        when(resultSet.getString("name")).thenReturn("Test Item");
        when(resultSet.getBigDecimal("unit_price")).thenReturn(new BigDecimal("10.50"));
        when(resultSet.getString("category_code")).thenReturn("CAT_A");
        when(resultSet.getInt("reorder_threshold")).thenReturn(10);
    }
}