package com.syos.application.validation;

import com.syos.application.dto.BillItemRequest;


//validates that quantity is valid (positive and within limits).

 // chain respons(Concrete Handler)

public class QuantityValidator extends ValidationHandler {
    private static final int MAX_QUANTITY_PER_ITEM = 1000;

    @Override
    public String validate(BillItemRequest request) {
        int quantity = request.getQuantity();

        if (quantity <= 0) {
            return "==Quantity must be positive. Got: " + quantity;
        }

        if (quantity > MAX_QUANTITY_PER_ITEM) {
            return "Quantity exceeds maximum allowed (" + MAX_QUANTITY_PER_ITEM + "). Got: " + quantity;
        }

        // Pass to next handler
        return validateNext(request);
    }
}