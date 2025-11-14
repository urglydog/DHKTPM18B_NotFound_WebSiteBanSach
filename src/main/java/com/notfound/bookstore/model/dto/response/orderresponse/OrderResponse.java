package com.notfound.bookstore.model.dto.response.orderresponse;

import com.notfound.bookstore.model.dto.response.addressresponse.AddressResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    UUID id;
    String orderCode;
    LocalDateTime orderDate;
    String status;
    BigDecimal subtotal;
    BigDecimal total;
    String paymentMethod;

    // Thông tin khuyến mãi
    String promotionCode;
    String promotionName;
    Double discountPercent;
    BigDecimal discountAmount;

    List<OrderItemResponse> items;
    AddressResponse shippingAddress;
}
