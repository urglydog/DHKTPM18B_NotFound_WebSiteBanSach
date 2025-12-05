package com.notfound.bookstore.model.dto.response.userresponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    UUID id;
    String username;
    String email;
    String fullName;
    String phoneNumber;
    String role;
    Boolean emailVerified;
    String avatarUrl;
    LocalDate dateOfBirth;
    String gender;
    LocalDateTime lastLogin;
    String membershipTier;
    Integer points;
    String providerId;
}