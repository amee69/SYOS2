package com.syos.application.factory;

import com.syos.application.service.ValidationService;
import com.syos.domain.repository.ItemRepository;
import com.syos.domain.repository.StockRepository;

public class ValidationServiceFactory {
    private final ItemRepository itemRepository;
    private final StockRepository stockRepository;

    public ValidationServiceFactory(ItemRepository itemRepository, StockRepository stockRepository) {
        this.itemRepository = itemRepository;
        this.stockRepository = stockRepository;
    }

    public ValidationService create(String stockType) {
        return new ValidationService(itemRepository, stockRepository, stockType);
    }
}