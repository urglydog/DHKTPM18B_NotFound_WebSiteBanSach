package com.notfound.bookstore.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "news_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsImage extends BaseImage {

    @ManyToOne
    @JoinColumn(name = "news_id")
    News news;
}
