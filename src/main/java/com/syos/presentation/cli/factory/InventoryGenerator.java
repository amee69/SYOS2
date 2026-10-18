package com.syos.presentation.cli.factory;

import com.syos.application.service.StockService;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.service.Report;
import com.syos.presentation.cli.reports.InventoryReport;

public class InventoryGenerator extends ReportGenerator {
    private final StockService stockService;
    private final ItemRepository itemRepo;

    public InventoryGenerator(StockService ss, ItemRepository ir) {
        this.stockService = ss;
        this.itemRepo = ir;
    }

    @Override
    public Report createReport() {
        return new InventoryReport(stockService, itemRepo);
    }
}