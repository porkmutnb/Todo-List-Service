package com.chermew.todolist.controller;

import com.chermew.todolist.annotation.LogActivity;
import com.chermew.todolist.dto.*;
import com.chermew.todolist.security.UserPrincipal;
import com.chermew.todolist.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @LogActivity(action = "CATEGORY_CREATE", entityType = "categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        CategoryResponse response = categoryService.createCategory(request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", response));
    }

    @PutMapping("/{categoryId}")
    @LogActivity(action = "CATEGORY_UPDATE", entityType = "categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        CategoryResponse response = categoryService.updateCategory(categoryId, request, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    @GetMapping("/{categoryId}")
    @LogActivity(action = "CATEGORY_GET_BY_ID", entityType = "categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        CategoryResponse response = categoryService.getCategoryById(categoryId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Category fetched successfully", response));
    }

    @DeleteMapping("/{categoryId}")
    @LogActivity(action = "CATEGORY_DELETE", entityType = "categories")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        categoryService.deleteCategory(categoryId, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }

}
