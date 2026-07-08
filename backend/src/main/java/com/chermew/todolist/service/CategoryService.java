package com.chermew.todolist.service;

import com.chermew.todolist.dto.*;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request, UUID userId);
    CategoryResponse updateCategory(UUID categoryId, CategoryRequest request, UUID userId);
    CategoryResponse getCategoryById(UUID categoryId, UUID userId);
    void deleteCategory(UUID categoryId, UUID id);
}
