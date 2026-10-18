package com.syos.presentation.cli.factory;

import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.StockRepository;
import com.syos.domain.service.Report;
import com.syos.presentation.cli.reports.BatchStockReport;


//factory for creating BatchStockReport


public class BatchStockGenerator extends ReportGenerator {
    private final StockRepository stockRepo;
    private final ItemRepository itemRepo;

    public BatchStockGenerator(StockRepository stockRepo, ItemRepository itemRepo) {
        this.stockRepo = stockRepo;
        this.itemRepo = itemRepo;
    }

    @Override
    public Report createReport() {

        return new BatchStockReport(stockRepo, itemRepo);
    }
}