package com.syos.presentation.cli.factory;

import com.syos.application.service.StockService;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.service.Report;
import com.syos.presentation.cli.reports.ReshelvingReport;


 // decouples report creation from report usage
 // ReportController doesn't need to know how to create reports

public class ReshelvingGenerator extends ReportGenerator {

    private final StockService stockService;
    private final ItemRepository itemRepo;

    public ReshelvingGenerator(StockService stockService, ItemRepository itemRepo) {
        this.stockService = stockService;
        this.itemRepo = itemRepo;
    }

    //createreoprt factory method
    @Override
    public Report createReport() {
        return new ReshelvingReport(stockService, itemRepo);
    }
}