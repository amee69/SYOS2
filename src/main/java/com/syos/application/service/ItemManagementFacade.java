package com.syos.application.service;

import com.syos.application.dto.ItemCreateRequest;
import com.syos.domain.entity.Category;
import com.syos.domain.entity.Item;
import com.syos.domain.repository.CategoryRepository;
import com.syos.domain.repository.ItemRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


//  item code generation logic (prefix + sequence + year)
// category validation
// sequence number calculation

public class ItemManagementFacade {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    public ItemManagementFacade(ItemRepository itemRepository, CategoryRepository categoryRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
    }

    // ==================== public interface  ====================

   //get all valid categories.
      //controller uses this to display category menu.

    public List<Category> getCategories() {

        return categoryRepository.findAll();
    }





    public Item createItem(ItemCreateRequest request) {
        //  Validate category exists
        if (!categoryRepository.existsByCode(request.getCategoryCode())) {
            throw new IllegalArgumentException("Invalid category code: " + request.getCategoryCode());
        }

        //  generate item code
        String itemCode = generateItemCode(request.getCategoryCode());

        //  create and save item
        Item item = new Item(
                itemCode,
                request.getName(),
                request.getPrice(),
                request.getCategoryCode(),
                request.getReorderThreshold()
        );
        itemRepository.save(item);

        return item;
    }


     // deletes an item if it has no dependencies (Stock/Sales).

    public void deleteItem(String itemCode) {
       // check if it exists first
        if (!itemRepository.existsByCode(itemCode)) {
            throw new IllegalArgumentException("Item with code '" + itemCode + "' does not exist.");
        }

        // attempt delete (repository handles FK constraints)
        itemRepository.delete(itemCode);
    }






    // ==================== Private====================


//      code format stuff
//
//     the complex sub sys

    private String generateItemCode(String categoryCode) {
        // Get next sequence number for thiz category
        int sequence = getNextSequence(categoryCode); //below

        //  current year
        String year = String.valueOf(LocalDate.now().getYear());

        // bev-001-2026
        return String.format("%s-%03d-%s", categoryCode, sequence, year);
    }


      //calculates the next sequence number for a category.
     // counts existing items with same prefix and adds 1.

    private int getNextSequence(String categoryCode) {
        List<Item> allItems = itemRepository.findAll();

        long count = allItems.stream()
                .filter(item -> item.getItemCode().startsWith(categoryCode + "-"))
                .count();

        return (int) count + 1;
    }
}