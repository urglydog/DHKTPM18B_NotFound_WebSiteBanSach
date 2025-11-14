package com.notfound.bookstore.model.dto.request.authorrequest;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @Dự án: DHKTPM18B_NotFound_WebSiteBanSach
 * @Class: AuthorFilterRequest
 * @Tạo vào ngày: 11/14/2025
 * @Tác giả: Nguyen Huu Sang
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorFilterRequest {
    String nationality;
    Integer birthYear; // Năm sinh cụ thể
    String sortByName; // "asc" hoặc "desc"
    String sortByBirthYear; // "asc" hoặc "desc"
    int page = 0;
    int size = 10;
}