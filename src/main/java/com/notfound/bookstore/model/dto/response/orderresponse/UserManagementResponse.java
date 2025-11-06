package com.notfound.bookstore.model.dto.response.orderresponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserManagementResponse {
    UUID id;
    String username;
    String email;
    String role;
    LocalDateTime createdAt;
    Integer totalOrders;
    BigDecimal totalSpent;
}