package com.syos.presentation.cli.factory;

import com.syos.domain.repository.TransactionRepository;
import com.syos.domain.service.Report;
import com.syos.presentation.cli.reports.DailySalesReport;

public class SalesGenerator extends ReportGenerator {
    private final TransactionRepository transRepo;
    private String transactionType;  // Filter

    public SalesGenerator(TransactionRepository tr) {
        this.transRepo = tr;
    }

    // set filter before generating
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public Report createReport() {
        //would have the bill here, but since template using this object is returned
        return new DailySalesReport(transRepo, transactionType);
    }
}