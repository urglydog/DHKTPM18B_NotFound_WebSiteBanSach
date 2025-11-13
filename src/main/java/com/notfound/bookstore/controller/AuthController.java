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

/**
 * Controller xử lý các chức năng xác thực và phân quyền
 * Bao gồm đăng ký, đăng nhập, quên mật khẩu và OAuth với Google
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;
    RedisService redisService;
    UserService userService;
    EmailService emailService;

    /**
     * Đăng ký tài khoản mới
     *
     * @param request Thông tin đăng ký bao gồm username, email, mật khẩu và các thông tin cá nhân
     * @return Thông tin xác thực sau khi đăng ký thành công (token và thông tin user)
     */
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ApiResponse.<AuthResponse>builder()
                .code(1000)
                .message("Đăng ký thành công")
                .result(authResponse)
                .build();
    }

    /**
     * Đăng nhập vào hệ thống
     *
     * @param request Thông tin đăng nhập (email và mật khẩu)
     * @return Thông tin xác thực sau khi đăng nhập thành công (token và thông tin user)
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ApiResponse.<AuthResponse>builder()
                .code(1000)
                .message("Đăng nhập thành công!")
                .result(authResponse)
                .build();
    }

    /**
     * Gửi mã OTP để đặt lại mật khẩu
     * OTP có hiệu lực trong 5 phút và được lưu trong Redis
     *
     * @param request Email cần gửi OTP
     * @return Kết quả gửi OTP
     * @throws MessagingException Nếu có lỗi khi gửi email
     */
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

    /**
     * Xác thực OTP và đặt lại mật khẩu mới
     * Người dùng chỉ được nhập sai OTP tối đa 5 lần
     *
     * @param request Thông tin bao gồm email, OTP, mật khẩu mới và xác nhận mật khẩu
     * @return Kết quả đặt lại mật khẩu
     */
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

    /**
     * Gửi email xác thực tài khoản
     * Email chứa link xác thực để kích hoạt tài khoản
     *
     * @param request Email cần xác thực
     * @return Kết quả gửi email xác thực
     */
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

    /**
     * Xác nhận email thông qua token
     * Endpoint này được gọi khi người dùng click vào link trong email xác thực
     *
     * @param token Token xác thực từ email
     * @return Kết quả xác thực email
     */
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

    /**
     * Xử lý callback từ Google OAuth
     * Đăng nhập hoặc tạo tài khoản mới thông qua Google
     *
     * @param code Authorization code từ Google
     * @return Thông tin xác thực sau khi đăng nhập Google thành công
     */
    @GetMapping("/google/callback")
    public ApiResponse<AuthResponse> googleCallback(@RequestParam("code") String code) {
        if (code == null || code.isEmpty()) {
            return ApiResponse.<AuthResponse>builder()
                    .code(4000)
                    .message("Authorization code không hợp lệ")
                    .build();
        }

        try {
            AuthResponse authResponse = authService.handleGoogleOAuthCallback(code);
            return ApiResponse.<AuthResponse>builder()
                    .code(1000)
                    .message("Đăng nhập Google thành công!")
                    .result(authResponse)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<AuthResponse>builder()
                    .code(4000)
                    .message("Đăng nhập Google thất bại: " + e.getMessage())
                    .build();
        }
    }
}