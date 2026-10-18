package com.syos.domain.discount;

import com.syos.domain.service.DiscountCalculator;
import java.math.BigDecimal;

public class LoyaltyPointsRedemption extends DiscountDecorator {
    private final int pointsToRedeem;
    private static final BigDecimal POINT_VALUE = BigDecimal.ONE; // 1 point = LKR 1

    //const
    public LoyaltyPointsRedemption(DiscountCalculator calculator, int points) {
        super(calculator); //for constructor of discount decorator
        this.pointsToRedeem = points;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal totalAmount) {


        BigDecimal pointsAsDecimal = BigDecimal.valueOf(pointsToRedeem);

        BigDecimal myDiscount = pointsAsDecimal.multiply(POINT_VALUE);

        //  get the discount previous discoiunts through chain
        //access wrappedCalculator from DiscountDecorator
        BigDecimal previousDiscount = wrappedCalculator.calculateDiscount(totalAmount);

        //  add my discount to the previous discount
        BigDecimal totalDiscount = previousDiscount.add(myDiscount);

        return totalDiscount;
    }

    @Override
    public String getDiscountDescription() {
        String wrapped = wrappedCalculator.getDiscountDescription();
        String pointsDesc = "Loyalty Points: " + pointsToRedeem + " pts (LKR " + pointsToRedeem + ")";

        if (wrapped.equals("No discount applied")) {
            return pointsDesc;
        }
        return wrapped + " + " + pointsDesc;
    }
}