package com.syos.application.dto;

import java.math.BigDecimal;


  //Data Transfer Object for bill item requests.
  //used to transfer data between  layers.


public class BillItemRequest {
    private final String itemCode;
    private final int quantity;

    //billItemRequest dto
    public BillItemRequest(String itemCode, int quantity) {
        if (itemCode == null || itemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Item code cannot be null or empty");
        }
//        if (quantity <= 0) {
//            throw new IllegalArgumentException("Quantity must be positive");
//        } commented to show chain of resoponsibility
        this.itemCode = itemCode.trim();
        this.quantity = quantity;
    }

    public String getItemCode() {
        return itemCode;
    }

    public int getQuantity() {

        return quantity;
    }

    @Override
    public String toString() {
        return "BillItemRequest{itemCode='" + itemCode + "', quantity=" + quantity + "}";
    }
}