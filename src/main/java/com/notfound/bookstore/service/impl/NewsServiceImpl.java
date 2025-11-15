package com.notfound.bookstore.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notfound.bookstore.exception.AppException;
import com.notfound.bookstore.exception.ErrorCode;
import com.notfound.bookstore.model.dto.request.newsrequest.CreateNewsRequest;
import com.notfound.bookstore.model.dto.response.newsresponse.NewsImageResponse;
import com.notfound.bookstore.model.dto.response.newsresponse.NewsMetadata;
import com.notfound.bookstore.model.dto.response.newsresponse.NewsResponse;
import com.notfound.bookstore.model.entity.News;
import com.notfound.bookstore.model.entity.NewsImage;
import com.notfound.bookstore.model.entity.User;
import com.notfound.bookstore.repository.NewsRepository;
import com.notfound.bookstore.repository.UserRepository;
import com.notfound.bookstore.service.NewsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Dự án: DHKTPM18B_NotFound_WebSiteBanSach
 * @Class: NewsServiceImpl
 * @Tạo vào ngày: 11/15/2025
 * @Tác giả: Nguyen Huu Sang
 */

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NewsServiceImpl implements NewsService {

    NewsRepository newsRepository;
    UserRepository userRepository;
    ObjectMapper objectMapper;

    @Override
    @Transactional
    public NewsResponse createNews(CreateNewsRequest request, UUID authorId) {
        log.info("Creating news with title: {}", request.getTitle());

        // Tìm user (author)
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Tự động generate metadata từ HTML content
        String metadata = generateMetadata(request.getContent());

        // Tạo News entity
        News news = News.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .metadata(metadata)
                .status(News.Status.DRAFT)
                .author(author)
                .images(new ArrayList<>())
                .build();

        // Thêm images nếu có
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<NewsImage> images = request.getImages().stream()
                    .map(imgReq -> {
                        NewsImage newsImage = NewsImage.builder()
                                .url(imgReq.getUrl())
                                .alt(imgReq.getAlt())
                                .caption(imgReq.getCaption())
                                .news(news)
                                .build();
                        // Set priority (kế thừa từ BaseImage)
                        newsImage.setPriority(imgReq.getPriority() != null ? imgReq.getPriority() : 1);
                        return newsImage;
                    })
                    .collect(Collectors.toList());
            news.getImages().addAll(images);
        }

        News savedNews = newsRepository.save(news);
        log.info("News created successfully with ID: {}", savedNews.getNewsID());

        return mapToResponse(savedNews);
    }

    @Override
    @Transactional
    public NewsResponse updateNews(UUID newsId, CreateNewsRequest request) {
        log.info("Updating news with ID: {}", newsId);

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));

        // Cập nhật thông tin cơ bản
        news.setTitle(request.getTitle());
        news.setContent(request.getContent());

        // Tự động generate lại metadata
        String metadata = generateMetadata(request.getContent());
        news.setMetadata(metadata);

        // Cập nhật images
        if (request.getImages() != null) {
            // Xóa images cũ
            news.getImages().clear();

            // Thêm images mới
            List<NewsImage> newImages = request.getImages().stream()
                    .map(imgReq -> {
                        NewsImage newsImage = NewsImage.builder()
                                .url(imgReq.getUrl())
                                .alt(imgReq.getAlt())
                                .caption(imgReq.getCaption())
                                .news(news)
                                .build();
                        newsImage.setPriority(imgReq.getPriority() != null ? imgReq.getPriority() : 1);
                        return newsImage;
                    })
                    .collect(Collectors.toList());
            news.getImages().addAll(newImages);
        }

        News updatedNews = newsRepository.save(news);
        log.info("News updated successfully");

        return mapToResponse(updatedNews);
    }

    @Override
    public NewsResponse getNewsById(UUID newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));
        return mapToResponse(news);
    }

    @Override
    public Page<NewsResponse> getAllNews(Pageable pageable) {
        return newsRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<NewsResponse> getPublishedNews(Pageable pageable) {
        return newsRepository.findByStatusOrderByCreatedAtDesc(News.Status.PUBLISHED, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<NewsResponse> getNewsByAuthor(UUID authorId, Pageable pageable) {
        return newsRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<NewsResponse> searchNewsByTitle(String title, Pageable pageable) {
        return newsRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(title, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deleteNews(UUID newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));
        newsRepository.delete(news);
        log.info("News deleted successfully with ID: {}", newsId);
    }

    @Override
    @Transactional
    public NewsResponse publishNews(UUID newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));
        news.setStatus(News.Status.PUBLISHED);
        News publishedNews = newsRepository.save(news);
        log.info("News published successfully with ID: {}", newsId);
        return mapToResponse(publishedNews);
    }

    @Override
    @Transactional
    public NewsResponse archiveNews(UUID newsId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new AppException(ErrorCode.NEWS_NOT_FOUND));
        news.setStatus(News.Status.ARCHIVED);
        News archivedNews = newsRepository.save(news);
        log.info("News archived successfully with ID: {}", newsId);
        return mapToResponse(archivedNews);
    }

    @Override
    public long countByStatus(News.Status status) {
        return newsRepository.countByStatus(status);
    }

    /**
     * ✅ QUAN TRỌNG: Tự động generate metadata từ HTML content
     */
    private String generateMetadata(String htmlContent) {
        try {
            Document doc = Jsoup.parse(htmlContent);

            // 1. Trích xuất Table of Contents từ các heading
            List<NewsMetadata.TableOfContentItem> sections = new ArrayList<>();
            Elements headings = doc.select("h2, h3, h4");

            int sectionIndex = 1;
            for (Element heading : headings) {
                String id = heading.attr("id");
                if (id == null || id.isEmpty()) {
                    id = "section-" + sectionIndex++;
                }

                String tagName = heading.tagName();
                int level = Integer.parseInt(tagName.substring(1));

                sections.add(NewsMetadata.TableOfContentItem.builder()
                        .id(id)
                        .title(heading.text())
                        .level(level)
                        .build());
            }

            // 2. Trích xuất các link trong nội dung
            List<NewsMetadata.NewsLink> links = new ArrayList<>();
            Elements linkElements = doc.select("a[href]");

            for (Element link : linkElements) {
                String url = link.attr("href");
                String text = link.text();

                String type = "external";
                if (url.startsWith("/books/")) {
                    type = "book";
                } else if (url.startsWith("/products/")) {
                    type = "product";
                } else if (url.startsWith("/")) {
                    type = "internal";
                }

                links.add(NewsMetadata.NewsLink.builder()
                        .text(text)
                        .url(url)
                        .type(type)
                        .build());
            }

            // 3. Tạo description từ đoạn text đầu tiên
            String description = "";
            Element firstParagraph = doc.selectFirst("p");
            if (firstParagraph != null) {
                description = firstParagraph.text();
                if (description.length() > 200) {
                    description = description.substring(0, 197) + "...";
                }
            }

            // 4. Build metadata object
            NewsMetadata metadata = NewsMetadata.builder()
                    .description(description)
                    .sections(sections)
                    .links(links)
                    .build();

            return objectMapper.writeValueAsString(metadata);

        } catch (JsonProcessingException e) {
            log.error("Error generating metadata", e);
            return "{}";
        }
    }

    /**
     * Map News entity to NewsResponse DTO
     */
    private NewsResponse mapToResponse(News news) {
        NewsMetadata metadata = null;
        if (news.getMetadata() != null && !news.getMetadata().isEmpty()) {
            try {
                metadata = objectMapper.readValue(news.getMetadata(), NewsMetadata.class);
            } catch (JsonProcessingException e) {
                log.error("Error parsing metadata for news ID: {}", news.getNewsID(), e);
            }
        }

        List<NewsImageResponse> imageResponses = news.getImages().stream()
                .map(img -> NewsImageResponse.builder()
                        .id(img.getId())
                        .url(img.getUrl())
                        .alt(img.getAlt())
                        .caption(img.getCaption())
                        .priority(img.getPriority())
                        .build())
                .collect(Collectors.toList());

        return NewsResponse.builder()
                .newsID(news.getNewsID())
                .title(news.getTitle())
                .content(news.getContent())
                .metadata(metadata)
                .status(news.getStatus().name())
                .createdAt(news.getCreatedAt())
                .updatedAt(news.getUpdatedAt())
                .authorName(news.getAuthor().getUsername())
                .authorId(news.getAuthor().getId())
                .images(imageResponses)
                .build();
    }
}