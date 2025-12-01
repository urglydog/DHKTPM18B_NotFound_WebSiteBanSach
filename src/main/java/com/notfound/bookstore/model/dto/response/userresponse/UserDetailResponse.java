package com.notfound.bookstore.model.dto.response.userresponse;

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
public class UserDetailResponse {
    
    UUID id;
    String username;
    String email;
    String fullName;
    String phoneNumber;
    String gender;
    String avatarUrl;
    String role;
    String status;
    
    // Thống kê
    Integer totalOrders;
    BigDecimal totalSpent;
    Integer totalReviews;
    LocalDateTime lastOrderDate;
    
    // Địa chỉ
    List<AddressResponse> addresses;
    
    // Thời gian
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime lastLoginAt;
}
