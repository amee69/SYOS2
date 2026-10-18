package com.syos.application.service;

import com.syos.application.dto.CustomerRegistrationRequest;
import com.syos.domain.entity.Customer;
import com.syos.domain.repository.CustomerRepository;

import java.math.BigDecimal;

public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        System.out.println("checkingggggg");

    }

    public Customer register(CustomerRegistrationRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }


        Customer newCustomer = new Customer(
                0,
                request.getEmail(),
                request.getFullName(),
                request.getPhone(),
                request.getPassword(),
                0,
                BigDecimal.ZERO,
                "BRONZE"
        );

        customerRepository.save(newCustomer);
        return newCustomer;
    }
}