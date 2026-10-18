package com.syos.application.validation;

import com.syos.application.dto.BillItemRequest;
import com.syos.domain.repository.StockRepository;

 // validates that sufficient stock is available.
 // concrete 2nd

public class StockAvailabilityValidator extends ValidationHandler {
    private final StockRepository stockRepository;
    private final String stockType; // "SHELF" or "WEB"

    public StockAvailabilityValidator(StockRepository stockRepository, String stockType) {
        this.stockRepository = stockRepository;
        this.stockType = stockType;
    }

    @Override
    public String validate(BillItemRequest request) {
        int availableStock = stockRepository.getQuantityByLocation(
                request.getItemCode(),
                stockType
        );

        if (availableStock < request.getQuantity()) {
            return String.format(
                    "== Insufficient stock for %s. Requested: %d, Available: %d",
                    request.getItemCode(),
                    request.getQuantity(),
                    availableStock
            );
        }

        // Pass to next handler
        return validateNext(request);
    }
}