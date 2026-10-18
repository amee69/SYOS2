package com.syos.presentation.cli.factory;

import com.syos.domain.repository.TransactionRepository;
import com.syos.domain.service.Report;
import com.syos.presentation.cli.reports.DailySalesReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalesGenerator Factory Tests")
class SalesGeneratorTest {

    // mocking the repo since the generator needs it for the constructor
    @Mock
    private TransactionRepository transactionRepository;

    @Test
    @DisplayName("Should create a DailySalesReport instance")
    void createReport_ReturnsCorrectInstance() {
        // arrange
        SalesGenerator generator = new SalesGenerator(transactionRepository);

        // calling setter to cover that line of code
        generator.setTransactionType("POS");

        // act
        Report result = generator.createReport();

        // assert
        assertNotNull(result);
        // simple check to make sure the factory actually made a Sales report
        assertTrue(result instanceof DailySalesReport);
    }
}