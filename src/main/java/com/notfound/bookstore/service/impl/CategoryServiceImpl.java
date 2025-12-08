package com.notfound.bookstore.service.impl;

import com.notfound.bookstore.exception.AppException;
import com.notfound.bookstore.exception.ErrorCode;
import com.notfound.bookstore.model.dto.response.categoryresponse.CategoryResponse;
import com.notfound.bookstore.model.entity.Category;
import com.notfound.bookstore.repository.CategoryRepository;
import com.notfound.bookstore.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    // Lấy category theo ID
    @Override
    public CategoryResponse getCategory(UUID categoryId) {
        log.info("Fetching category with ID: {}", categoryId);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        log.info("Found category: {}", category.getName());
        return mapToCategoryResponse(category);
    }

    // Lấy tất cả các Category
    @Override
    public List<CategoryResponse> getAllCategories() {
        log.info("Fetching all categories");
        List<Category> categories = categoryRepository.findAll();
        log.info("Found {} categories", categories.size());
        
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    // Lấy tất cả các Category với phân trang
    @Override
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        log.info("Fetching categories with pagination - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        log.info("Found {} categories on page {}", categoryPage.getNumberOfElements(), pageable.getPageNumber());
        
        return categoryPage.map(this::mapToCategoryResponse);
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        CategoryResponse.CategoryResponseBuilder builder = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription());

        if (category.getParentCategory() != null) {
            builder.parentCategoryId(category.getParentCategory().getId())
                   .parentCategoryName(category.getParentCategory().getName());
        }

        return builder.build();
    }
}

