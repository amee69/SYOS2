package com.syos.domain.entity;


 // products category.
 //  ItemManagementFacade to validate categories
 // and generate proper item codes.

public class Category {
    private final String code;        // "BEV"
    private final String name;        // "Beverages"
    private final String description; // "Tea, coffee, drinks"

    public Category(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}