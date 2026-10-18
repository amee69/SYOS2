package com.syos.domain.repository;

import com.syos.domain.entity.Category;
import java.util.List;
import java.util.Optional;


// abstract data access, repository pattern

public interface CategoryRepository {
    List<Category> findAll();
    Optional<Category> findByCode(String code);
    boolean existsByCode(String code);
}