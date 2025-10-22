package com.notfound.bookstore.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(exclude = {"authors", "categories", "reviews", "orderItems", "cartItems", "wishlists"})
public class Book {

    @Id
    @UuidGenerator
    UUID id;

    @Column(nullable = false)
    String title;

    @Column(unique = true)
    String isbn;

    @Column(nullable = false)
    Double price;

    @Column(name = "discount_price")
    Double discountPrice;

    @Column(name = "stock_quantity", nullable = false)
    Integer stockQuantity;

    @Column(name = "publish_date")
    LocalDate publishDate;

    @Column(columnDefinition = "TEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status;

    @CreationTimestamp
    @Column(name = "created_at")
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_authors",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    List<Author> authors;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_categories",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    List<Category> categories;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Review> reviews;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<OrderItem> orderItems;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<CartItem> cartItems;

    @ManyToMany(mappedBy = "books", fetch = FetchType.LAZY)
    List<Wishlist> wishlists;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY,  orphanRemoval = true)
    List<BookImage> images;

    public enum Status {
        AVAILABLE, OUT_OF_STOCK, DISCONTINUED
    }

    public Book(String title, String isbn, Double price, Integer stockQuantity, LocalDate publishDate,String description, Status status) {
        this.title = title;
        this.isbn = isbn;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.publishDate = publishDate;
        this.description = description;
        this.status = status;
    }
}
