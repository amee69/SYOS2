package com.syos.domain.exception;

import java.math.BigDecimal;


public class PaymentException extends RuntimeException {
    private final BigDecimal amountRequired;
    private final BigDecimal amountProvided;

    public PaymentException(String message) {
        super(message);
        this.amountRequired = null;
        this.amountProvided = null;
    }

    public PaymentException(BigDecimal amountRequired, BigDecimal amountProvided) {
        super(String.format(
                "Insufficient payment: required LKR %.2f, provided LKR %.2f",
                amountRequired, amountProvided
        ));
        this.amountRequired = amountRequired;
        this.amountProvided = amountProvided;
    }



    public BigDecimal getShortfall() {
        if (amountRequired != null && amountProvided != null) {
            return amountRequired.subtract(amountProvided);
        }
        return null;
    }
}