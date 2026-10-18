package com.syos.application.validation;

import com.syos.application.dto.BillItemRequest;


 // Abstract handler for the Chain of Responsibility pattern.
 // Each handler can process a request or pass it to the next handler.


 // srp each handler validates one aspect
 // open/closed new validators can add without change current ones

public abstract class ValidationHandler {
    protected ValidationHandler nextHandler;

      //Set the next handler in the chain

    public ValidationHandler setNext(ValidationHandler handler) {
        this.nextHandler = handler;
        return handler; //  chaining handler1.setNext(handler2).setNext(handler3)
    }

     // validate the request. returns error message or null if valid
    public abstract String validate(BillItemRequest request);

     // pass to next handler if exists

    protected String validateNext(BillItemRequest request) {
        if (nextHandler != null) {
            return nextHandler.validate(request);
        }
        return null; // All validations passed
    }
}
