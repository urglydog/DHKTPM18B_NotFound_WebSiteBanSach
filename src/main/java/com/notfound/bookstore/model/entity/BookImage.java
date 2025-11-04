package com.notfound.bookstore.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookImage extends BaseImage {
    @ManyToOne
    @JoinColumn(name = "book_id")
    Book book;
}
