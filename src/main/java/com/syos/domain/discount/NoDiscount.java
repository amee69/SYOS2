package com.syos.domain.discount;

import com.syos.domain.service.DiscountCalculator;
import java.math.BigDecimal;

public class NoDiscount implements DiscountCalculator {

    @Override
    public BigDecimal calculateDiscount(BigDecimal totalAmount) {
        return BigDecimal.ZERO;
    }

    @Override
    public String getDiscountDescription() {
        return "No discount applied";
    }
}