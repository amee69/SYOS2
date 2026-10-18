package com.syos.presentation.cli.reports;

import com.syos.domain.service.Report;
import com.syos.infrastructure.util.FileHelper;
import java.time.LocalDate;

public abstract class AbstractReport implements Report {

    // template Methodm, defines the skeleton
    @Override
    public final void generate() {
        StringBuilder report = new StringBuilder();

        //  generate header (same for all reports)
        report.append(generateHeader());

        //  generate body (different for each report)
        report.append(generateBody());

        //  generate footer (same for all reports)
        report.append(generateFooter());

        //  display
        System.out.println(report.toString());

        //  save to file
        saveToFile(report.toString());
    }

    // hook methods  can be customized if needed
    protected String generateHeader() {
        return "\n" + "=".repeat(70) + "\n" +
                "     " + getReportTitle() + " - " + LocalDate.now() + "\n" +
                "=".repeat(70) + "\n";
    }

    protected String generateFooter() {
        return "=".repeat(70) + "\n" +
                "Generated on: " + LocalDate.now() + "\n" +
                "=".repeat(70) + "\n";
    }

    protected void saveToFile(String content) {
        FileHelper.saveToFile(getFilePrefix(), content);
    }

    // Abstract methods -> each report implement these
    protected abstract String generateBody();
    protected abstract String getReportTitle();
    protected abstract String getFilePrefix();
}