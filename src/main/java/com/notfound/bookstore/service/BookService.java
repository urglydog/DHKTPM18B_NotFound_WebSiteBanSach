package com.notfound.bookstore.service;

import com.notfound.bookstore.model.dto.request.bookrequest.BookFilterRequest;
import com.notfound.bookstore.model.dto.request.bookrequest.BookSearchRequest;
import com.notfound.bookstore.model.dto.request.bookrequest.BookSortRequest;
import com.notfound.bookstore.model.dto.response.bookresponse.BookResponse;
import com.notfound.bookstore.model.dto.response.bookresponse.BookSummaryResponse;
import com.notfound.bookstore.model.dto.response.bookresponse.PageResponse;
import com.notfound.bookstore.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface BookService {

    //Tìm kiếm sách theo từ khóa (tên sách, tác giả, hoặc thể loại)
    PageResponse<BookSummaryResponse> searchBooks(BookSearchRequest request);

    //Lọc sách theo các tiêu chí: giá, đánh giá trung bình và ngày phát hành
    PageResponse<BookSummaryResponse> findByFilters(BookFilterRequest request);

    //Lấy danh sách sách được sắp xếp theo loại sắp xếp được chỉ định
    PageResponse<BookSummaryResponse> getSortedBooks(BookSortRequest request);

    //Lấy thông tin chi tiết của một cuốn sách dựa trên ID
    BookResponse getBookById(String id);
}
