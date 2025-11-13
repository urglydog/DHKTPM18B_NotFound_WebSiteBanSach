package com.notfound.bookstore.controller;

import com.notfound.bookstore.model.dto.request.bookrequest.CreateBookRequest;
import com.notfound.bookstore.model.dto.request.bookrequest.UpdateBookRequest;
import com.notfound.bookstore.model.dto.request.categoryrequest.CreateCategoryRequest;
import com.notfound.bookstore.model.dto.request.categoryrequest.UpdateCategoryRequest;
import com.notfound.bookstore.model.dto.response.ApiResponse;
import com.notfound.bookstore.model.dto.response.bookresponse.BookFullDetailResponse;
import com.notfound.bookstore.model.dto.response.categoryresponse.CategoryResponse;
import com.notfound.bookstore.service.AdminService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminController {

    AdminService adminService;

    @PostMapping("/books")
    public ApiResponse<BookFullDetailResponse> createBook(@Valid @RequestBody CreateBookRequest request) {
        BookFullDetailResponse response = adminService.createBook(request);
        return ApiResponse.<BookFullDetailResponse>builder()
                .code(1000)
                .message("Tạo sách thành công")
                .result(response)
                .build();
    }

    @PutMapping("/books/{bookId}")
    public ApiResponse<BookFullDetailResponse> updateBook(
            @PathVariable UUID bookId,
            @Valid @RequestBody UpdateBookRequest request) {
        BookFullDetailResponse response = adminService.updateBook(bookId, request);
        return ApiResponse.<BookFullDetailResponse>builder()
                .code(1000)
                .message("Cập nhật sách thành công")
                .result(response)
                .build();
    }

    @DeleteMapping("/books/{bookId}")
    public ApiResponse<Void> deleteBook(@PathVariable UUID bookId) {
        adminService.deleteBook(bookId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Xóa sách thành công")
                .build();
    }

    @GetMapping("/books/{bookId}")
    public ApiResponse<BookFullDetailResponse> getBookDetail(@PathVariable UUID bookId) {
        BookFullDetailResponse response = adminService.getBookDetail(bookId);
        return ApiResponse.<BookFullDetailResponse>builder()
                .code(1000)
                .message("Lấy thông tin sách thành công")
                .result(response)
                .build();
    }

    @GetMapping("/books")
    public ApiResponse<Page<BookFullDetailResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookFullDetailResponse> response = adminService.getAllBooks(pageable);
        return ApiResponse.<Page<BookFullDetailResponse>>builder()
                .code(1000)
                .message("Lấy danh sách sách thành công")
                .result(response)
                .build();
    }

    @PostMapping("/books/{bookId}/images")
    public ApiResponse<BookFullDetailResponse> uploadBookImages(
            @PathVariable UUID bookId,
            @RequestParam("images") List<MultipartFile> images) {
        BookFullDetailResponse response = adminService.uploadBookImages(bookId, images);
        return ApiResponse.<BookFullDetailResponse>builder()
                .code(1000)
                .message("Upload ảnh thành công")
                .result(response)
                .build();
    }

    @DeleteMapping("/books/{bookId}/images/{imageId}")
    public ApiResponse<Void> deleteBookImage(
            @PathVariable UUID bookId,
            @PathVariable Long imageId) {
        adminService.deleteBookImage(bookId, imageId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Xóa ảnh thành công")
                .build();
    }

    @PostMapping("/categories")
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = adminService.createCategory(request);
        return ApiResponse.<CategoryResponse>builder()
                .code(1000)
                .message("Tạo thể loại thành công")
                .result(response)
                .build();
    }

    @PutMapping("/categories/{categoryId}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse response = adminService.updateCategory(categoryId, request);
        return ApiResponse.<CategoryResponse>builder()
                .code(1000)
                .message("Cập nhật thể loại thành công")
                .result(response)
                .build();
    }

    @DeleteMapping("/categories/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable UUID categoryId) {
        adminService.deleteCategory(categoryId);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Xóa thể loại thành công")
                .build();
    }

    @GetMapping("/categories/{categoryId}")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable UUID categoryId) {
        CategoryResponse response = adminService.getCategory(categoryId);
        return ApiResponse.<CategoryResponse>builder()
                .code(1000)
                .message("Lấy thông tin thể loại thành công")
                .result(response)
                .build();
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> response = adminService.getAllCategories();
        return ApiResponse.<List<CategoryResponse>>builder()
                .code(1000)
                .message("Lấy danh sách thể loại thành công")
                .result(response)
                .build();
    }

    @GetMapping("/categories/paged")
    public ApiResponse<Page<CategoryResponse>> getAllCategoriesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryResponse> response = adminService.getAllCategories(pageable);
        return ApiResponse.<Page<CategoryResponse>>builder()
                .code(1000)
                .message("Lấy danh sách thể loại thành công")
                .result(response)
                .build();
    }
}
