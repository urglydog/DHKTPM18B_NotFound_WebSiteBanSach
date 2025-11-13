package com.notfound.bookstore.model.dto.response.bookresponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookSummaryResponse {
    UUID id;
    String title;
    Double price;
    Double discountPrice;
    String mainImageUrl;
    Double averageRating;
    Integer reviewCount;
}
