package com.syos.infrastructure.persistence;

import com.syos.domain.entity.Customer;
import com.syos.infrastructure.database.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PostgresCustomerRepositoryTest {

    private PostgresCustomerRepository repository;
    private MockedStatic<DatabaseManager> dbMock;

    @Mock private DatabaseManager dbManager;
    @Mock private Connection connection;
    @Mock private PreparedStatement preparedStatement;
    @Mock private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        repository = new PostgresCustomerRepository();

        // mock static db singleton
        dbMock = mockStatic(DatabaseManager.class);
        dbMock.when(DatabaseManager::getInstance).thenReturn(dbManager);

        // setup jdbc chain
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @AfterEach
    void tearDown() {
        // important to close static mock
        dbMock.close();
    }

    @Test
    void findByEmail_Found() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        mockCustomerData();

        Optional<Customer> result = repository.findByEmail("test@test.com");

        assertTrue(result.isPresent());
        assertEquals("test@test.com", result.get().getEmail());
    }

    @Test
    void findById_Found() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        mockCustomerData();

        Optional<Customer> result = repository.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getCustomerId());
    }

    @Test
    void updateLoyaltyPoints_Success() throws SQLException {
        repository.updateLoyaltyPoints(1, 100);
        // check if update executed
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void save_Success() throws SQLException {
        Customer customer = new Customer(0, "new@test.com", "New User", "123", "pass", 0, BigDecimal.ZERO, "NEW");

        repository.save(customer);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void existsByEmail_ReturnsTrue() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1); // count(*) = 1

        assertTrue(repository.existsByEmail("test@test.com"));
    }

    @Test
    void authenticate_Success() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        mockCustomerData(); // helper sets password/phone too

        Optional<Customer> result = repository.authenticate("test@test.com", "secret");

        assertTrue(result.isPresent());
    }

    @Test
    void testSqlException_ThrowsRuntime() throws SQLException {
        // force crash
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("DB Error"));

        assertThrows(RuntimeException.class, () -> repository.findById(1));
    }

    // helper to fill all result set fields
    private void mockCustomerData() throws SQLException {
        when(resultSet.getInt("customer_id")).thenReturn(1);
        when(resultSet.getString("email")).thenReturn("test@test.com");
        when(resultSet.getString("full_name")).thenReturn("Test User");
        when(resultSet.getString("phone")).thenReturn("077123");     // added here
        when(resultSet.getString("password")).thenReturn("secret");  // added here
        when(resultSet.getInt("loyalty_points")).thenReturn(50);
        when(resultSet.getBigDecimal("total_purchases")).thenReturn(new BigDecimal("100.00"));
        when(resultSet.getString("customer_tier")).thenReturn("BRONZE");
    }
}