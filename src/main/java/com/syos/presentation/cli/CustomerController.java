package com.syos.presentation.cli;

import com.syos.application.dto.CustomerRegistrationRequest;
import com.syos.application.service.CustomerService;
import com.syos.domain.entity.Customer;

import java.util.Scanner;

public class CustomerController {

    private final CustomerService customerService;
    private final Scanner scanner;

    public CustomerController(CustomerService customerService, Scanner scanner) {
        this.customerService = customerService;
        this.scanner = scanner;
    }

    public void registerCustomer() {
        System.out.println("\n========= CUSTOMER REGISTRATION =========");

        try {
            //  name
            System.out.print("Full Name: ");
            String fullName = scanner.nextLine().trim();
            if (fullName.isEmpty()) {
                System.out.println("!! Name cannot be empty.");
                return;
            }

            //  email
            System.out.print("Email: ");
            String email = scanner.nextLine().trim().toLowerCase();
            if (email.isEmpty() || !email.contains("@")) {
                System.out.println("!! Invalid email format.");
                return;
            }

            //  Phone
            String phone;
            while (true) {
                System.out.print("Phone (e.g., 0771234567): ");
                phone = scanner.nextLine().trim();
                if (!phone.isEmpty() && phone.matches("\\d+")) {
                    break;
                }
                System.out.println("!! Phone must be a number and cannot be empty.");
            }

            //  password
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            if (password.length() < 4) {
                System.out.println("!! Password must be at least 4 characters.");
                return;
            }

            //  confirm pass
            System.out.print("Confirm Password: ");
            String confirmPassword = scanner.nextLine().trim();
            if (!password.equals(confirmPassword)) {
                System.out.println("!! Passwords do not match.");
                return;
            }

            //  show summary and confirm
            System.out.println("\n--- Registration Summary ---");
            System.out.println("Name  : " + fullName);
            System.out.println("Email : " + email);
            System.out.println("Phone : " + phone);
            System.out.println("Tier  : BRONZE (starting tier)");
            System.out.println("Points: 0 (starting balance)");
            System.out.print("\nConfirm registration? (y/n): ");

            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("y")) {
                System.out.println("!! Registration cancelled.");
                return;
            }

            //  create DTO and call service
            CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                    fullName, email, phone, password
            );

            Customer newCustomer = customerService.register(request);

            //  message
            System.out.println("\n" + "=".repeat(40));
            System.out.println(":) REGISTRATION SUCCESSFUL!");
            System.out.println("=".repeat(40));
            System.out.println("Welcome to SYOS, " + newCustomer.getFullName() + "!");
            System.out.println("You can now shop online using your email.");
            System.out.println("\n<0> Loyalty Program Benefits:");
            System.out.println("   • Earn 1 point per LKR 100 spent");
            System.out.println("=".repeat(40));

        } catch (IllegalArgumentException e) {
            System.out.println("!! " + e.getMessage());
        } catch (Exception e) {
            System.out.println("! Registration Error: " + e.getMessage());
        }
    }
}