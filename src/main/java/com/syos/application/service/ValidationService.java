package com.syos.application.service;

import com.syos.application.dto.BillItemRequest;
import com.syos.application.validation.*;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.StockRepository;


//service that validates checkout requests using chain of responsibility
public class ValidationService {
    private final ValidationHandler validationChain;

    public ValidationService(ItemRepository itemRepository, StockRepository stockRepository, String stockType) {
        // build the validation chain
        ValidationHandler itemValidator = new ItemCodeValidator(itemRepository);
        ValidationHandler quantityValidator = new QuantityValidator();
        ValidationHandler stockValidator = new StockAvailabilityValidator(stockRepository, stockType);


        //since these three chains instantiation is done
        //they can use validationhandler setNext method
        // since these inherit from validationHandler
        itemValidator.setNext(quantityValidator).setNext(stockValidator);

        this.validationChain = itemValidator;
    }
//     validate a single item request
//     return error messege or null if valid
      //expect BillItemRequest dto type
    public String validateItem(BillItemRequest request) {

        return validationChain.validate(request);
    }
}