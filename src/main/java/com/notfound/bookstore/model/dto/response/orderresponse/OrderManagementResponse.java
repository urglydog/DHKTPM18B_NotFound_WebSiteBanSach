package com.notfound.bookstore.model.dto.response.orderresponse;

import com.notfound.bookstore.model.dto.response.orderresponse.OrderResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderManagementResponse extends OrderResponse {
    String customerName;
    String customerEmail;
    String customerPhone;
}