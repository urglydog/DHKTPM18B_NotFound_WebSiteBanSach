package com.notfound.bookstore.service.impl;

import com.notfound.bookstore.model.dto.request.bookrequest.BookFilterRequest;
import com.notfound.bookstore.model.dto.request.bookrequest.BookSearchRequest;
import com.notfound.bookstore.model.dto.request.bookrequest.BookSortRequest;
import com.notfound.bookstore.model.dto.request.bookrequest.BookWithRating;
import com.notfound.bookstore.model.dto.response.bookresponse.BookResponse;
import com.notfound.bookstore.model.dto.response.bookresponse.BookSummaryResponse;
import com.notfound.bookstore.model.dto.response.bookresponse.PageResponse;
import com.notfound.bookstore.model.entity.Book;
import com.notfound.bookstore.model.mapper.BookMapper;
import com.notfound.bookstore.repository.BookRepository;
import com.notfound.bookstore.service.BookService;
import com.notfound.bookstore.service.GeminiService;
import com.notfound.bookstore.service.QdrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final GeminiService geminiService;
    private final QdrantService qdrantService;

    // Tìm kiếm sách theo từ khóa (tên sách, tác giả, hoặc thể loại)
    @Override
    public PageResponse<BookSummaryResponse> searchBooks(BookSearchRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10);

        // TRƯỜNG HỢP 1: KHÔNG NHẬP KEYWORD → LẤY ALL
        if (!StringUtils.hasText(request.getKeyword())) {
            Page<Book> bookPage = bookRepository.findAll(pageable);
            Page<BookSummaryResponse> responsePage = bookPage.map(bookMapper::toBookSummaryResponse);
            return bookMapper.toPageResponse(responsePage);
        }
        // TRƯỜNG HỢP 2: CÓ KEYWORD → DÙNG AI SEARCH
        String keyword = request.getKeyword().trim();
        log.info("AI Searching with keyword: {}", keyword);

        // Gemini → Vector
        double[] queryVector = geminiService.embed(keyword);

        // Qdrant → Search vector → Lấy danh sách BookID
        List<String> bookIds = qdrantService.searchBookIds(queryVector, 50);

        // Nếu AI không trả ra kết quả
        if (bookIds.isEmpty()) {
            log.warn("AI search empty → fallback to database search");
            Page<Book> bookPage = bookRepository.searchBooks(keyword, pageable);
            Page<BookSummaryResponse> responsePage = bookPage.map(bookMapper::toBookSummaryResponse);
            return bookMapper.toPageResponse(responsePage);
        }

        // Lấy danh sách Book từ database theo bookIds
        List<UUID> uuidList = bookIds.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());

        List<Book> dbBooks = bookRepository.findAllById(uuidList);

        Map<UUID, Book> bookMap = dbBooks.stream()
                .collect(Collectors.toMap(Book::getId, b -> b));

        List<Book> books = uuidList.stream()
                .map(bookMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Manual paging vì Qdrant không hỗ trợ Pageable
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), books.size());
        List<Book> pagedBooks = books.subList(start, end);

        Page<Book> bookPage = new PageImpl<>(pagedBooks, pageable, books.size());

        Page<BookSummaryResponse> responsePage = bookPage.map(bookMapper::toBookSummaryResponse);

        return bookMapper.toPageResponse(responsePage);
    }

    // Lọc sách theo các tiêu chí: giá, đánh giá trung bình, ngày phát hành và từ
    // khóa
    @Override
    public PageResponse<BookSummaryResponse> findByFilters(BookFilterRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<BookWithRating> resultPage = bookRepository.findByFiltersWithRating(
                request.getKeyword(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getMinRating(),
                request.getPublishedAfter(),
                pageable);

        Page<BookSummaryResponse> responsePage = resultPage.map(result -> {
            BookSummaryResponse response = bookMapper.toBookSummaryResponse(result.getBook());
            response.setAverageRating(result.getAverageRating());
            response.setReviewCount(result.getReviewCount().intValue());
            return response;
        });

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

    // Lấy tất cả sách với phân trang đơn giản
    @Override
    public PageResponse<BookSummaryResponse> getAllBooks(Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                pageSize != null ? pageSize : 10);

        Page<BookWithRating> resultPage = bookRepository.findAllBooksWithRating(pageable);

        Page<BookSummaryResponse> responsePage = resultPage.map(result -> {
            BookSummaryResponse response = bookMapper.toBookSummaryResponse(result.getBook());
            response.setAverageRating(result.getAverageRating());
            response.setReviewCount(result.getReviewCount().intValue());
            return response;
        });

        return bookMapper.toPageResponse(responsePage);
    }

    // Lấy thông tin chi tiết của một cuốn sách dựa trên ID
    @Override
    public BookResponse getBookById(String id) {
        Book book = bookRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return bookMapper.toBookResponse(book);
    }

    // Lấy danh sách sách bán chạy nhất
    @Override
    public List<BookSummaryResponse> getBestSellingBooks(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit != null ? limit : 10);
        List<Book> books = bookRepository.findBestSellingBooks(pageable);
        return books.stream()
                .map(bookMapper::toBookSummaryResponse)
                .collect(Collectors.toList());
    }
}
