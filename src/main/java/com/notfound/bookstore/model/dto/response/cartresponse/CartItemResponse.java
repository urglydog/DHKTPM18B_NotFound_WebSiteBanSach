package com.notfound.bookstore.model.dto.response.cartresponse;

import com.notfound.bookstore.model.dto.response.bookresponse.BookResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    UUID id;
    BookResponse book;
    Integer quantity;
    BigDecimal price;
    BigDecimal subtotal;
}