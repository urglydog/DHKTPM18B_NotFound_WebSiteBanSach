package com.notfound.bookstore.service;

import com.notfound.bookstore.model.entity.Book;
import com.notfound.bookstore.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Service
public class BookService {
    @Autowired
    private BookRepository bookRepository;
    //Tìm kiếm sách theo từ khóa (tên sách, tác giả, hoặc thể loại)
    public Page<Book> searchBooks(String keyword, Pageable pageable) {
        if (!StringUtils.hasText(keyword)) {
            return bookRepository.findAll(pageable);
        }
        String trimmed = keyword.trim();
        return bookRepository.searchBooks(trimmed, pageable);
    }

    //Lọc sách theo các tiêu chí: giá, đánh giá trung bình và ngày phát hành
    public Page<Book> findByFilters(Double minPrice, Double maxPrice, Double minRating,
                                    LocalDate publishedAfter, Pageable pageable) {
        return bookRepository.findByFilters(minPrice, maxPrice, minRating, publishedAfter, pageable);
    }

    //Lấy danh sách sách được sắp xếp theo loại sắp xếp được chỉ định
    public Page<Book> getSortedBooks(String sortType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return switch (sortType) {
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
    }
}
