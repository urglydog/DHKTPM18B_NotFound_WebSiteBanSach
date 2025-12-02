package com.notfound.bookstore.repository;

import com.notfound.bookstore.model.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface NewsRepository extends JpaRepository<News, UUID> {
    List<News> findByAuthorId(UUID authorId);
    List<News> findByOrderByCreatedAtDesc();
    // Tin tức mới nhất
    Page<News> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Tin tức theo tác giả
    Page<News> findByAuthorIdOrderByCreatedAtDesc(UUID authorId, Pageable pageable);

    // Tìm kiếm tin tức
    Page<News> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title, Pageable pageable);

    // THÊM MỚI - Lấy news theo status (DRAFT, PUBLISHED, ARCHIVED)
    Page<News> findByStatusOrderByCreatedAtDesc(News.Status status, Pageable pageable);

    // THÊM MỚI - Tìm theo status và author
    Page<News> findByStatusAndAuthorIdOrderByCreatedAtDesc(News.Status status, UUID authorId, Pageable pageable);

    // THÊM MỚI - Đếm số lượng news theo status
    long countByStatus(News.Status status);
}
