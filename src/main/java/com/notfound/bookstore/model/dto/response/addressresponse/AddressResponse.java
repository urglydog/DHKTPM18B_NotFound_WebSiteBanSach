package com.notfound.bookstore.model.dto.response.addressresponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressResponse {
    UUID id;
    String recipientName;
    String phoneNumber;
    String street;
    String city;
    String district;
    Boolean isDefault;
}