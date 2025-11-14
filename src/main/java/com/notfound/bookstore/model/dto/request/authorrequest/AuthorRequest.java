package com.notfound.bookstore.model.dto.request.authorrequest;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorRequest {

    @NotBlank(message = "Tên tác giả không được để trống")
    String name;

    String biography;

    LocalDate dateOfBirth;

    String nationality;
}