package com.notfound.bookstore.model.dto.request.orderRequest;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckoutRequest {
    UUID addressId;

    @NotBlank
    String paymentMethod;

    String note;
    String discountCode;
}