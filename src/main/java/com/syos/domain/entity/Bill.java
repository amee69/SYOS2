package com.syos.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Bill {

    private final int serialNumber;
    private final LocalDateTime billDate;
    private final List<BillItem> billItems;
    private final BigDecimal totalAmount;
    private final BigDecimal discount;
    private final String paymentType;
    private final String transactionType;

    private final Integer customerId;
    private final String cashierUsername;
    private final String discountDescription;
    private final int loyaltyPointsUsed;
    private final int loyaltyPointsEarned;

    private final BigDecimal cashTendered;
    private final BigDecimal changeAmount;

    //constr prvt bcs to enforce builder pattern / external code cannot instantiate
    private Bill(Builder builder) {
        this.serialNumber = builder.serialNumber;
        this.billDate = builder.billDate;
        this.billItems = builder.billItems;
        this.totalAmount = builder.totalAmount;
        this.discount = builder.discount;
        this.paymentType = builder.paymentType;
        this.transactionType = builder.transactionType;
        this.customerId = builder.customerId;
        this.cashierUsername = builder.cashierUsername;
        this.discountDescription = builder.discountDescription;
        this.loyaltyPointsUsed = builder.loyaltyPointsUsed;
        this.loyaltyPointsEarned = builder.loyaltyPointsEarned;
        this.cashTendered = builder.cashTendered;
        this.changeAmount = builder.changeAmount;
    }

    //getters
    public int getSerialNumber() { return serialNumber; }
    public LocalDateTime getBillDate() { return billDate; }
    public List<BillItem> getBillItems() { return billItems; }
    public BigDecimal getTotalFullPrice() { return totalAmount; }
    public BigDecimal getTotalDiscount() { return discount; }
    public String getPaymentType() { return paymentType; }
//    public BigDecimal getNetTotal() { return totalAmount.subtract(discount); }
public BigDecimal getNetTotal() { return totalAmount.subtract(discount).max(BigDecimal.ZERO); }
    public String getTransactionType() { return transactionType; }
    public Integer getCustomerId() { return customerId; }
    public String getCashierUsername() { return cashierUsername; }
    public String getDiscountDescription() { return discountDescription; }
    public int getLoyaltyPointsUsed() { return loyaltyPointsUsed; }
    public int getLoyaltyPointsEarned() { return loyaltyPointsEarned; }
    public BigDecimal getCashTendered() { return cashTendered; }
    public BigDecimal getChangeAmount() { return changeAmount; }

    // ================= BUILDER =================

    public static class Builder {
        private int serialNumber;
        private LocalDateTime billDate = LocalDateTime.now();
        private List<BillItem> billItems;
        private BigDecimal totalAmount;
        private BigDecimal discount = BigDecimal.ZERO;
        private String paymentType = "CASH";
        private String transactionType = "POS";
        private Integer customerId;
        private String cashierUsername;
        private String discountDescription = "";
        private int loyaltyPointsUsed = 0;
        private int loyaltyPointsEarned = 0;
        private BigDecimal cashTendered = BigDecimal.ZERO;
        private BigDecimal changeAmount = BigDecimal.ZERO;

        public Builder setSerial(int serial) {
            this.serialNumber = serial;
            return this;
        }

        public Builder setItems(List<BillItem> items) {
            this.billItems = items;
            return this;
        }

        public Builder setTotal(BigDecimal total) {
            this.totalAmount = total;
            return this;
        }

        public Builder setBillDate(LocalDateTime date) {
            this.billDate = date;
            return this;
        }

        public Builder setDiscount(BigDecimal discount) {
            this.discount = discount;
            return this;
        }

        public Builder setPaymentType(String type) {
            this.paymentType = type;
            return this;
        }

        public Builder setTransactionType(String type) {
            this.transactionType = type;
            return this;
        }

        public Builder setCustomerId(Integer id) {
            this.customerId = id;
            return this;
        }

        public Builder setCashierUsername(String username) {
            this.cashierUsername = username;
            return this;
        }

        public Builder setDiscountDescription(String description) {
            this.discountDescription = description;
            return this;
        }

        public Builder setLoyaltyPointsUsed(int points) {
            this.loyaltyPointsUsed = points;
            return this;
        }

        public Builder setLoyaltyPointsEarned(int points) {
            this.loyaltyPointsEarned = points;
            return this;
        }

        // new setters
        public Builder setCashTendered(BigDecimal cash) {
            this.cashTendered = cash;
            return this;
        }

        public Builder setChangeAmount(BigDecimal change) {
            this.changeAmount = change;
            return this;
        }

        public Bill build() { //used in oirstgresstransaction and billing service
            if (billItems == null) throw new IllegalStateException("Bill items cannot be null.");
            if (totalAmount == null) throw new IllegalStateException("Total amount is required.");
            return new Bill(this);
        }
    }
}