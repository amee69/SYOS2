package com.syos.presentation.cli;

import com.syos.application.dto.BillItemRequest;
import com.syos.application.dto.CheckoutRequest;
import com.syos.application.factory.ValidationServiceFactory;
import com.syos.application.service.BillingService;
import com.syos.application.service.ValidationService;
import com.syos.domain.discount.*;
import com.syos.domain.entity.*;
import com.syos.domain.exception.ItemNotFoundException;
import com.syos.domain.exception.PaymentException;
import com.syos.domain.repository.CustomerRepository;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.LoyaltyTierRepository;

import com.syos.domain.service.DiscountCalculator;
import com.syos.domain.service.PaymentStrategy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BillingController {
    private final BillingService billingService;
    private final ItemRepository itemRepo;
    private final Scanner scanner;
    private final CustomerRepository customerRepo;
    private final LoyaltyTierRepository loyaltyTierRepository;
    private final ValidationServiceFactory validationFactory;
    private final PaymentFactory paymentFactory;  // ADD

    public BillingController(BillingService billingService, ItemRepository itemRepo,
                             Scanner scanner, CustomerRepository customerRepo,
                             ValidationServiceFactory validationFactory,
                             LoyaltyTierRepository loyaltyTierRepository,
                             PaymentFactory paymentFactory) {  // ADD
        this.billingService = billingService;
        this.itemRepo = itemRepo;
        this.scanner = scanner;
        this.customerRepo = customerRepo;
        this.validationFactory = validationFactory;
        this.loyaltyTierRepository = loyaltyTierRepository;
        this.paymentFactory = paymentFactory;  // ADD
    }


//     UNIFIED entry point for all transactions.
//       transactionType "POS" or "WEB"
//       Cashier username (POS) or WEB-SYSTEM (WEB)
//      Customer object (can be null for POS walk-in) /can be filled later

    public void execute(String transactionType, String operatorName, Customer customer) {

        boolean isWebOrder = transactionType.equalsIgnoreCase("WEB");

        // display header based on type
        if (isWebOrder) {
            System.out.println("\n--- [WEB PORTAL] ---");
            System.out.println("Customer: " + customer.getFullName());
        } else {
            System.out.println("\n--- [POS TERMINAL] ---");
            System.out.println("Cashier: " + operatorName);
        }

        boolean sessionActive = true;

        while (sessionActive) {
            List<BillItem> cart = new ArrayList<>();
            System.out.println("\n--- [NEW TRANSACTION] ---");

            // Item scanning loop same  both
            while (true) {
                System.out.print("Enter Item Code (or 'done'): ");
                String code = scanner.nextLine().trim();
                if (code.equalsIgnoreCase("done")) break;

                System.out.print("Qty: ");
                try {
                    //  handle empty string input for quantity
                    String qtyInput = scanner.nextLine().trim();
                    int qty = qtyInput.isEmpty() ? 0 : Integer.parseInt(qtyInput);

                    // use appropriate stock type for validation
                    String stockType = isWebOrder ? "WEB" : "SHELF";
                    // create a new validator here to so the correct stock type is used
                    ValidationService validator = validationFactory.create(stockType);

                    //dto use
                    BillItemRequest request = new BillItemRequest(code, qty);
                    //dto pass validator
                    String error = validator.validateItem(request);
                    //death of dto after validateitem

                    if (error != null) {
                        System.out.println("!! >:[ " + error);
                        continue;
                    }

                    //here for more secuirty incase chain fails
                    Item item = itemRepo.findByCode(code).orElseThrow(() -> new ItemNotFoundException(code));

                    cart.add(new BillItem(
                                    item.getItemCode(),
                                    item.getName(),
                                    qty,
                                    item.getUnitPrice()
                            ));

                    BigDecimal runningTotal = cart.stream()
                            .map(BillItem::getTotalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Format: + Added to cart. [Total Due: LKR 1500.00]
                    System.out.printf("+ Added to cart. [Total Due: LKR %.2f]%n", runningTotal);
                    // ---------------------------------------------------

                } catch (NumberFormatException e) {
                    System.out.println("-? Invalid quantity.");
                } catch (Exception e) {
                    System.out.println("! Error: " + e.getMessage());
                }
            }

            // process payment if cart has items
            if (!cart.isEmpty()) {
                showCartSummary(cart);
                processPayment(cart, transactionType, operatorName, customer);
            } else {
                System.out.println("!(?) Cart is empty. No transaction processed.");
            }

            // continue or exit
            System.out.println("------------------------------------------------");
            System.out.print("Press [Enter] for new transaction, or type 'exit': ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                sessionActive = false;
                if (isWebOrder) {
                    System.out.println("Bye Thank you!");
                } else {
                    System.out.println("Logging out...");
                }
            }
        }
    }


     // UNIFIED payment processing for both POS and WEB

    private void processPayment(List<BillItem> cart, String transactionType,
                                String operatorName, Customer customer) {

        boolean isWebOrder = transactionType.equalsIgnoreCase("WEB");

        BigDecimal baseTotal = cart.stream()
                .map(BillItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = cart.stream().mapToInt(BillItem::getQuantity).sum();

        System.out.println("\n--- PAYMENT DETAILS ---");
        System.out.println("Items: " + totalItems);
        System.out.println("Subtotal: LKR " + baseTotal);

        int pointsToRedeem = 0;

        // Look up customer if not provided (POS walk-in)
        if (customer == null && !isWebOrder) {
            while (true) {
                System.out.print("\nCustomer email (or press Enter to skip): ");
                String email = scanner.nextLine().trim();

                if (email.isEmpty()) {
                    break;
                }

                var customerOpt = customerRepo.findByEmail(email);
                if (customerOpt.isPresent()) {
                    customer = customerOpt.get();
                    break;
                } else {
                    System.out.println(" Customer not found. Please try again or press Enter to skip.");
                }
            }
        }

        // Ask about points redemption (UI concern — just gathering input)
        if (customer != null) {
            System.out.println(":) Customer: " + customer.getFullName());

            if (customer.getLoyaltyPoints() > 0) {
                System.out.println(":00 Available Points: " + customer.getLoyaltyPoints());

                while (true) {
                    System.out.print("Redeem points? (Enter amount or press Enter to skip, Max: " + customer.getLoyaltyPoints() + "): ");
                    String input = scanner.nextLine().trim();

                    if (input.isEmpty()) {
                        pointsToRedeem = 0;
                        break;
                    }

                    try {
                        int requestedPoints = Integer.parseInt(input);

                        if (requestedPoints < 0) {
                            System.out.println("(!-Number!)  Points cannot be negative. Try again.");
                            continue;
                        }

                        if (requestedPoints > customer.getLoyaltyPoints()) {
                            System.out.println(":(  Insufficient points (Max: " + customer.getLoyaltyPoints() + "). Try again.");
                            continue;
                        }

                        pointsToRedeem = requestedPoints;
                        break;

                    } catch (NumberFormatException e) {
                        System.out.println(":0  Invalid input. Please enter a number or press Enter to skip.");
                    }
                }
            }
        }

        // Net amount will be calculated by BillingService (business logic stays in service)
        BigDecimal netAmount = baseTotal; // approximate for payment factory


        //====>  factory pattern implementation

        //  delegate the user interaction to the Factory

        PaymentStrategy strategy = paymentFactory.create(transactionType, netAmount, scanner);

        //  handle cancellation (Factory returns null if user types 'cancel')
        if (strategy == null) {
            System.out.println("! Transaction Cancelled.");
            return;
        }

        // =========================================================

        // === build and process checkout request (uses billItemRequest DTO which is also builder pattern
        try {
            CheckoutRequest request = CheckoutRequest.builder()
                    .setItems(cart.stream()//use cart to map ui object to dto object
                            .map(item -> new BillItemRequest(item.getItemCode(), item.getQuantity()))
                            .toList())
                    .setTransactionType(transactionType)
                    .setCashierUsername(operatorName)
                    .setCustomerId(customer != null ? customer.getCustomerId() : null)
                    .setLoyaltyPointsToRedeem(pointsToRedeem)
                    //.setPaymentStrategy(strategy) //  pass Strategy object (can web or shelf)
                    .setCashTendered(strategy.getCashTendered())
                    .setPaymentType(strategy.getPaymentType())
                    .setDiscountDetails(BigDecimal.ZERO, "")
                    .build();

            //srp delegation
            Bill bill = billingService.processCheckout(request);

            // Pass the strategy object to printReceipt
            printReceipt(bill, strategy, customer, isWebOrder);

        } catch (PaymentException e) {
            System.out.println("!!! Payment Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("!!! Transaction Error: " + e.getMessage());
        }
    }





    /**
     * web or shelf receipt printing
     */
    private void printReceipt(Bill bill, PaymentStrategy payment, Customer customer, boolean isWebOrder) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println(isWebOrder ? "       ORDER CONFIRMATION" : "           RECEIPT");
        System.out.println("=".repeat(40));
        System.out.println("Bill #" + bill.getSerialNumber());
        System.out.println("Date: " + bill.getBillDate());
        System.out.println("Type: " + bill.getTransactionType());

        if (!isWebOrder) {
            System.out.println("Cashier: " + bill.getCashierUsername());
        }
        if (customer != null) {
            System.out.println("Customer: " + customer.getFullName());
        }

        System.out.println("-".repeat(40));

        // Items
        for (BillItem item : bill.getBillItems()) {
            System.out.printf("%-20s x%d  LKR %.2f%n",
                    item.getItemName(), item.getQuantity(), item.getTotalPrice());
        }

        System.out.println("-".repeat(40));
        System.out.println("Subtotal : LKR " + bill.getTotalFullPrice());
        System.out.println("Discount : LKR " + bill.getTotalDiscount());
        System.out.println("Net Total: LKR " + bill.getNetTotal());

        //  polimorph part ask the strategy for details
        if (!isWebOrder) {
            System.out.println("Cash     : LKR " + payment.getCashTendered());

            // Only print change if it's > 0 (handled by strategy math)
            if (payment.getChangeDue().compareTo(BigDecimal.ZERO) > 0) {
                System.out.println("Change   : LKR " + payment.getChangeDue());
            }
        } else {
            System.out.println("Payment  : ONLINE ✓");
        }

        // loyalty info
        if (customer != null) {
            System.out.println("\n--- LOYALTY BALANCE ---");
            System.out.println("Points Used  : -" + bill.getLoyaltyPointsUsed());
            System.out.println("Points Earned: +" + bill.getLoyaltyPointsEarned());
        }

        System.out.println("=".repeat(40));
        System.out.println(isWebOrder ? "Order Complete! Thank you!" : " Transaction Complete.");
    }




    private void showCartSummary(List<BillItem> cart) {
        System.out.println("\n============ CART SUMMARY ============");
        System.out.println(String.format("%-20s %5s %12s %12s", "ITEM", "QTY", "PRICE", "TOTAL"));
        System.out.println("-".repeat(52));

        BigDecimal subtotal = BigDecimal.ZERO;
        for (BillItem item : cart) {
            System.out.printf("%-20s %5d %12.2f %12.2f\n",
                    item.getItemName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice());
            subtotal = subtotal.add(item.getTotalPrice());
        }

        System.out.println("-".repeat(52));
        System.out.println(String.format("%38s LKR %.2f", "SUBTOTAL:", subtotal));
        System.out.println("======================================\n");
    }



    // === CONVENIENCE OVERLOADS (Shortcuts) ===


      //POS transactions (staff login, no pre-selected customer)

    public void execute(String location, String cashierName) {
        execute(location, cashierName, null);
    }


      //WEB transactions (customer login)

    public void executeWebOrder(Customer customer) {
        execute("WEB", "WEB-SYSTEM", customer);
    }
}