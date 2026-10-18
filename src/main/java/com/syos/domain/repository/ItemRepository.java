package com.syos.domain.repository;

import com.syos.domain.entity.Item;
import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Optional<Item> findByCode(String itemCode);
    List<Item> findAll(); // Added for reports
    void save(Item item);
    boolean existsByCode(String itemCode);

    void delete(String itemCode);
}

