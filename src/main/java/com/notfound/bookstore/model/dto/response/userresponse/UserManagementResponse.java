package com.notfound.bookstore.model.dto.response.userresponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserManagementResponse {
    private UUID id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private Integer totalOrders;
    private BigDecimal totalSpent;
}
