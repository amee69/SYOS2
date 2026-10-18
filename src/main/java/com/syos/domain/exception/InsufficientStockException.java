package com.syos.domain.exception;


//  domain exception thrown when there is not enough stock to fulfill a request.

//  clean Architecture Domain exceptions represent business rule violations.

public class InsufficientStockException extends RuntimeException {
    private final String itemCode;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(String itemCode, int requestedQuantity, int availableQuantity) {
        super(String.format(
                "Insufficient stock for item '%s': requested %d, available %d",
                itemCode, requestedQuantity, availableQuantity
        ));
        this.itemCode = itemCode;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

}