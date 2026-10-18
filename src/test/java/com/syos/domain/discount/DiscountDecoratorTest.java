package com.syos.domain.discount;

import com.syos.domain.service.DiscountCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DiscountDecoratorTest {

    private DiscountCalculator mockCalculator;
    private TestDiscountDecorator decorator;

    //  need a concrete class to test an abstract one
    private static class TestDiscountDecorator extends DiscountDecorator {
        public TestDiscountDecorator(DiscountCalculator calculator) {
            super(calculator);
        }
    }

    @BeforeEach
    void setUp() {
        mockCalculator = Mockito.mock(DiscountCalculator.class);
        decorator = new TestDiscountDecorator(mockCalculator);
    }

    @Test
    @DisplayName("Should delegate calculateDiscount to the wrapped calculator")
    void calculateDiscount_Delegates() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal expectedDiscount = new BigDecimal("100.00");
        when(mockCalculator.calculateDiscount(amount)).thenReturn(expectedDiscount);

        // Act
        BigDecimal actualDiscount = decorator.calculateDiscount(amount);

        // Assert
        assertEquals(expectedDiscount, actualDiscount);
        verify(mockCalculator).calculateDiscount(amount);
    }

    @Test
    @DisplayName("Should delegate getDiscountDescription to the wrapped calculator")
    void getDiscountDescription_Delegates() {
        // Arrange
        String description = "Base Discount";
        when(mockCalculator.getDiscountDescription()).thenReturn(description);

        // Act
        String actualDescription = decorator.getDiscountDescription();

        // Assert
        assertEquals(description, actualDescription);
        verify(mockCalculator).getDiscountDescription();
    }
}