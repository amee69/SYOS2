package com.syos.domain.service;

import java.math.BigDecimal;


//  decorator pattern, component Interface
//  allows flexible discount calculation without modifying Bill class

public interface DiscountCalculator {
    BigDecimal calculateDiscount(BigDecimal totalAmount);
    String getDiscountDescription();
}