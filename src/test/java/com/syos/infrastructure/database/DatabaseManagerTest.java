package com.syos.infrastructure.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

@DisplayName("DatabaseManager Infrastructure Tests")
class DatabaseManagerTest {

    @Test
    @DisplayName("Singleton Pattern: Should always return the same instance")
    void getInstance_ReturnsSameObject() {
        // Act
        DatabaseManager instance1 = DatabaseManager.getInstance();
        DatabaseManager instance2 = DatabaseManager.getInstance();

        // Assert
        assertNotNull(instance1, "Instance should not be null");
        assertSame(instance1, instance2, "Both instances must be the exact same object in memory");
    }
}