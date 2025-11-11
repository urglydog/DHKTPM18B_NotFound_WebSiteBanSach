package com.notfound.bookstore.model.dto.request.bookrequest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookFilterRequest {
    Double minPrice;
    Double maxPrice;
    Double minRating;
    LocalDate publishedAfter;

    @Min(0)
    @Builder.Default
    Integer page = 0;

    @Min(1)
    @Max(100)
    @Builder.Default
    Integer size = 10;
}
