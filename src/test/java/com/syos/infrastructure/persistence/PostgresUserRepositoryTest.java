package com.syos.infrastructure.persistence;

import com.syos.domain.entity.User;
import com.syos.infrastructure.database.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PostgresUserRepositoryTest {

    private PostgresUserRepository repository;
    private MockedStatic<DatabaseManager> dbMock;

    @Mock private DatabaseManager dbManager;
    @Mock private Connection connection;
    @Mock private PreparedStatement statement;
    @Mock private ResultSet resultSet;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        repository = new PostgresUserRepository();

        // mocking the static database manager
        dbMock = mockStatic(DatabaseManager.class);
        dbMock.when(DatabaseManager::getInstance).thenReturn(dbManager);

        // setting up the chain: manager -> connection -> statement -> resultset
        when(dbManager.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @AfterEach
    void tearDown() {
        // close the static mock
        dbMock.close();
    }

    @Test
    void authenticate_Success() throws SQLException {
        // simulate finding a user
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("username")).thenReturn("admin");
        when(resultSet.getString("role")).thenReturn("ADMIN");

        Optional<User> result = repository.authenticate("admin", "password123");

        assertTrue(result.isPresent());
        assertEquals("admin", result.get().getUsername());
        assertEquals("ADMIN", result.get().getRole());
    }

    @Test
    void authenticate_InvalidCredentials() throws SQLException {
        // simulate no user found
        when(resultSet.next()).thenReturn(false);

        Optional<User> result = repository.authenticate("wrongUser", "wrongPass");

        assertTrue(result.isEmpty());
    }

    @Test
    void authenticate_DatabaseError() throws SQLException {
        // simulate a db crash
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Connection lost"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            repository.authenticate("user", "pass");
        });

        assertTrue(exception.getMessage().contains("Database Auth Error"));
    }
}