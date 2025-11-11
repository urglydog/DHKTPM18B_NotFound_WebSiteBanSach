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
    public ApiResponse<PageResponse<BookSummaryResponse>> searchBooks(@RequestParam(required = false) String keyword,
                                                                      @RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size) {
        PageResponse<BookSummaryResponse> books = bookService.searchBooks(BookSearchRequest.builder()
                .keyword(keyword)
                .page(page)
                .size(size)
                .build()
        );
        return ApiResponse.<PageResponse<BookSummaryResponse>>builder()
                .code(1000)
                .message("Tìm kiếm sách thành công")
                .result(books)
                .build();
    }

    // Lọc sách theo các tiêu chí: giá, đánh giá trung bình và ngày phát hành
    @GetMapping("/filter")
    public ResponseEntity<PageResponse<BookSummaryResponse>> filterBooks(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishedAfter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        BookFilterRequest request = BookFilterRequest.builder()
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .publishedAfter(publishedAfter)
                .page(page)
                .size(size)
                .build();

        PageResponse<BookSummaryResponse> books = bookService.findByFilters(request);
        return ResponseEntity.ok(books);
    }

    // Lấy danh sách sách được sắp xếp theo loại sắp xếp được chỉ định
    @GetMapping("/sorted")
    public ResponseEntity<PageResponse<BookSummaryResponse>> getSortedBooks(
            @RequestParam(defaultValue = "date_desc") String sortType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        BookSortRequest request = BookSortRequest.builder()
                .sortType(sortType)
                .page(page)
                .size(size)
                .build();

        PageResponse<BookSummaryResponse> books = bookService.getSortedBooks(request);
        return ResponseEntity.ok(books);
    }

    // Lấy tất cả sách với phân trang
    @GetMapping
    public ResponseEntity<PageResponse<BookSummaryResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        BookSearchRequest request = BookSearchRequest.builder()
                .page(page)
                .size(size)
                .build();

        PageResponse<BookSummaryResponse> books = bookService.searchBooks(request);
        return ResponseEntity.ok(books);
    }

    // Lấy thông tin chi tiết của một cuốn sách dựa trên ID
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable String id) {
        BookResponse book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }
}
