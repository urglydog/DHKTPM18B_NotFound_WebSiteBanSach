package com.notfound.bookstore.controller;

import com.notfound.bookstore.model.dto.response.ApiResponse;
import com.notfound.bookstore.model.dto.response.userresponse.UserResponse;
import com.notfound.bookstore.model.mapper.UserMapper;
import com.notfound.bookstore.security.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller xử lý các chức năng liên quan đến profile của user hiện tại
 * Cho phép user xem và quản lý thông tin cá nhân của chính mình
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileController {

    SecurityUtils securityUtils;
    UserMapper userMapper;

    /**
     * Lấy thông tin user hiện tại đang đăng nhập
     * GET /api/user/me
     * 
     * Lưu ý: URL đúng là /api/user/me (không có "s" sau "user")
     * 
     * @return Thông tin user hiện tại
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<UserResponse> getCurrentUser() {
        log.info("GET /api/user/me - Getting current user information");
        var currentUser = securityUtils.getCurrentUser();
        log.debug("Current user: {}", currentUser.getUsername());
        UserResponse userResponse = userMapper.toUserResponse(currentUser);
        
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Lấy thông tin user thành công")
                .result(userResponse)
                .build();
    }
}


