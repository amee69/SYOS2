package com.syos.infrastructure.payment;

import com.syos.domain.service.PaymentStrategy;
import java.math.BigDecimal;


 // strategy concrete strategy for online payments

 // web online payments


public class OnlinePayment implements PaymentStrategy {

    private final BigDecimal amount;

    public OnlinePayment(BigDecimal amount) {
        this.amount = amount;
    }

    //simulate
    @Override
    public boolean process(BigDecimal totalAmount) {

        return amount.compareTo(totalAmount) >= 0;
    }

    //return type as online
    @Override
    public String getPaymentType() {
        return "ONLINE";
    }

    //since billling service needs
    @Override
    public BigDecimal getCashTendered() {
        return amount;
    }

    //no change, but need to return BigDecimal
    @Override
    public BigDecimal getChangeDue() {
        return BigDecimal.ZERO;
    }
}