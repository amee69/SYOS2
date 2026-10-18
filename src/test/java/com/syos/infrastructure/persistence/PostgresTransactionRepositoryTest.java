package com.syos.infrastructure.persistence;

import com.syos.domain.entity.Bill;
import com.syos.domain.entity.BillItem;
import com.syos.infrastructure.database.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostgresTransactionRepositoryTest {

    private PostgresTransactionRepository repository;
    private MockedStatic<DatabaseManager> dbMock;

    @Mock private DatabaseManager dbManager;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private Statement statement;
    @Mock private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        repository = new PostgresTransactionRepository();

        //   databaseManagerm ock
        dbMock = mockStatic(DatabaseManager.class);
        dbMock.when(DatabaseManager::getInstance).thenReturn(dbManager);


        when(dbManager.getConnection()).thenReturn(connection);
    }

    @AfterEach
    void tearDown() {
        //  closemocj
        dbMock.close();
    }


    @Test
    void getItemSalesByDate_Success() throws SQLException {
        // mock prepareStatement for reports
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // setup report row
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString("item_code")).thenReturn("P001");
        when(resultSet.getString("item_name")).thenReturn("Soap");
        when(resultSet.getInt("total_qty")).thenReturn(5);
        when(resultSet.getBigDecimal("total_revenue")).thenReturn(new BigDecimal("500.00"));

        var sales = repository.getItemSalesByDate(LocalDate.now(), "POS");

        assertFalse(sales.isEmpty());
        assertEquals("Soap", sales.get(0).itemName());
    }


    @Test
    void getAllTransactions_Success() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true).thenReturn(false);
        mockBillRow();

        List<Bill> results = repository.getAllTransactions("POS");
        assertEquals(1, results.size());
    }

    // helper to map all fields in buildBillFromResultSet method
    private void mockBillRow() throws SQLException {
        when(resultSet.getInt("bill_serial")).thenReturn(101);
        when(resultSet.getTimestamp("bill_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getBigDecimal("total_price")).thenReturn(BigDecimal.TEN);
        when(resultSet.getString("transaction_type")).thenReturn("WEB");
        when(resultSet.getString("payment_type")).thenReturn("CASH");


        when(resultSet.getBigDecimal("total_discount")).thenReturn(BigDecimal.ZERO);
        when(resultSet.getString("cashier_username")).thenReturn("staff1");
        when(resultSet.getBigDecimal("cash_tendered")).thenReturn(BigDecimal.TEN);
        when(resultSet.getBigDecimal("change_amount")).thenReturn(BigDecimal.ZERO);
    }
}