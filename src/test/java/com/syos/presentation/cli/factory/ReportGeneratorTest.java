package com.syos.presentation.cli.factory;

import com.syos.domain.service.Report;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportGeneratorTest {

    @Mock
    Report mockReport;

    @Test
    @DisplayName("Happy Path: run() should create report and call generate()")
    void testRun_ExecutesReport() {
        //  create a concrete instance of the anon abst class
        ReportGenerator generator = new ReportGenerator() {
            @Override
            public Report createReport() {
                return mockReport; // Return  mock instead of a real report
            }
        };

        //  Act
        generator.run();

        //  Assert: Verify that report.generate was  called
        verify(mockReport).generate();
    }

    @Test
    @DisplayName("Edge Case: If createReport returns null, run() should do nothing (no crash)")
    void testRun_NullReport() {
        //  Create a generator that fails to create a report (returns null)
        ReportGenerator generator = new ReportGenerator() {
            @Override
            public Report createReport() {
                return null;
            }
        };

        //  Act no nulls
        generator.run();

        // = Assert non since no error will occir
    }
}