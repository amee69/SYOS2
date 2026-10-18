package com.syos.domain.discount;

import com.syos.domain.service.DiscountCalculator;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class LoyaltyTierDiscount extends DiscountDecorator {
    private final String tierName;
    private final BigDecimal discountPercent;

    public LoyaltyTierDiscount(DiscountCalculator calculator, String tier, BigDecimal percent) {
        super(calculator); ///to pass previous chain to parent
        this.tierName = tier;
        this.discountPercent = percent;
    }

    //need for interface contract
    @Override
    public BigDecimal calculateDiscount(BigDecimal totalAmount) {
        if (discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return wrappedCalculator.calculateDiscount(totalAmount);
        }

        BigDecimal thisDiscount = totalAmount
                .multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal previousDiscount = wrappedCalculator.calculateDiscount(totalAmount);

        BigDecimal total = previousDiscount.add(thisDiscount);


        return total;
    }

    @Override
    public String getDiscountDescription() {
        String wrapped = wrappedCalculator.getDiscountDescription();
        if (discountPercent.compareTo(BigDecimal.ZERO) == 0) {
            return wrapped;
        }

        String tierDiscount = tierName + " Member " + discountPercent + "%";
        if (wrapped.equals("No discount applied")) {
            return tierDiscount;
        }
        return wrapped + " + " + tierDiscount;
    }





}