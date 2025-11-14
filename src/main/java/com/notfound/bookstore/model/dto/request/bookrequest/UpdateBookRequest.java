package com.notfound.bookstore.model.dto.request.bookrequest;

import com.notfound.bookstore.model.entity.Book;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateBookRequest {
    String title;

    String isbn;

    @DecimalMin(value = "0.0", message = "Price must be positive")
    Double price;

    @DecimalMin(value = "0.0", message = "Discount price must be positive")
    Double discountPrice;

    @Min(value = 0, message = "Stock cannot be negative")
    Integer stockQuantity;

    LocalDate publishDate;

    String description;

    Book.Status status;

    List<UUID> authorIds;

    List<UUID> categoryIds;
}
