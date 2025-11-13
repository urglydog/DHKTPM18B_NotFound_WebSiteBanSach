package com.notfound.bookstore.model.dto.response.categoryresponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    UUID id;
    String name;
    String description;
    UUID parentCategoryId;
    String parentCategoryName;
}

