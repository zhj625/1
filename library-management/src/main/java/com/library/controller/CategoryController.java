package com.library.controller;

import com.library.common.Result;
import com.library.dto.request.CategoryRequest;
import com.library.dto.response.CategoryResponse;
import com.library.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public Result<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return Result.success(categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public Result<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return Result.success(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return Result.success(categoryService.getCategoryById(id));
    }

    @GetMapping
    public Result<List<CategoryResponse>> getAllCategories() {
        return Result.success(categoryService.getAllCategories());
    }

    @GetMapping("/tree")
    public Result<List<CategoryResponse>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }
}
