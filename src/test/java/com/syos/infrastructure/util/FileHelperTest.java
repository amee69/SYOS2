package com.syos.infrastructure.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileHelper Utility Tests")
class FileHelperTest {

    private static final String DIRECTORY = "reports/";
    private static final String TEST_PREFIX = "UnitTestFile";

    @Test
    @DisplayName("Should create a file with the correct content")
    void saveToFile_CreatesFile() throws IOException {
        //  Arrange
        String content = "This is a test content line.";

        //  Act
        FileHelper.saveToFile(TEST_PREFIX, content);

        // Assert
        File folder = new File(DIRECTORY);
        assertTrue(folder.exists(), "Reports directory should exist");

        // find  file  search by prefix
        File[] foundFiles = folder.listFiles((dir, name) -> name.startsWith(TEST_PREFIX));

        assertNotNull(foundFiles, "Should find files in directory");
        assertTrue(foundFiles.length > 0, "Should find at least one file starting with " + TEST_PREFIX);

        // Verify Content
        File createdFile = foundFiles[0];
        String fileContent = Files.readString(createdFile.toPath());
        assertEquals(content, fileContent, "File content should match what was written");
    }

    // delete file after
    @AfterEach
    void tearDown() {
        File folder = new File(DIRECTORY);
        if (folder.exists()) {
            File[] files = folder.listFiles((dir, name) -> name.startsWith(TEST_PREFIX));
            if (files != null) {
                for (File f : files) {
                    f.delete(); // delete  test file
                }
            }
        }
    }
}