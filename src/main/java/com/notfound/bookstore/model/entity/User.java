package com.notfound.bookstore.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.notfound.bookstore.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(exclude = {"addresses", "reviews", "cart", "orders", "wishlist", "newsArticles"})
public class User {

    @Id
    @UuidGenerator
    UUID id;

    @Column(unique = true, nullable = false)
    String username;

    @Column(nullable = false)
    String password;

    @Column(unique = true, nullable = false)
    String email;

    @Column
    String fullName;

    @Column
    String phoneNumber;

    @Column
    String gender;

    @Column
    String avatar_url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Address> addresses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    List<Review> reviews;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Cart cart;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Order> orders;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Wishlist wishlist;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<News> newsArticles;

    public User(String username, String password, String email, Role role, String avatar_url) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.avatar_url = avatar_url;
    }
}
