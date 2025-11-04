package com.notfound.bookstore.service;

import com.notfound.bookstore.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface BookService {
    //Tìm kiếm sách theo từ khóa (tên sách, tác giả, hoặc thể loại)
    Page<Book> searchBooks(String keyword, Pageable pageable);

    //Lọc sách theo các tiêu chí: giá, đánh giá trung bình và ngày phát hành
    Page<Book> findByFilters(Double minPrice, Double maxPrice, Double minRating,
                             LocalDate publishedAfter, Pageable pageable);

    //Lấy danh sách sách được sắp xếp theo loại sắp xếp được chỉ định
    Page<Book> getSortedBooks(String sortType, int page, int size);
}
