package com.notfound.bookstore.model.dto.request.authorrequest;

import lombok.*;
import lombok.experimental.FieldDefaults;


/**
 * @Dự án: DHKTPM18B_NotFound_WebSiteBanSach
 * @Class: AuthorSearchRequest
 * @Tạo vào ngày: 11/14/2025
 * @Tác giả: Nguyen Huu Sang
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorSearchRequest {
    String name;
    int page = 0;
    int size = 10;
}