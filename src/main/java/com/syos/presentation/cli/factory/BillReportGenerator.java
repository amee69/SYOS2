package com.syos.presentation.cli.factory;

import com.syos.domain.repository.TransactionRepository;
import com.syos.domain.service.Report;
import com.syos.presentation.cli.reports.BillReport;

//facto
public class BillReportGenerator extends ReportGenerator {
    private final TransactionRepository transRepo;
    private String transactionType;

    public BillReportGenerator(TransactionRepository transRepo) {
        this.transRepo = transRepo;
    }

    //  set filter before generating
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public Report createReport() {
        return new BillReport(transRepo, transactionType);
    }
}