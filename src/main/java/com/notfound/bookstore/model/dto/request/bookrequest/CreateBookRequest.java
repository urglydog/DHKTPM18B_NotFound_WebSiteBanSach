package com.notfound.bookstore.model.dto.request.bookrequest;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBookRequest {
    @NotBlank(message = "Title is required")
    String title;

    @NotBlank(message = "Author is required")
    String author;

    @NotBlank(message = "Description is required")
    String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be positive")
    BigDecimal price;

    @NotNull
    @Min(value = 0, message = "Stock cannot be negative")
    Integer stock;

    String isbn;
    String categoryId;
    String publisher;
    LocalDate publishDate;
}