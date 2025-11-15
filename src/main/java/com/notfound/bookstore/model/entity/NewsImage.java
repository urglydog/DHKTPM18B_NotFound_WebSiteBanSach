package com.notfound.bookstore.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "news_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NewsImage extends BaseImage {

    @ManyToOne
    @JoinColumn(name = "news_id")
    News news;

    //  THÊM: Alt text cho SEO
    String alt;

    //  THÊM: Chú thích hình ảnh
    @Column(columnDefinition = "TEXT")
    String caption;
}
