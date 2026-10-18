package com.syos.presentation.cli;

import com.syos.application.service.StockService;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.StockRepository;
import com.syos.domain.repository.TransactionRepository;
import com.syos.presentation.cli.factory.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


//  controller for all reports in the system
//   Factory Method: Uses generator factories to create reports


public class ReportController {

    private final Map<String, ReportGenerator> generators = new HashMap<>();
    private final Scanner scanner;

    public ReportController(StockService stockservice, ItemRepository itemrepository,
                            TransactionRepository transactionrepository, StockRepository stockrepository,
                            Scanner scanner) {

        this.scanner = scanner;

        generators.put("SALES", new SalesGenerator(transactionrepository));
        generators.put("INVENTORY", new InventoryGenerator(stockservice, itemrepository));
        generators.put("REORDER", new ReorderGenerator(stockservice, itemrepository));
        generators.put("BATCH", new BatchStockGenerator(stockrepository, itemrepository));
        generators.put("BILLS", new BillReportGenerator(transactionrepository));
        generators.put("RESHELVE", new ReshelvingGenerator(stockservice, itemrepository));
    }

    // public methods called by MainMenu


     //  asks for transaction type filter

    //not use execute since this needs filtering, therefore to keep execute command clean, this done
    public void runDailySalesReport() {
        String filter = askForTransactionTypeFilter();
        ReportGenerator generator = generators.get("SALES"); //checks hashmap above
        generator.setTransactionType(filter);
        generator.run(); //the interface genetor method
    }

    public void runReorderReport() {

        execute("REORDER");
    }

    public void runInventoryReport() {
        execute("INVENTORY");
    }

    public void runBatchStockReport() {
        execute("BATCH");
    }


      //  transaction type filter choise

    public void runBillReport() {
        String filter = askForTransactionTypeFilter();
        ReportGenerator generator = generators.get("BILLS");
        generator.setTransactionType(filter);
        generator.run();
    }

    public void runReshelvingReport() {
        execute("RESHELVE");
    }




    //private helpers

    private String askForTransactionTypeFilter() {
        System.out.println("\n--- Filter by Transaction Type ---");
        System.out.println("1. ALL (Combined)");
        System.out.println("2. POS only (In-Store)");
        System.out.println("3. WEB only (Online)");
        System.out.print("Selection: ");

        String choice = scanner.nextLine().trim();

        return switch (choice) {
            case "2" -> "POS";
            case "3" -> "WEB";
            default -> null;  // ALL
        };
    }

    private void execute(String type) { //easy run, like easy to execute for all types of reports
        ReportGenerator reportgeneratorfactory = generators.get(type); //chosen generator

        //reportgeneratorfactory is the chosen generator.
        if (reportgeneratorfactory != null) {
            reportgeneratorfactory.run(); //chosen generator doesnt have their own run method, therefore
                                                //it looks at parent  calss run method and uses it.
        } else {
            System.out.println("!! Error: Report factory for '" + type + "' not found.");
        }
    }
}