package com.notfound.bookstore.controller;

import com.notfound.bookstore.model.dto.request.bookrequest.BookFilterRequest;
import com.notfound.bookstore.model.dto.request.bookrequest.BookSearchRequest;
import com.notfound.bookstore.model.dto.request.bookrequest.BookSortRequest;
import com.notfound.bookstore.model.dto.response.ApiResponse;
import com.notfound.bookstore.model.dto.response.bookresponse.BookResponse;
import com.notfound.bookstore.model.dto.response.bookresponse.BookSummaryResponse;
import com.notfound.bookstore.model.dto.response.bookresponse.PageResponse;
import com.notfound.bookstore.model.entity.Book;
import com.notfound.bookstore.service.BookService;
import com.notfound.bookstore.service.impl.BookServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // Tìm kiếm sách theo từ khóa (tên sách, tác giả, hoặc thể loại)
    @GetMapping("/search")
    public ApiResponse<PageResponse<BookSummaryResponse>> searchBooks(@ModelAttribute BookSearchRequest request)
    {
        return ApiResponse.<PageResponse<BookSummaryResponse>>builder()
                .code(1000)
                .message("Tìm kiếm sách thành công")
                .result(bookService.searchBooks(request))
                .build();
    }

    // Lọc sách theo các tiêu chí: giá, đánh giá trung bình và ngày phát hành
    @GetMapping("/filter")
    public ApiResponse<PageResponse<BookSummaryResponse>> filterBooks(
            @Valid @ModelAttribute BookFilterRequest request) {
        return ApiResponse.<PageResponse<BookSummaryResponse>>builder()
                .code(1000)
                .message("Lọc sách thành công")
                .result(bookService.findByFilters(request))
                .build();
    }

    // Lấy danh sách sách được sắp xếp theo loại sắp xếp được chỉ định
    @GetMapping("/sorted")
    public ApiResponse<PageResponse<BookSummaryResponse>> getSortedBooks(@ModelAttribute BookSortRequest request) {
        return ApiResponse.<PageResponse<BookSummaryResponse>>builder()
                .code(1000)
                .message("Sắp xếp sách thành công")
                .result(bookService.getSortedBooks(request))
                .build();
    }

    // Lấy tất cả sách với phân trang
    @GetMapping
    public ApiResponse<PageResponse<BookSummaryResponse>> getAllBooks(@ModelAttribute BookSearchRequest request) {
        return ApiResponse.<PageResponse<BookSummaryResponse>>builder()
                .code(1000)
                .message("Lấy danh sách sách thành công")
                .result(bookService.searchBooks(request))
                .build();
    }

    // Lấy thông tin chi tiết của một cuốn sách dựa trên ID
    @GetMapping("/{id}")
    public ApiResponse<BookResponse> getBookById(@PathVariable String id) {
        return ApiResponse.<BookResponse>builder()
                .code(1000)
                .message("Lấy thông tin sách thành công")
                .result(bookService.getBookById(id))
                .build();
    }
}
