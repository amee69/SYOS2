package com.syos.presentation.cli.commands;

// new MenuCommand("Receive Stock", warehouseController::manageArrivals);
// new MenuCommand("Daily Sales", reportController::runDailySalesReport);

public class MenuCommand implements Command {

    //  'reportType' -> 'description' ( both Warehouse & Reports)
    private final String description;

    // java built in 'work'
    private final Runnable action;


    public MenuCommand(String description, Runnable action) {
        this.description = description;
        this.action = action;
    }

    @Override
    public void execute() {
        // print to console
        System.out.println(">0<  Processing: " + description);
        action.run();
    }
}