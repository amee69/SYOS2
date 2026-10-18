package com.syos.presentation.cli;

import com.syos.application.dto.ItemCreateRequest;
import com.syos.application.service.ItemManagementFacade;
import com.syos.application.service.StockService;
import com.syos.domain.entity.Category;
import com.syos.domain.entity.Item;
//import com.syos.domain.entity.StockBatch;
import com.syos.application.dto.StockBatchRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class WarehouseController {
    private final StockService stockService;
    private final Scanner scanner;
    private final com.syos.domain.repository.ItemRepository itemRepository;
    private final ItemManagementFacade itemFacade;

    public WarehouseController(StockService stockService, Scanner scanner,
                               com.syos.domain.repository.ItemRepository itemRepository,
                               ItemManagementFacade itemFacade) {
        this.stockService = stockService;
        this.scanner = scanner;
        this.itemRepository = itemRepository;
        this.itemFacade = itemFacade;
    }

    //  add new item


    public void addNewItem() {
        System.out.println("\n========= ADD NEW ITEM =========");

        try {

            List<Category> categories = itemFacade.getCategories();
            if (categories.isEmpty()) {
                System.out.println("! No categories found in system!");
                return;
            }


            Category selectedCategory = null;
            while (true) {
                System.out.println("\nSelect Category:");
                System.out.println("-".repeat(40));
                for (int i = 0; i < categories.size(); i++) {
                    Category cat = categories.get(i);
                    System.out.printf("%d. %s (%s)\n", i + 1, cat.getName(), cat.getCode());
                    System.out.println("   " + cat.getDescription());
                }
                System.out.println("-".repeat(40));
                System.out.print("Choice (1-" + categories.size() + "): ");

                String choice = scanner.nextLine().trim();
                try {
                    int index = Integer.parseInt(choice) - 1;
                    if (index >= 0 && index < categories.size()) {
                        selectedCategory = categories.get(index);
                        System.out.println("Selected: " + selectedCategory.getName());
                        break; // Valid selection, exit loop
                    } else {
                        System.out.println("(!) Invalid selection. Please choose a number from the list.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("(!) Please enter a valid number.");
                }
            }


            String name = "";
            while (true) {
                System.out.print("\nItem Name: ");
                name = scanner.nextLine().trim();
                if (!name.isEmpty()) {
                    break; // sicne valid, exit loop
                }
                System.out.println("(!) Item name cannot be empty. Please try again.");
            }


            BigDecimal price = BigDecimal.ZERO;
            while (true) {
                System.out.print("Unit Price (LKR): ");
                try {
                    price = new BigDecimal(scanner.nextLine().trim());
                    if (price.compareTo(BigDecimal.ZERO) > 0) {
                        break; // Valid price, exit loop
                    } else {
                        System.out.println("(!) Price must be greater than 0.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("(!) Invalid price format. Please enter a number (e.g. 1500.50).");
                }
            }


            int threshold = 50; // Default
            while(true) {
                System.out.print("Reorder Threshold (default 50): ");
                String thresholdInput = scanner.nextLine().trim();
                if (thresholdInput.isEmpty()) {
                    break; // Use default
                }
                try {
                    threshold = Integer.parseInt(thresholdInput);
                    if(threshold >= 0) break;
                    System.out.println("(!) Threshold cannot be negative.");
                } catch (NumberFormatException e) {
                    System.out.println("(!) Please enter a whole number.");
                }
            }


            System.out.println("\n--- Confirm New Item ---");
            System.out.println("Category : " + selectedCategory.getName());
            System.out.println("Name     : " + name);
            System.out.println("Price    : LKR " + price);
            System.out.println("Threshold: " + threshold);
            System.out.print("\nCreate this item? (y/n): ");

            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("y")) {
                System.out.println(" Item creation cancelled.");
                return;
            }

            // execute
            ItemCreateRequest request = new ItemCreateRequest(name, price, selectedCategory.getCode(), threshold);
            Item newItem = itemFacade.createItem(request);


            System.out.println("\n" + "=".repeat(40));
            System.out.println("ITEM CREATED SUCCESSFULLY!");
            System.out.println("=".repeat(40));
            System.out.println("Code     : " + newItem.getItemCode());
            System.out.println("Name     : " + newItem.getName());
            System.out.println("Category : " + selectedCategory.getName());
            System.out.println("Price    : LKR " + newItem.getUnitPrice());
            System.out.println("=".repeat(40));
            System.out.println("\n:> Item is now available for stock management.");

        } catch (IllegalArgumentException e) {
            System.out.println(" Validation Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println(" System Error: " + e.getMessage());
        }
    }

    // ... inside WarehouseController class ...

    public void deleteItem() {
        System.out.println("\n========= DELETE ITEM =========");

        try {
            System.out.print("Enter Item Code to delete: ");
            String code = scanner.nextLine().trim();

            //  find item to show details
            var itemOpt = itemRepository.findByCode(code);
            if (itemOpt.isEmpty()) {
                System.out.println("! Item not found.");
                return;
            }

            //  show what is being deleted
            var item = itemOpt.get();
            System.out.println("-".repeat(40));
            System.out.println("Code : " + item.getItemCode());
            System.out.println("Name : " + item.getName());
            System.out.println("Price: LKR " + item.getUnitPrice());
            System.out.println("-".repeat(40));
            System.out.println("(!) WARNING: This cannot be undone.");
            System.out.println("(!) Note: You cannot delete items that have stock or sales history.");

            //  confirm
            System.out.print("\nAre you sure you want to delete this item? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (confirm.equals("y")) {
                //  call Facade
                itemFacade.deleteItem(code);
                System.out.println("\n:) Item deleted successfully.");
            } else {
                System.out.println(" Operation cancelled.");
            }

        } catch (RuntimeException e) {
            //  catches the "Foreign Key" error from the Repository
            System.out.println("\nX Delete Failed: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nX System Error: " + e.getMessage());
        }
    }



     // handles new stock arrivals into the store.

    public void manageArrivals() {
        System.out.println("\n--- [RECORD NEW SHIPMENT] ---");

        try {
            System.out.print("Item Code: ");
            String code = scanner.nextLine().trim();

            var itemOpt = itemRepository.findByCode(code);
            if (itemOpt.isEmpty()) {
                System.out.println(" Item code '" + code + "' not found in system.");
                System.out.println(" Tip: Use option 3 to add new items first.");
                return;
            }

            var item = itemOpt.get();
            System.out.println(" Item: " + item.getName());

            System.out.print("Quantity: ");
            int qty;
            try {
                qty = Integer.parseInt(scanner.nextLine().trim());
                if (qty <= 0) {
                    System.out.println(" Quantity must be greater than 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println(" Invalid quantity. Please enter a number.");
                return;
            }

            System.out.print("Expiry Date (YYYY-MM-DD): ");
            LocalDate expiry;
            try {
                expiry = LocalDate.parse(scanner.nextLine().trim());

                if (expiry.isBefore(LocalDate.now())) {
                    System.out.println(" Expiry date cannot be in the past.");
                    return;
                }

                if (expiry.isBefore(LocalDate.now().plusDays(30))) {
                    System.out.println("  Warning: Item expires in less than 30 days!");
                    System.out.print("Continue anyway? (y/n): ");
                    String confirm = scanner.nextLine().trim().toLowerCase();
                    if (!confirm.equals("y")) {
                        System.out.println(" Batch registration cancelled.");
                        return;
                    }
                }
            } catch (Exception e) {
                System.out.println(" Invalid date format. Use YYYY-MM-DD");
                return;
            }

//            StockBatch batch = new StockBatch(0, code, LocalDate.now(), expiry, qty);
//            stockService.addBatch(batch);
            StockBatchRequest request = new StockBatchRequest(code, qty, expiry);
            stockService.addBatch(request); //DTO used here

            System.out.println("Batch registered successfully!");
            System.out.println("   Item: " + item.getName() + " (" + code + ")");
            System.out.println("   Quantity: " + qty + " units");
            System.out.println("   Expires: " + expiry);

        } catch (Exception e) {
            System.out.println("System Error: " + e.getMessage());
        }
    }


     // moves stock from warehouse to operational areas

    public void manageDistribution() {
        System.out.println("\n--- [STOCK ALLOCATION] ---");

        try {
            System.out.print("Item Code: ");
            String code = scanner.nextLine().trim();

            var itemOpt = itemRepository.findByCode(code);
            if (itemOpt.isEmpty()) {
                System.out.println("Item code '" + code + "' not found in system.");
                return;
            }

            var item = itemOpt.get();
            System.out.println("Item: " + item.getName());

            int warehouseStock = stockService.getStockByLocation(code, "STORE");
            System.out.println("Warehouse Stock: " + warehouseStock + " units");

            if (warehouseStock <= 0) {
                System.out.println("No stock in warehouse to distribute!");
                return;
            }

            System.out.print("Quantity to distribute: ");
            int qty;
            try {
                qty = Integer.parseInt(scanner.nextLine().trim());
                if (qty <= 0) {
                    System.out.println("!! Quantity must be greater than 0.");
                    return;
                }
                if (qty > warehouseStock) {
                    System.out.println("!! Insufficient warehouse stock!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("!! Invalid quantity.");
                return;
            }

            System.out.print("Target Location (1. SHELF, 2. WEB): ");
            String choice = scanner.nextLine().trim();
            String target = choice.equals("1") ? "SHELF" : choice.equals("2") ? "WEB" : null;

            if (target == null) {
                System.out.println("!! Invalid selection.");
                return;
            }

            //int currentTargetStock = stockService.getStockByLocation(code, target);
            int beforeStock = stockService.getStockByLocation(code, target);
            stockService.moveStockToLocation(code, qty, target);
            int afterStock = stockService.getStockByLocation(code, target);

            int actualMoved = afterStock - beforeStock;
            if (actualMoved > 0) {
                System.out.println(":) Distribution completed!");
                System.out.println("   Moved: " + actualMoved + " units → " + target);
            }

        } catch (Exception e) {
            System.out.println("!! Allocation Failed: " + e.getMessage());
        }
    }
}