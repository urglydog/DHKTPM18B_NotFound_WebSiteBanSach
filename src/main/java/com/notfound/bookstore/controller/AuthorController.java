package com.notfound.bookstore.controller;

import com.notfound.bookstore.model.dto.request.authorrequest.*;
import com.notfound.bookstore.model.dto.response.ApiResponse;
import com.notfound.bookstore.model.dto.response.authorresponse.AuthorResponse;
import com.notfound.bookstore.model.dto.response.authorresponse.AuthorSummaryResponse;
import com.notfound.bookstore.model.dto.response.bookresponse.PageResponse;
import com.notfound.bookstore.service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * @Dự án: DHKTPM18B_NotFound_WebSiteBanSach
 * @Class: AuthorController
 * @Tạo vào ngày: 11/13/2025
 * @Tác giả: Nguyen Huu Sang
 */

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    // GetALl
    @GetMapping
    public ApiResponse<PageResponse<AuthorSummaryResponse>> getAllAuthors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortByName,
            @RequestParam(required = false) String sortByBirthYear) {
        AuthorFilterRequest request = new AuthorFilterRequest();
        request.setPage(page);
        request.setSize(size);
        request.setSortByName(sortByName);
        request.setSortByBirthYear(sortByBirthYear);
        return ApiResponse.<PageResponse<AuthorSummaryResponse>>builder()
                .code(1000)
                .message("Lấy danh sách tác giả thành công")
                .result(authorService.filterAuthors(request))
                .build();
    }

    @PostMapping("/search")
    public ResponseEntity<PageResponse<AuthorSummaryResponse>> searchByName(
            @RequestBody AuthorSearchRequest request) {
        return ResponseEntity.ok(authorService.searchByName(request));
    }

    // sắp xếp theo tên: asc, desc
    @PostMapping("/filter")
    public ResponseEntity<PageResponse<AuthorSummaryResponse>> filterAuthors(
            @RequestBody AuthorFilterRequest request) {
        return ResponseEntity.ok(authorService.filterAuthors(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable UUID id) {
        return ResponseEntity.ok(authorService.getAuthorById(id));
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(@Valid @RequestBody AuthorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authorService.createAuthor(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> updateAuthor(
            @PathVariable UUID id,
            @Valid @RequestBody AuthorRequest request) {
        return ResponseEntity.ok(authorService.updateAuthor(id, request));
    }

}