package com.syos.domain.service;

import java.math.BigDecimal;


 // Domain Service Interface
 // without modifying the core BillingService logic can use extendability future
public interface PaymentStrategy {
    boolean process(BigDecimal amount);
    String getPaymentType();

    BigDecimal getCashTendered();
    BigDecimal getChangeDue();
}