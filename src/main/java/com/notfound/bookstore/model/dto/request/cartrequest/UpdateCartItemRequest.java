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
public class UpdateCartItemRequest {
    @NotNull(message = "Cart item ID is required")
    UUID cartItemId;

    @NotNull
    @Min(value = 0, message = "Quantity cannot be negative")
    Integer quantity; // ✅ Cho phép 0 để xóa item
}