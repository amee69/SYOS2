package com.syos.domain.exception;



public class InvalidQuantityException extends RuntimeException {
    private final int quantity;

    public InvalidQuantityException(int quantity) {
        super("Invalid quantity: " + quantity + ". Quantsity must be positive.");
        this.quantity = quantity;
    }

    public InvalidQuantityException(String message) {
        super(message);
        this.quantity = 0;
    }

    public int getQuantity() {
        return quantity;
    }
}