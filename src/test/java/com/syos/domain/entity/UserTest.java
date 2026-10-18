package com.syos.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Tests")
class UserTest {

    @Test
    @DisplayName("Should create user with correct details")
    void constructor_HappyPath() {
        //  Arrange
        String username = "JohnDoe";
        String role = "CASHIER";

        //  Act
        User user = new User(username, role);

        //  Assert
        assertEquals(username, user.getUsername());
        assertEquals(role, user.getRole());
    }

    @Test
    @DisplayName("isManager() should return true for MANAGER role")
    void isManager_ReturnsTrue_ForManager() {
        // Arrange
        User user = new User("admin", "MANAGER");

        // Act & Assert
        assertTrue(user.isManager(), "Should identify MANAGER as manager");
    }

    @Test
    @DisplayName("isManager() should handle case insensitivity")
    void isManager_ReturnsTrue_CaseInsensitive() {
        // Arrange
        // code uses equalsIgnoreCase, so "manager" should work
        User user = new User("admin", "manager");

        // Act & Assert
        assertTrue(user.isManager(), "Should identify 'manager' (lowercase) as manager");
    }

    @Test
    @DisplayName("isManager() should return false for other roles")
    void isManager_ReturnsFalse_ForCashier() {
        // Arrange
        User user = new User("clerk", "CASHIER");

        // Act & Assert
        assertFalse(user.isManager(), "CASHIER is not a manager");
    }

    @Test
    @DisplayName("isManager() should handle null role safely")
    void isManager_HandlesNull_Safely() {
        // Arrange
        // "MANAGER".equalsIgnoreCase(null) returns false (Java safe)
        User user = new User("somethingRoleuser", null);

        // Act & Assert
        assertFalse(user.isManager(), "Null role should not be manager and should not crash");
    }
}