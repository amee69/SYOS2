package com.syos.domain.discount;

import com.syos.domain.service.DiscountCalculator;
import java.math.BigDecimal;

public abstract class DiscountDecorator implements DiscountCalculator {
    protected DiscountCalculator wrappedCalculator; //to hold different calculations

    public DiscountDecorator(DiscountCalculator calculator) {
        this.wrappedCalculator = calculator;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal totalAmount) {
        //whatever discount is stored, use that ones calculateDiscount method defined.
        return wrappedCalculator.calculateDiscount(totalAmount);
    }

    @Override
    public String getDiscountDescription() {
        return wrappedCalculator.getDiscountDescription();
    }
}