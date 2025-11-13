package com.notfound.bookstore.controller;

import com.notfound.bookstore.model.dto.response.ApiResponse;
import com.notfound.bookstore.model.dto.response.categoryresponse.CategoryResponse;
import com.notfound.bookstore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable UUID categoryId) {
        CategoryResponse response = categoryService.getCategory(categoryId);
        return ApiResponse.<CategoryResponse>builder()
                .code(1000)
                .message("Lấy thông tin thể loại thành công")
                .result(response)
                .build();
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> response = categoryService.getAllCategories();
        return ApiResponse.<List<CategoryResponse>>builder()
                .code(1000)
                .message("Lấy danh sách thể loại thành công")
                .result(response)
                .build();
    }

    @GetMapping("/paged")
    public ApiResponse<Page<CategoryResponse>> getAllCategoriesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryResponse> response = categoryService.getAllCategories(pageable);
        return ApiResponse.<Page<CategoryResponse>>builder()
                .code(1000)
                .message("Lấy danh sách thể loại thành công")
                .result(response)
                .build();
    }
}

