package com.notfound.bookstore.controller;

import com.notfound.bookstore.model.entity.Book;
import com.notfound.bookstore.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/books")
public class BookController {
    @Autowired
    private BookService bookService;

    //Tìm kiếm sách theo từ khóa
    @GetMapping("/search")
    public ResponseEntity<Page<Book>> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookService.searchBooks(keyword, pageable);
        return ResponseEntity.ok(books);
    }

    //Lọc sách theo giá, đánh giá và ngày phát hành
    @GetMapping("/filter")
    public ResponseEntity<Page<Book>> filterBooks(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishedAfter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookService.findByFilters(minPrice, maxPrice, minRating, publishedAfter, pageable);
        return ResponseEntity.ok(books);
    }

    //Lấy danh sách sách với sắp xếp
    @GetMapping("/sorted")
    public ResponseEntity<Page<Book>> getSortedBooks(
            @RequestParam(defaultValue = "date_desc") String sortType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Book> books = bookService.getSortedBooks(sortType, page, size);
        return ResponseEntity.ok(books);
    }

    //Lấy tất cả sách có phân trang
    @GetMapping
    public ResponseEntity<Page<Book>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookService.searchBooks(null, pageable);
        return ResponseEntity.ok(books);
    }
}
