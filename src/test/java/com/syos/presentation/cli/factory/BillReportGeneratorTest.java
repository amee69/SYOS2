package com.syos.presentation.cli.factory;

import com.syos.domain.repository.TransactionRepository;
import com.syos.domain.service.Report;
import com.syos.presentation.cli.reports.BillReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillReportGenerator Factory Tests")
class BillReportGeneratorTest {

    // mocking the repo since the generator constructor needs it
    @Mock
    private TransactionRepository transactionRepository;

    @Test
    @DisplayName("Should create a BillReport instance correctly")
    void createReport_ReturnsBillReportInstance() {
        // arrange
        BillReportGenerator generator = new BillReportGenerator(transactionRepository);

        // calling setter to cover that line (even if null is fine for the test)
        generator.setTransactionType("WEB");

        // act
        Report result = generator.createReport();

        // assert
        assertNotNull(result);
        // check if the factory actually returned the right type of object
        assertTrue(result instanceof BillReport);
    }
}