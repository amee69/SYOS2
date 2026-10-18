package com.syos.infrastructure.web;

import com.syos.application.factory.ValidationServiceFactory;
import com.syos.application.service.*;
import com.syos.domain.observer.StockObserver;
import com.syos.domain.observer.StockLevelMonitor;
import com.syos.infrastructure.database.DatabaseManager;
import com.syos.infrastructure.persistence.*;
import com.syos.presentation.cli.observer.ConsoleStockAlertObserver;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import com.syos.infrastructure.concurrency.CheckoutQueueService;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        System.out.println("SYOS: Initializing application context...");

        // Infrastructure — repositories
        var realStockRepo = new PostgresStockRepository();
        var stockRepo = new LoggingStockRepositoryProxy(realStockRepo);
        var itemRepo = new PostgresItemRepository();
        var transRepo = new PostgresTransactionRepository();
        var userRepo = new PostgresUserRepository();
        var customerRepo = new PostgresCustomerRepository();
        var categoryRepo = new PostgresCategoryRepository();
        var loyaltyTierRepo = new PostgresLoyaltyTierRepository();

        // Domain
        var stockMonitor = new StockLevelMonitor();
        var billFileWriter = new BillFileWriter();

        // Application services
        var stockService = new StockService(stockRepo, itemRepo, stockMonitor);
        StockObserver stockAlert = new ConsoleStockAlertObserver();
        stockService.addStockObserver(stockAlert);

        var billingService = new BillingService(
                transRepo, customerRepo, stockRepo,
                itemRepo, stockService, loyaltyTierRepo, billFileWriter
        );

        var checkoutQueue = new CheckoutQueueService(billingService);
        ctx.setAttribute("checkoutQueue", checkoutQueue);

        var customerService = new CustomerService(customerRepo);
        var itemFacade = new ItemManagementFacade(itemRepo, categoryRepo);
        var validationFactory = new ValidationServiceFactory(itemRepo, stockRepo);

        // store everything in ServletContext so Servlets can access them
        ctx.setAttribute("billingService", billingService);
        ctx.setAttribute("stockService", stockService);
        ctx.setAttribute("customerService", customerService);
        ctx.setAttribute("itemFacade", itemFacade);
        ctx.setAttribute("itemRepo", itemRepo);
        ctx.setAttribute("stockRepo", stockRepo);
        ctx.setAttribute("transRepo", transRepo);
        ctx.setAttribute("userRepo", userRepo);
        ctx.setAttribute("customerRepo", customerRepo);
        ctx.setAttribute("loyaltyTierRepo", loyaltyTierRepo);
        ctx.setAttribute("validationFactory", validationFactory);

        System.out.println("SYOS: Application context ready.");
    }

//    @Override
//    public void contextDestroyed(ServletContextEvent sce) {
//        CheckoutQueueService queue = (CheckoutQueueService)
//                sce.getServletContext().getAttribute("checkoutQueue");
//        if (queue != null) queue.shutdown();
//        System.out.println("SYOS: Application context destroyed.");
//    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        CheckoutQueueService queue = (CheckoutQueueService) sce.getServletContext().getAttribute("checkoutQueue");
        if (queue != null) queue.shutdown();

        DatabaseManager.getInstance().shutdown();
        System.out.println("SYOS: Application context destroyed.");
    }



}