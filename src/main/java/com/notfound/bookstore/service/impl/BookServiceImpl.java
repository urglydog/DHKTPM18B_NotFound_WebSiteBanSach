package com.notfound.bookstore.service.impl;

import com.notfound.bookstore.model.dto.request.bookrequest.BookFilterRequest;
import com.notfound.bookstore.model.dto.request.bookrequest.BookSearchRequest;
import com.notfound.bookstore.model.dto.request.bookrequest.BookSortRequest;
import com.notfound.bookstore.model.dto.response.bookresponse.BookResponse;
import com.notfound.bookstore.model.dto.response.bookresponse.BookSummaryResponse;
import com.notfound.bookstore.model.dto.response.bookresponse.PageResponse;
import com.notfound.bookstore.model.entity.Book;
import com.notfound.bookstore.model.mapper.BookMapper;
import com.notfound.bookstore.repository.BookRepository;
import com.notfound.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDate;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    @Autowired
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    //Tìm kiếm sách theo từ khóa (tên sách, tác giả, hoặc thể loại)
    @Override
    public PageResponse<BookSummaryResponse> searchBooks(BookSearchRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10
        );

        Page<Book> bookPage;
        if (!StringUtils.hasText(request.getKeyword())) {
            bookPage = bookRepository.findAll(pageable);
        } else {
            String keyword = request.getKeyword().trim();
            log.info("Searching with keyword: " , keyword);
            bookPage = bookRepository.searchBooks(keyword, pageable);
            log.info("Found books: " , bookPage.getTotalElements()); // ← Thêm log
        }

        Page<BookSummaryResponse> responsePage = bookPage.map(bookMapper::toBookSummaryResponse);
        return bookMapper.toPageResponse(responsePage);
    }

    // Lọc sách theo các tiêu chí: giá, đánh giá trung bình và ngày phát hành
    @Override
    public PageResponse<BookSummaryResponse> findByFilters(BookFilterRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<Book> bookPage = bookRepository.findByFilters(
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getMinRating(),
                request.getPublishedAfter(),
                pageable
        );

        Page<BookSummaryResponse> responsePage = bookPage.map(bookMapper::toBookSummaryResponse);
        return bookMapper.toPageResponse(responsePage);
    }

    // Lấy danh sách sách được sắp xếp theo loại sắp xếp được chỉ định
    @Override
    public PageResponse<BookSummaryResponse> getSortedBooks(BookSortRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<Book> bookPage = switch (request.getSortType()) {
            case "price_asc" -> bookRepository.findAllByOrderByPriceAsc(pageable);
            case "price_desc" -> bookRepository.findAllByOrderByPriceDesc(pageable);
            case "title_asc" -> bookRepository.findAllByOrderByTitleAsc(pageable);
            case "title_desc" -> bookRepository.findAllByOrderByTitleDesc(pageable);
            case "date_asc" -> bookRepository.findAllByOrderByPublishDateAsc(pageable);
            case "date_desc" -> bookRepository.findAllByOrderByPublishDateDesc(pageable);
            case "rating_asc" -> bookRepository.findAllOrderByAverageRatingAsc(pageable);
            case "rating_desc" -> bookRepository.findAllOrderByAverageRatingDesc(pageable);
            default -> bookRepository.findAllByOrderByPublishDateDesc(pageable);
        };

        Page<BookSummaryResponse> responsePage = bookPage.map(bookMapper::toBookSummaryResponse);
        return bookMapper.toPageResponse(responsePage);
    }

    // Lấy thông tin chi tiết của một cuốn sách dựa trên ID
    @Override
    public BookResponse getBookById(String id) {
        Book book = bookRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return bookMapper.toBookResponse(book);
    }
}
