package com.notfound.bookstore.model.dto.response.bookresponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookResponse {
    UUID id;
    String title;
    String author;
    String description;
    BigDecimal price;
    Integer stock;
    String imageUrl;
    Double avgRating;
    Integer totalReviews;
    String categoryName;
}