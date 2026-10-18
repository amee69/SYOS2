package com.syos.domain.repository;

import com.syos.domain.entity.Customer;
import java.math.BigDecimal;
import java.util.Optional;

public interface CustomerRepository {
    //fix null values cause so used optional
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findById(int customerId);
    void updateLoyaltyPoints(int customerId, int newPoints);
    void updateTotalPurchases(int customerId, BigDecimal newTotal);

    void save(Customer customer);
    boolean existsByEmail(String email);

    Optional<Customer> authenticate(String email, String password);
}