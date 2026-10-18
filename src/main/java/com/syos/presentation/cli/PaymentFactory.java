package com.syos.presentation.cli;

import com.syos.domain.exception.PaymentException;
import com.syos.domain.service.PaymentStrategy;
import com.syos.infrastructure.payment.CashPayment;
import com.syos.infrastructure.payment.OnlinePayment;

import java.math.BigDecimal;
import java.util.Scanner;

public class PaymentFactory {


    //  decides which payment strategy to create and interacts with the user to set it up
     // handles zero-amount transactions without requiring payment.

    public PaymentStrategy create(String transactionType, BigDecimal amount, Scanner scanner) {


        // handle zero-amount transactions (fully covered by discounts/points)

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("\n:) No payment required - fully covered by discounts/points!");
            // return a cashPayment with zero tendered (no cash needed)
            CashPayment zeroPayment = new CashPayment(BigDecimal.ZERO);
            zeroPayment.process(BigDecimal.ZERO); // Process with zero amount
            return zeroPayment;
        }

        if (transactionType.equalsIgnoreCase("WEB")) {
            //  WEB LOGIC
            System.out.print("\nConfirm payment of LKR " + amount + "? (y/n): ");
            String confirm = scanner.nextLine().trim();

            if (confirm.equalsIgnoreCase("y")) {
                System.out.println("\n000 Processing online payment...");
                System.out.println(":) Payment confirmed!");
                return new OnlinePayment(amount); //return online startegy
            } else {
                return null; // user cancelled
            }

        } else {
            // POS LOGIC
            while (true) {
                System.out.print("\n = Cash Tendered (or 'cancel'): ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("cancel")) {
                    return null; // User cancelled
                }

                try {
                    BigDecimal cashTendered = new BigDecimal(input);
                    CashPayment payment = new CashPayment(cashTendered);

                    //  factory validate shortfall
                    if (!payment.process(amount)) {
                        BigDecimal shortfall = amount.subtract(cashTendered);
                        System.out.println("!! Insufficient cash! Shortfall: " + shortfall);

                        continue; // ask again
                    }

                    return payment; // return valid strategy

                } catch (NumberFormatException e) {
                    System.out.println("! Invalid amount. Please enter numbers (e.g., 5000).");
                }
            }
        }
    }
}