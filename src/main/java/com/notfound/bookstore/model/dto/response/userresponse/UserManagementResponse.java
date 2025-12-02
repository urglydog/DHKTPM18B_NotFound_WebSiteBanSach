package com.notfound.bookstore.model.dto.response.userresponse;

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
    String fullName;
    String phoneNumber;
    String avatarUrl;
    String role;
    String status;
    LocalDateTime createdAt;
    Integer totalOrders;
    BigDecimal totalSpent;
}
