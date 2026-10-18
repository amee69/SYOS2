package com.syos.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    @DisplayName("Constructor should correctly set code, name, and description")
    void testConstructorAndGetters_HappyPath() {
        //  Arrange
        String expectedCode = "BEV";
        String expectedName = "Beverages";
        String expectedDesc = "Tea, Coffee";

        // Act
        Category category = new Category(expectedCode, expectedName, expectedDesc);

        //  ASSERT
        //  verify that the getters return exactly what is put in.
        assertEquals(expectedCode, category.getCode(), "Code should match constructor input");
        assertEquals(expectedName, category.getName(), "Name should match constructor input");
        assertEquals(expectedDesc, category.getDescription(), "Description should match constructor input");
    }

    @Test
    @DisplayName("Category should handle empty strings")
    void testConstructor_WithEmptyStrings() {
        // edge Case: Empty strings are valid in Java, checking if they crash the app
        Category category = new Category("", "", "");

        assertNotNull(category);
        assertEquals("", category.getCode());
    }

    @Test
    @DisplayName("Category should accept null values without crashing")
    void testConstructor_WithNulls() {
        // edge Case: Nulls
        //  verify that the object is created and getters return null (not crashing)
        Category category = new Category(null, null, null);

        assertNotNull(category);
        assertNull(category.getCode());
        assertNull(category.getName());
    }
}