package com.syos.infrastructure.persistence;

import com.syos.domain.entity.Category;
import com.syos.infrastructure.database.DatabaseManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostgresCategoryRepositoryTest {

    // mock all the sql stuff
    @Mock DatabaseManager mockDbManager;
    @Mock Connection mockConnection;
    @Mock Statement mockStatement;
    @Mock PreparedStatement mockPrepStmt;
    @Mock ResultSet mockResultSet;

    @Test
    @DisplayName("findAll: Should return a list of categories")
    void testFindAll() throws SQLException {
        // use try-with-resources for the static mock so it closes after
        try (MockedStatic<DatabaseManager> mockedStatic = mockStatic(DatabaseManager.class)) {

            // setup the chain: db manager -> connection -> statement -> resultset
            mockedStatic.when(DatabaseManager::getInstance).thenReturn(mockDbManager);
            when(mockDbManager.getConnection()).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);
            when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

            // fake the rows in the db
            // first call true (has row), second call false (end of list)
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getString("category_code")).thenReturn("C01");
            when(mockResultSet.getString("category_name")).thenReturn("Beverages");
            when(mockResultSet.getString("description")).thenReturn("Drinks");

            // run the method
            PostgresCategoryRepository repo = new PostgresCategoryRepository();
            List<Category> result = repo.findAll();

            // check if  got the list back
            assertEquals(1, result.size());
            assertEquals("Beverages", result.get(0).getName());
        }
    }

    @Test
    @DisplayName("findByCode: Should find category if it exists")
    void testFindByCode_Found() throws SQLException {
        try (MockedStatic<DatabaseManager> mockedStatic = mockStatic(DatabaseManager.class)) {

            mockedStatic.when(DatabaseManager::getInstance).thenReturn(mockDbManager);
            when(mockDbManager.getConnection()).thenReturn(mockConnection);

            // this one use prepareStatement not createStatement
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPrepStmt);
            when(mockPrepStmt.executeQuery()).thenReturn(mockResultSet);

            // simulate finding one row
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("category_code")).thenReturn("C01");
            when(mockResultSet.getString("category_name")).thenReturn("Beverages");
            when(mockResultSet.getString("description")).thenReturn("Drinks");

            PostgresCategoryRepository repo = new PostgresCategoryRepository();
            Optional<Category> result = repo.findByCode("C01");

            // seeing if its present
            assertTrue(result.isPresent());
            assertEquals("Beverages", result.get().getName());
        }
    }

    @Test
    @DisplayName("existsByCode: Should return true if count is > 0")
    void testExistsByCode_True() throws SQLException {
        try (MockedStatic<DatabaseManager> mockedStatic = mockStatic(DatabaseManager.class)) {

            mockedStatic.when(DatabaseManager::getInstance).thenReturn(mockDbManager);
            when(mockDbManager.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPrepStmt);
            when(mockPrepStmt.executeQuery()).thenReturn(mockResultSet);

            // simulate count(*) returning 1
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt(1)).thenReturn(1);

            PostgresCategoryRepository repo = new PostgresCategoryRepository();
            boolean exists = repo.existsByCode("C01");

            assertTrue(exists);
        }
    }

    @Test
    @DisplayName("Edge Case: Should throw runtime exception if sql fails")
    void testFindAll_Exception() throws SQLException {
        try (MockedStatic<DatabaseManager> mockedStatic = mockStatic(DatabaseManager.class)) {

            mockedStatic.when(DatabaseManager::getInstance).thenReturn(mockDbManager);
            when(mockDbManager.getConnection()).thenReturn(mockConnection);
            when(mockConnection.createStatement()).thenReturn(mockStatement);

            // force it to crash with sql error
            when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("DB Down"));

            PostgresCategoryRepository repo = new PostgresCategoryRepository();

            // verify it wraps the sql exception in a runtime one
            assertThrows(RuntimeException.class, () -> repo.findAll());
        }
    }
}