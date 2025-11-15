package com.notfound.bookstore.service;

import com.notfound.bookstore.model.dto.request.newsrequest.CreateNewsRequest;
import com.notfound.bookstore.model.dto.response.newsresponse.NewsResponse;
import com.notfound.bookstore.model.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * @Dự án: DHKTPM18B_NotFound_WebSiteBanSach
 * @Interface: NewsService
 * @Tạo vào ngày: 11/15/2025
 * @Tác giả: Nguyen Huu Sang
 */

public interface NewsService {

        /**
         * Tạo news mới
         * @param request - dữ liệu news (title, content, images...)
         * @param authorId - ID của user tạo bài viết
         * @return NewsResponse
         */
        NewsResponse createNews(CreateNewsRequest request, UUID authorId);

        /**
         * Cập nhật news
         * @param newsId - ID của news cần update
         * @param request - dữ liệu mới
         * @return NewsResponse
         */
        NewsResponse updateNews(UUID newsId, CreateNewsRequest request);

        /**
         * Lấy chi tiết news theo ID
         * @param newsId - ID của news
         * @return NewsResponse
         */
        NewsResponse getNewsById(UUID newsId);

        /**
         * Lấy tất cả news (có phân trang, sắp xếp theo ngày tạo mới nhất)
         * @param pageable - thông tin phân trang
         * @return Page<NewsResponse>
         */
        Page<NewsResponse> getAllNews(Pageable pageable);

        /**
         * Lấy news đã publish (cho user xem)
         * @param pageable - thông tin phân trang
         * @return Page<NewsResponse>
         */
        Page<NewsResponse> getPublishedNews(Pageable pageable);

        /**
         * Lấy news theo author
         * @param authorId - ID của tác giả
         * @param pageable - thông tin phân trang
         * @return Page<NewsResponse>
         */
        Page<NewsResponse> getNewsByAuthor(UUID authorId, Pageable pageable);

        /**
         * Tìm kiếm news theo title
         * @param title - từ khóa tìm kiếm
         * @param pageable - thông tin phân trang
         * @return Page<NewsResponse>
         */
        Page<NewsResponse> searchNewsByTitle(String title, Pageable pageable);

        /**
         * Xóa news
         * @param newsId - ID của news cần xóa
         */
        void deleteNews(UUID newsId);

        /**
         * Publish news (chuyển từ DRAFT sang PUBLISHED)
         * @param newsId - ID của news
         * @return NewsResponse
         */
        NewsResponse publishNews(UUID newsId);

        /**
         * Archive news (chuyển sang ARCHIVED)
         * @param newsId - ID của news
         * @return NewsResponse
         */
        NewsResponse archiveNews(UUID newsId);

        /**
         * Đếm số lượng news theo status
         * @param status - DRAFT, PUBLISHED, hoặc ARCHIVED
         * @return long
         */
        long countByStatus(News.Status status);

}

