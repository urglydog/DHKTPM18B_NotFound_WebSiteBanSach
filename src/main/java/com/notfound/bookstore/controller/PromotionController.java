package com.notfound.bookstore.controller;

import com.notfound.bookstore.model.dto.request.promotionrequest.CreatePromotionRequest;
import com.notfound.bookstore.model.dto.request.promotionrequest.UpdatePromotionRequest;
import com.notfound.bookstore.model.dto.request.promotionrequest.ValidatePromotionCodeRequest;
import com.notfound.bookstore.model.dto.response.ApiResponse;
import com.notfound.bookstore.model.dto.response.promotionresponse.PromotionResponse;
import com.notfound.bookstore.model.dto.response.promotionresponse.PromotionValidationResponse;
import com.notfound.bookstore.service.PromotionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionController {

    PromotionService promotionService;

    /**
     * Tạo khuyến mãi mới (Admin only)
     * POST /api/promotions
     */
    @PostMapping
    public ApiResponse<PromotionResponse> createPromotion(
            @Valid @RequestBody CreatePromotionRequest request) {
        PromotionResponse promotion = promotionService.createPromotion(request);
        return ApiResponse.<PromotionResponse>builder()
                .code(1000)
                .message("Tạo khuyến mãi thành công")
                .result(promotion)
                .build();
    }

    /**
     * Cập nhật khuyến mãi (Admin only)
     * PUT /api/promotions/{id}
     */
    @PutMapping("/{id}")
    public ApiResponse<PromotionResponse> updatePromotion(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePromotionRequest request) {
        PromotionResponse promotion = promotionService.updatePromotion(id, request);
        return ApiResponse.<PromotionResponse>builder()
                .code(1000)
                .message("Cập nhật khuyến mãi thành công")
                .result(promotion)
                .build();
    }

    /**
     * Xóa khuyến mãi (Admin only)
     * DELETE /api/promotions/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePromotion(@PathVariable UUID id) {
        promotionService.deletePromotion(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Xóa khuyến mãi thành công")
                .build();
    }

    /**
     * Lấy khuyến mãi theo ID
     * GET /api/promotions/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<PromotionResponse> getPromotionById(@PathVariable UUID id) {
        PromotionResponse promotion = promotionService.getPromotionById(id);
        return ApiResponse.<PromotionResponse>builder()
                .code(1000)
                .message("Lấy thông tin khuyến mãi thành công")
                .result(promotion)
                .build();
    }

    /**
     * Lấy tất cả khuyến mãi (có phân trang) (Admin only)
     * GET /api/promotions?page=0&size=10
     */
    @GetMapping
    public ApiResponse<Page<PromotionResponse>> getAllPromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PromotionResponse> promotions = promotionService.getAllPromotions(pageable);
        return ApiResponse.<Page<PromotionResponse>>builder()
                .code(1000)
                .message("Lấy danh sách khuyến mãi thành công")
                .result(promotions)
                .build();
    }

    /**
     * Lấy danh sách khuyến mãi đang hoạt động (Public)
     * GET /api/promotions/active
     */
    @GetMapping("/active")
    public ApiResponse<List<PromotionResponse>> getActivePromotions() {
        List<PromotionResponse> promotions = promotionService.getActivePromotions();
        return ApiResponse.<List<PromotionResponse>>builder()
                .code(1000)
                .message("Lấy danh sách khuyến mãi đang hoạt động thành công")
                .result(promotions)
                .build();
    }

    /**
     * Lấy khuyến mãi áp dụng cho sách
     * GET /api/promotions/book/{bookId}
     */
    @GetMapping("/book/{bookId}")
    public ApiResponse<List<PromotionResponse>> getPromotionsByBookId(
            @PathVariable UUID bookId) {
        List<PromotionResponse> promotions = promotionService.getPromotionsByBookId(bookId);
        return ApiResponse.<List<PromotionResponse>>builder()
                .code(1000)
                .message("Lấy danh sách khuyến mãi cho sách thành công")
                .result(promotions)
                .build();
    }

    /**
     * Validate mã khuyến mãi (Public)
     * POST /api/promotions/validate
     */
    @PostMapping("/validate")
    public ApiResponse<PromotionValidationResponse> validatePromotionCode(
            @Valid @RequestBody ValidatePromotionCodeRequest request) {
        PromotionValidationResponse response = promotionService.validatePromotionCode(request);
        return ApiResponse.<PromotionValidationResponse>builder()
                .code(response.getIsValid() ? 1000 : 4002)
                .message(response.getMessage())
                .result(response)
                .build();
    }

    /**
     * Cập nhật trạng thái khuyến mãi (Admin only)
     * PATCH /api/promotions/{id}/status?status=ACTIVE
     */
    @PatchMapping("/{id}/status")
    public ApiResponse<PromotionResponse> updatePromotionStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        PromotionResponse promotion = promotionService.updatePromotionStatus(id, status);
        return ApiResponse.<PromotionResponse>builder()
                .code(1000)
                .message("Cập nhật trạng thái khuyến mãi thành công")
                .result(promotion)
                .build();
    }
}
