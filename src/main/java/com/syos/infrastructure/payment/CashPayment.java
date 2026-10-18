package com.syos.infrastructure.payment;

import com.syos.domain.service.PaymentStrategy;
import java.math.BigDecimal;

public class CashPayment implements PaymentStrategy {
    private final BigDecimal tenderedAmount;
    private BigDecimal changeDue;

    public CashPayment(BigDecimal tenderedAmount) {
        this.tenderedAmount = tenderedAmount;
        this.changeDue = BigDecimal.ZERO;
    }

    @Override
    //usually actual payemtn logic
    public boolean process(BigDecimal totalAmount) {
        if (tenderedAmount.compareTo(totalAmount) >= 0) {
            this.changeDue = tenderedAmount.subtract(totalAmount);
            return true;
        }
        return false;
    }

    @Override
    public String getPaymentType() {
        return "CASH";
    }

    @Override
    public BigDecimal getChangeDue() {
        return changeDue;
    }


    @Override
    public BigDecimal getCashTendered() {
        return tenderedAmount;
    }
}