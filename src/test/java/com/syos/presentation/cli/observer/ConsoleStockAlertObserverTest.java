package com.syos.presentation.cli.observer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleStockAlertObserverTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream standardOut = System.out;
    private ConsoleStockAlertObserver observer;

    @BeforeEach
    public void setUp() {
        //  redirect system.out to our variable so we can read it
        System.setOut(new PrintStream(outputStreamCaptor));
        observer = new ConsoleStockAlertObserver();
    }

    @AfterEach
    public void tearDown() {
        //  restore standard System.out so other tests print normally
        System.setOut(standardOut);
    }

    @Test
    @DisplayName("onLowStock should print correct warning details")
    void onLowStock_PrintsWarning() {
        // Act
        observer.onLowStock("DRY-001", "Rice", 15, 50);

        // Convert the captured output to a String
        String output = outputStreamCaptor.toString();

        // Assert: Check that key phrases exist in the output
        assertTrue(output.contains("  STOCK WARNING: LOW STOCK DETECTED"));
        assertTrue(output.contains("DRY-001"));
        assertTrue(output.contains("Rice"));
        assertTrue(output.contains("15 units"));
        assertTrue(output.contains("50 units"));
    }

    @Test
    @DisplayName("onOutOfStock should print critical alert box")
    void onOutOfStock_PrintsCriticalAlert() {
        // Act
        observer.onOutOfStock("DAIRY-002", "Milk");

        String output = outputStreamCaptor.toString();

        // Assert
        assertTrue(output.contains(" CRITICAL: OUT OF STOCK"));
        assertTrue(output.contains("Item Code: DAIRY-002"));
        assertTrue(output.contains("Item Name: Milk"));
        assertTrue(output.contains("IMMEDIATE REORDER REQUIRED!"));
    }

    @Test
    @DisplayName("onStockReplenished should print success message")
    void onStockReplenished_PrintsSuccess() {
        // Act
        observer.onStockReplenished("DRY-001", "Rice", 100);

        String output = outputStreamCaptor.toString();

        // Assert
        assertTrue(output.contains("✓ DISTRIBUTION COMPLETE : stock replenished"));
        assertTrue(output.contains("Rice (DRY-001)"));
        assertTrue(output.contains("100 units"));
    }

    @Test
    @DisplayName("Formatting: Long names should be handled safely")
    void onOutOfStock_HandlesLongNames() {
        // act Pass a very long name to test the padRight/substring logic
        String longName = "ThisIsAVeryLongProductNameThatExceedsTheLimitOfTheBox";
        observer.onOutOfStock("CODE-999", longName);

        String output = outputStreamCaptor.toString();

        // assert Should not crash, and should contain the code
        assertTrue(output.contains("CODE-999"));
        // the output might be truncated, but as long as it printed the alert frame, it passed.
        assertTrue(output.contains("CRITICAL: OUT OF STOCK"));
    }
}