package com.notfound.bookstore.model.dto.request.cartrequest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddToCartRequest {
    @NotNull(message = "Book ID is required")
    UUID bookId;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity;
}