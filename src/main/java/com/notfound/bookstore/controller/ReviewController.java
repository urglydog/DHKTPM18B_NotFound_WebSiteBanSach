package com.notfound.bookstore.controller;

import com.notfound.bookstore.model.dto.request.reviewrequest.CreateReviewRequest;
import com.notfound.bookstore.model.dto.response.ApiResponse;
import com.notfound.bookstore.model.dto.response.reviewresponse.ReviewResponse;
import com.notfound.bookstore.service.ReviewService;
import com.notfound.bookstore.service.impl.ReviewServiceImpl;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {
    ReviewService reviewService;

    @PostMapping("/book/add")
    public ApiResponse<ReviewResponse> addReviewBook(@Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse review = reviewService.addReviewBook(request);
        return ApiResponse.<ReviewResponse>builder()
                .code(1000)
                .message("Thêm đánh giá thành công")
                .result(review)
                .build();
    }

    @GetMapping("/book/{bookId}")
    public ApiResponse<Page<ReviewResponse>> getListReviewByBookId(
            @PathVariable UUID bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReviewResponse> reviews = reviewService.getReviewsByBookId(bookId, page, size);
        return ApiResponse.<Page<ReviewResponse>>builder()
                .code(1000)
                .message("Lấy danh sách đánh giá thành công")
                .result(reviews)
                .build();
    }
}
