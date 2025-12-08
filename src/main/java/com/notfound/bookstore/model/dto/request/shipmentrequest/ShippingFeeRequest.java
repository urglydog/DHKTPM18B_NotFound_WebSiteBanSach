package com.notfound.bookstore.model.dto.request.shipmentrequest;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippingFeeRequest {

    Integer toDistrictId;
    String toWardCode;

    @Builder.Default
    Integer length = 20;

    @Builder.Default
    Integer width = 15;

    @Builder.Default
    Integer height = 2;

    @Builder.Default
    Integer weight = 300;

    @Builder.Default
    Integer insuranceValue = 0;
}