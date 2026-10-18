package com.syos.application.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CheckoutRequest {
    private final List<BillItemRequest> items;
    private final String transactionType;
    private final Integer customerId;
    private final String cashierUsername;
    private final int loyaltyPointsToRedeem;
    private final BigDecimal totalDiscount;
    private final String discountDescription;
    private final BigDecimal cashTendered;
    private final String paymentType;

    private CheckoutRequest(Builder builder) {
        this.items = List.copyOf(builder.items);
        this.transactionType = builder.transactionType;
        this.customerId = builder.customerId;
        this.cashierUsername = builder.cashierUsername;
        this.loyaltyPointsToRedeem = builder.loyaltyPointsToRedeem;
        this.totalDiscount = builder.totalDiscount;
        this.discountDescription = builder.discountDescription;
        this.cashTendered = builder.cashTendered;
        this.paymentType = builder.paymentType;
    }

    public List<BillItemRequest> getItems() { return items; }
    public String getTransactionType() { return transactionType; }
    public Integer getCustomerId() { return customerId; }
    public String getCashierUsername() { return cashierUsername; }
    public int getLoyaltyPointsToRedeem() { return loyaltyPointsToRedeem; }
    public BigDecimal getTotalDiscount() { return totalDiscount; }
    public String getDiscountDescription() { return discountDescription; }
    public BigDecimal getCashTendered() { return cashTendered; }
    public String getPaymentType() { return paymentType; }
    public boolean hasCustomer() { return customerId != null; }

    public static class Builder {
        private List<BillItemRequest> items = new ArrayList<>();
        private String transactionType = "POS";
        private Integer customerId = null;
        private String cashierUsername = null;
        private int loyaltyPointsToRedeem = 0;
        private BigDecimal totalDiscount = BigDecimal.ZERO;
        private String discountDescription = "";
        private BigDecimal cashTendered = BigDecimal.ZERO;
        private String paymentType = "CASH";

        public Builder setItems(List<BillItemRequest> items) {
            this.items = new ArrayList<>(items);
            return this;
        }
        public Builder setTransactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }
        public Builder setCustomerId(Integer customerId) {
            this.customerId = customerId;
            return this;
        }
        public Builder setCashierUsername(String cashierUsername) {
            this.cashierUsername = cashierUsername;
            return this;
        }
        public Builder setLoyaltyPointsToRedeem(int points) {
            this.loyaltyPointsToRedeem = points;
            return this;
        }
        public Builder setDiscountDetails(BigDecimal totalDiscount, String description) {
            this.totalDiscount = totalDiscount;
            this.discountDescription = description;
            return this;
        }
        public Builder setCashTendered(BigDecimal cashTendered) {
            this.cashTendered = cashTendered;
            return this;
        }
        public Builder setPaymentType(String paymentType) {
            this.paymentType = paymentType;
            return this;
        }
        public CheckoutRequest build() {
            if (items.isEmpty()) {
                throw new IllegalStateException("Checkout must have at least one item");
            }
            return new CheckoutRequest(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}