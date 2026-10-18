package com.syos.presentation.cli;

import com.syos.domain.entity.Customer;
import com.syos.domain.entity.User;
import com.syos.presentation.cli.commands.Command;
import com.syos.presentation.cli.commands.MenuCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MainMenu {
    private final Scanner scanner;
    private final AuthenticationController authController;
    private final BillingController billingController;
    private final WarehouseController warehouseController;
    private final ReportController reportController;
    private final CustomerController customerController;

    private final Map<String, Command> reportCommands = new HashMap<>();
    private final Map<String, Command> warehouseCommands = new HashMap<>();

    public MainMenu(AuthenticationController authController,
                    BillingController billingController,
                    WarehouseController warehouseController,
                    ReportController reportController,
                    CustomerController customerController,
                    Scanner scanner) {
        this.authController = authController;
        this.billingController = billingController;
        this.warehouseController = warehouseController;
        this.reportController = reportController;
        this.customerController = customerController;
        this.scanner = scanner;

        initializeReportCommands();
        initializeWarehouseCommands();
    }

    private void initializeReportCommands() {
        reportCommands.put("1", new MenuCommand("Daily Sales Report", reportController::runDailySalesReport));
        reportCommands.put("2", new MenuCommand("Reorder Alert Report", reportController::runReorderReport));
        reportCommands.put("3", new MenuCommand("Inventory Report", reportController::runInventoryReport));
        reportCommands.put("4", new MenuCommand("Batch Stock Report", reportController::runBatchStockReport));
        reportCommands.put("5", new MenuCommand("Bill Report", reportController::runBillReport));
        reportCommands.put("6", new MenuCommand("Reshelving Report", reportController::runReshelvingReport));
    }

    private void initializeWarehouseCommands() {
        warehouseCommands.put("1", new MenuCommand("Receive Batch", warehouseController::manageArrivals));
        warehouseCommands.put("2", new MenuCommand("Distribute Stock", warehouseController::manageDistribution));
        warehouseCommands.put("3", new MenuCommand("Add New Item", warehouseController::addNewItem));
        warehouseCommands.put("4", new MenuCommand("Delete Item", warehouseController::deleteItem));
    }

    public void display() {
        while (true) {
            System.out.println("\n========= SYOS SYSTEMS GATEWAY =========");
            System.out.println("1. POS Terminal (Staff)");
            System.out.println("2. Web Portal (Customer)");
            System.out.println("3. Admin Reports");
            System.out.println("4. Warehouse Management");
            System.out.println("5. Customer Registration");
            System.out.println("0. Exit");
            System.out.print("Selection: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> {
                    User session = authController.loginStaff("CASHIER");  // cahnged
                    if (session != null) {
                        billingController.execute("POS", session.getUsername()); //overload (((((
                    } else {
                        System.out.println("!? Login Failed or Access Denied.");
                    }
                }
                case "2" -> {
                    Customer customer = authController.loginCustomer();
                    if (customer != null) {
                        billingController.executeWebOrder(customer); //overload used (((((
                    } else {
                        System.out.println("! Login Failed. Please check your email and password.");
                        System.out.println("(:0) Don't have an account? Select option 5 to register.");
                    }
                }
                case "3" -> {
                    if (authController.loginStaff("MANAGER") != null)  // CHANGED
                        openReportMenu();
                    else System.out.println("! Access Denied.");
                }
                case "4" -> {
                    if (authController.loginStaff("MANAGER") != null)  // CHANGED
                        openWarehouseMenu();
                    else System.out.println("! Access Denied.");
                }
                case "5" -> customerController.registerCustomer();
                case "0" -> { return; }
                default -> System.out.println("! Invalid selection.");
            }
        }
    }

    private void openReportMenu() {
        while (true) {
            System.out.println("\n========= ADMIN REPORTS =========");
            System.out.println("1. Daily Sales Report (Item Breakdown)");
            System.out.println("2. Reorder Alert Report");
            System.out.println("3. Full Inventory Report (by Location)");
            System.out.println("4. Batch Stock Report (Warehouse)");
            System.out.println("5. Bill Report (All Transactions)");
            System.out.println("6. Reshelving Report (End of Day)");
            System.out.println("0. Back to Main Menu");
            System.out.print("Selection: ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                System.out.println("<<--  Returning to main menu...");
                return;
            }

            Command command = reportCommands.get(choice);
            if (command != null) {
                command.execute(); //command pattern hides the actionables from the execution
            } else {
                System.out.println("! Invalid selection!");
            }
        }
    }

    private void openWarehouseMenu() {
        while (true) {
            System.out.println("\n========= WAREHOUSE MANAGEMENT =========");
            System.out.println("1. Receive Batch (New Stock Arrival)");
            System.out.println("2. Distribute Stock (Warehouse → Shelf/Web)");
            System.out.println("3. Add New Item");
            System.out.println("4. Delete Item");
            System.out.println("0. Back to Main Menu");
            System.out.print("Selection: ");

            String choice = scanner.nextLine().trim();

            if (choice.equals("0")) {
                System.out.println("<==  Returning to main menu...");
                return;
            }

            Command command = warehouseCommands.get(choice);
            if (command != null) {
                command.execute();
            } else {
                System.out.println("!!! Invalid selection.");
            }
        }
    }
}