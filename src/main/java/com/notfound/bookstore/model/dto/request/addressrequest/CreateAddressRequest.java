package com.notfound.bookstore.model.dto.request.addressrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAddressRequest {
    @NotBlank(message = "Recipient name is required")
    String recipientName;

    @NotBlank
    @Pattern(regexp = "^(\\+84|0)[0-9]{9}$")
    String phoneNumber;

    @NotBlank
    String street;

    @NotBlank
    String city;

    @NotBlank
    String district;

    Boolean isDefault;
}