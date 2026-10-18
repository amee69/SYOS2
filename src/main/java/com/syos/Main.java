package com.syos;

import com.syos.application.factory.ValidationServiceFactory;
import com.syos.application.service.*;
import com.syos.domain.observer.StockLevelMonitor;
import com.syos.presentation.cli.observer.ConsoleStockAlertObserver;
import com.syos.domain.observer.StockObserver;
import com.syos.domain.repository.StockRepository;
import com.syos.infrastructure.persistence.*;

import com.syos.presentation.cli.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        System.out.println("Initializing system components...");

        // infrastructure layer
        System.out.println("Loading Infrastructure Layer...");
        var scanner = new Scanner(System.in);


        StockRepository realStockRepo = new PostgresStockRepository();
        StockRepository stockRepoTrojanHorse = new LoggingStockRepositoryProxy(realStockRepo);

        // ================================================================

        var transRepo = new PostgresTransactionRepository();
        var itemRepo = new PostgresItemRepository(); //type is itemrepo or postgresitemrepo polymorphism
        var userRepo = new PostgresUserRepository();
        var customerRepo = new PostgresCustomerRepository();
        var categoryRepo = new PostgresCategoryRepository();
        var loyaltyTierRepository = new PostgresLoyaltyTierRepository();
        var validationFactory = new ValidationServiceFactory(itemRepo, stockRepoTrojanHorse);
        var stockMonitor = new StockLevelMonitor(); //999
        var billFileWriter = new BillFileWriter();

        // Domain layer
        System.out.println("Setting up Stock Monitoring (Observer Pattern)");
        StockObserver stockAlertObserver = new ConsoleStockAlertObserver();

        //  application layer
        System.out.println("Initializing Application Services...");
        var stockService = new StockService(stockRepoTrojanHorse, itemRepo, stockMonitor);//999
        stockService.addStockObserver(stockAlertObserver);

        // chain of Responsibility Validators
        //var posValidationService = new ValidationService(itemRepo, stockRepoTrojanHorse, "SHELF");
        //var webValidationService = new ValidationService(itemRepo, stockRepoTrojanHorse, "WEB");

        // Facade for Item Management

        //facade
        var itemFacade = new ItemManagementFacade(itemRepo, categoryRepo);

        var billingService = new BillingService(
                transRepo,
                customerRepo,
                stockRepoTrojanHorse,
                itemRepo, //treated as interface because PostgresItemRepository implements itemrepository
                stockService,
                loyaltyTierRepository,
                billFileWriter
        );

        // Presentation layer
        System.out.println(" [_0_]  Setting up Presentation Layer...");

        var paymentFactory = new PaymentFactory();

        var billingController = new BillingController(
                billingService, itemRepo, scanner, customerRepo,
                validationFactory, loyaltyTierRepository, paymentFactory
        );

        //  Pass facade to WarehouseController
        var warehouseController = new WarehouseController(stockService, scanner, itemRepo, itemFacade);
        var reportController = new ReportController(stockService, itemRepo, transRepo, stockRepoTrojanHorse, scanner);
        var customerService = new CustomerService(customerRepo);
        var customerController = new CustomerController(customerService, scanner);
        var authController = new AuthenticationController(userRepo, customerRepo, scanner);
        //  Main entry point
        // Add this after scanner creation


// Update MainMenu instantiation
        MainMenu menu = new MainMenu(
                authController,
                billingController,
                warehouseController,
                reportController,
                customerController,
                scanner
        );

        System.out.println("\n System initialized successfully!\n");
        menu.display();
    }
}