package com.notfound.bookstore.model.dto.response.paymentresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String code;
    private String message;
    private String paymentUrl;
}