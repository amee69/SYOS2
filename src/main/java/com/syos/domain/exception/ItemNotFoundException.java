package com.syos.domain.exception;


public class ItemNotFoundException extends RuntimeException {
    private final String itemCode;

    public ItemNotFoundException(String itemCode) {
        super("Item not found with code: " + itemCode);
        this.itemCode = itemCode;
    }

    public ItemNotFoundException(String message, String itemCode) {
        super(message);
        this.itemCode = itemCode;
    }

    public String getItemCode() {
        return itemCode;
    }
}