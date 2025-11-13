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


/**
 * Controller xử lý các chức năng liên quan đến sách
 * Bao gồm tìm kiếm, lọc, sắp xếp và xem thông tin chi tiết sách
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * Tìm kiếm sách theo từ khóa
     * Hỗ trợ tìm kiếm theo tên sách, tác giả hoặc thể loại với phân trang
     *
     * @param request Thông tin tìm kiếm bao gồm từ khóa, số trang và kích thước trang
     * @return Danh sách sách tìm được với thông tin phân trang
     */
    @GetMapping("/search")
    public ApiResponse<PageResponse<BookSummaryResponse>> searchBooks(@ModelAttribute BookSearchRequest request)
    {
        return ApiResponse.<PageResponse<BookSummaryResponse>>builder()
                .code(1000)
                .message("Tìm kiếm sách thành công")
                .result(bookService.searchBooks(request))
                .build();
    }

    /**
     * Lọc sách theo các tiêu chí
     * Hỗ trợ lọc theo khoảng giá, đánh giá trung bình và ngày phát hành
     *
     * @param request Các tiêu chí lọc bao gồm minPrice, maxPrice, minRating, maxRating, fromDate, toDate
     * @return Danh sách sách phù hợp với bộ lọc được phân trang
     */
    @GetMapping("/filter")
    public ApiResponse<PageResponse<BookSummaryResponse>> filterBooks(
            @Valid @ModelAttribute BookFilterRequest request) {
        return ApiResponse.<PageResponse<BookSummaryResponse>>builder()
                .code(1000)
                .message("Lọc sách thành công")
                .result(bookService.findByFilters(request))
                .build();
    }

    /**
     * Sắp xếp danh sách sách theo tiêu chí
     * Hỗ trợ sắp xếp theo giá, đánh giá, ngày phát hành, tên sách với thứ tự tăng/giảm dần
     *
     * @param request Thông tin sắp xếp bao gồm sortBy (price, rating, releaseDate, title) và direction (ASC/DESC)
     * @return Danh sách sách được sắp xếp với phân trang
     */
    @GetMapping("/sorted")
    public ApiResponse<PageResponse<BookSummaryResponse>> getSortedBooks(@ModelAttribute BookSortRequest request) {
        return ApiResponse.<PageResponse<BookSummaryResponse>>builder()
                .code(1000)
                .message("Sắp xếp sách thành công")
                .result(bookService.getSortedBooks(request))
                .build();
    }

    /**
     * Lấy danh sách tất cả sách
     * Hỗ trợ phân trang để hiển thị danh sách sách
     *
     * @param request Thông tin phân trang (số trang và kích thước trang)
     * @return Danh sách tất cả sách được phân trang
     */
    @GetMapping
    public ApiResponse<PageResponse<BookSummaryResponse>> getAllBooks(@ModelAttribute BookSearchRequest request) {
        return ApiResponse.<PageResponse<BookSummaryResponse>>builder()
                .code(1000)
                .message("Lấy danh sách sách thành công")
                .result(bookService.searchBooks(request))
                .build();
    }

    /**
     * Lấy thông tin chi tiết của một cuốn sách
     * Bao gồm thông tin đầy đủ về sách: mô tả, hình ảnh, đánh giá, số lượng tồn kho...
     *
     * @param id ID của sách cần xem chi tiết
     * @return Thông tin chi tiết đầy đủ của sách
     */
    @GetMapping("/{id}")
    public ApiResponse<BookResponse> getBookById(@PathVariable String id) {
        return ApiResponse.<BookResponse>builder()
                .code(1000)
                .message("Lấy thông tin sách thành công")
                .result(bookService.getBookById(id))
                .build();
    }
}
