package com.notfound.bookstore.controller;

import com.notfound.bookstore.exception.ErrorCode;
import com.notfound.bookstore.model.dto.request.userrequest.EmailRequest;
import com.notfound.bookstore.model.dto.request.userrequest.LoginRequest;
import com.notfound.bookstore.model.dto.request.userrequest.RegisterRequest;
import com.notfound.bookstore.model.dto.request.userrequest.ResetPasswordRequest;
import com.notfound.bookstore.model.dto.response.ApiResponse;
import com.notfound.bookstore.model.dto.response.userresponse.AuthResponse;
import com.notfound.bookstore.service.AuthService;
import com.notfound.bookstore.service.EmailService;
import com.notfound.bookstore.service.RedisService;
import com.notfound.bookstore.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;
    RedisService redisService;
    UserService userService;
    EmailService emailService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ApiResponse.<AuthResponse>builder()
                .code(1000)
                .message("Đăng ký thành công")
                .result(authResponse)
                .build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ApiResponse.<AuthResponse>builder()
                .code(1000)
                .message("Đăng nhập thành công")
                .result(authResponse)
                .build();
    }

    @PostMapping("/send-otp")
    public ApiResponse<Void> sendOtp(@RequestBody EmailRequest request) throws MessagingException {
        String email = request.getEmail();

        if (redisService.existsByEmail(email)) {
            return ApiResponse.<Void>builder()
                    .code(ErrorCode.TOO_MANY_REQUESTS.getCode())
                    .message(ErrorCode.TOO_MANY_REQUESTS.getMessage())
                    .build();
        }

        if (email == null || !userService.existsByEmail(email)) {
            return ApiResponse.<Void>builder()
                    .code(ErrorCode.MAIL_NOT_EXISTED.getCode())
                    .message(ErrorCode.MAIL_NOT_EXISTED.getMessage())
                    .build();
        }

        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        redisService.saveOtp(email, otp, 5);
        emailService.sendOtp(email, otp);

        return ApiResponse.<Void>builder()
                .code(200)
                .message("OTP đã gửi về email")
                .build();
    }

    @PostMapping("/verify-otp")
    public ApiResponse<Void> verifyOtp(@RequestBody ResetPasswordRequest request) {
        String email = request.getEmail();
        String otp = request.getOtp();
        String password_new = request.getPasswordNew();
        String confirm_password = request.getConfirmPassword();

        if (email == null || otp == null || password_new == null || confirm_password == null) {
            return ApiResponse.<Void>builder()
                    .code(4000)
                    .message("Thiếu thông tin")
                    .build();
        }

        if (!password_new.equals(confirm_password)) {
            return ApiResponse.<Void>builder()
                    .code(4000)
                    .message("Mật khẩu xác nhận không giống nhau")
                    .build();
        }

        int attempts = redisService.getOtpAttempts(email);
        if (attempts >= 5) {
            return ApiResponse.<Void>builder()
                    .code(4003)
                    .message("Bạn đã nhập sai OTP quá 5 lần. Vui lòng yêu cầu OTP mới.")
                    .build();
        }

        String saved = redisService.getOtp(email);
        if (saved != null && saved.equals(otp)) {
            userService.resetPassword(email, password_new);
            redisService.deleteOtp(email);
            return ApiResponse.<Void>builder()
                    .code(200)
                    .message("Đổi mật khẩu thành công")
                    .build();
        }
        redisService.incrementOtpAttempts(email);
        return ApiResponse.<Void>builder()
                .code(400)
                .message("OTP sai hoặc đã hết hạn")
                .build();

    }

    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@RequestBody EmailRequest request) {
        String email = request.getEmail();

        if (email == null || !userService.existsByEmail(email)) {
            return ApiResponse.<Void>builder()
                    .code(400)
                    .message("Email không tồn tại")
                    .build();
        }

        String token = authService.generateEmailVerificationToken(email);
        emailService.sendHtmlEmail(email, token);

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Đã gửi email xác thực. Vui lòng kiểm tra hộp thư.")
                .build();
    }

    @GetMapping("/confirm-email")
    public ApiResponse<Void> confirmEmail(@RequestParam("token") String token) {

        if (token == null) {
            return ApiResponse.<Void>builder()
                    .code(4000)
                    .message("Xác thực không thành công")
                    .build();
        }

        String email = authService.validateEmailVerificationToken(token);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Xác thực email thành công " + email)
                .build();
    }
}