package com.syos.presentation.cli;

import com.syos.domain.entity.Customer;
import com.syos.domain.entity.User;
import com.syos.domain.repository.CustomerRepository;
import com.syos.domain.repository.UserRepository;

import java.util.Optional;
import java.util.Scanner;

public class AuthenticationController {
    private final UserRepository userRepo;
    private final CustomerRepository customerRepo;
    private final Scanner scanner;

    public AuthenticationController(UserRepository userRepo, CustomerRepository customerRepo, Scanner scanner) {
        this.userRepo = userRepo;
        this.customerRepo = customerRepo;
        this.scanner = scanner;
    }

    public User loginStaff(String requiredRole) {
        System.out.println("\n--- STAFF LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Optional<User> userOpt = userRepo.authenticate(username, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRole().equalsIgnoreCase("MANAGER") || user.getRole().equalsIgnoreCase(requiredRole)) {
                System.out.println(" Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
                return user;
            }
        }

        return null;
    }

    public Customer loginCustomer() {
        System.out.println("\n--- WEB PORTAL LOGIN ---");
        System.out.print("Email: ");
        String email = scanner.nextLine().trim().toLowerCase();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        Optional<Customer> customerOpt = customerRepo.authenticate(email, password);

        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            System.out.println("\nWelcome back, " + customer.getFullName() + "!");
            System.out.println("   Tier: " + customer.getCustomerTier());
            System.out.println("   Loyalty Points: " + customer.getLoyaltyPoints());
            return customer;
        }

        return null;
    }
}