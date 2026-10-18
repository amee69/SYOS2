package com.syos.application.validation;

import com.syos.application.dto.BillItemRequest;
import com.syos.domain.repository.ItemRepository;


 //Validates that an item code exists in the system.
 //Design Pattern: Chain of Responsibility (koncrete Handler)

public class ItemCodeValidator extends ValidationHandler {
    private final ItemRepository itemRepository;

    public ItemCodeValidator(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public String validate(BillItemRequest request) {
        // Check if item exists
        if (!itemRepository.existsByCode(request.getItemCode())) {
            return "== Item not founds: " + request.getItemCode();
        }

        // Pass to next handler
        return validateNext(request);
    }
}