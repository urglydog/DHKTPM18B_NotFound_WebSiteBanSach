package com.notfound.bookstore.service.impl;

import com.notfound.bookstore.exception.AppException;
import com.notfound.bookstore.exception.ErrorCode;
import com.notfound.bookstore.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {
    JavaMailSender mailSender;
    @Override
    public void sendOtp(String to, String otp) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Mã OTP từ Bookstore");

            // Nội dung HTML
            String htmlContent = """
                    <html>
                        <body style="font-family: Arial, sans-serif; background-color: #f6f6f6; padding: 20px;">
                            <div style="max-width: 600px; margin: auto; background: white; padding: 20px; border-radius: 8px;">
                                <h2 style="color: #4CAF50;">Mã xác thực OTP của bạn</h2>
                                <p>Xin chào,</p>
                                <p>Mã OTP của bạn là:</p>
                                <div style="font-size: 22px; font-weight: bold; color: #E91E63; margin: 10px 0;">
                                    %s
                                </div>
                                <p>Mã này có hiệu lực trong <b>5 phút</b>.</p>
                                <p>Trân trọng,<br>Đội ngũ Bookstore</p>
                            </div>
                        </body>
                    </html>
                    """.formatted(otp);

            helper.setText(htmlContent, true); // true => gửi dạng HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email OTP", e);
        }
    }

    @Override
    public void sendHtmlEmail(String email, String token) {
        try {
            String verifyLink = "http://localhost:8080/api/auth/confirm-email?token=" + token;

            String subject = "Xác thực tài khoản Bookstore";
            String content = """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px 0;">
                    <tr>
                        <td align="center">
                            <!-- Container chính -->
                            <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); overflow: hidden;">
                                <tr>
                                    <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 600;">
                                            📚 Bookstore
                                        </h1>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 40px 30px;">
                                        <h2 style="color: #333333; font-size: 24px; margin: 0 0 20px 0; font-weight: 600;">
                                            Xin chào! 👋
                                        </h2>
                                        <p style="color: #666666; font-size: 16px; line-height: 1.6; margin: 0 0 15px 0;">
                                            Cảm ơn bạn đã đăng ký tài khoản tại <strong style="color: #667eea;">Bookstore</strong>.
                                        </p>
                                        <p style="color: #666666; font-size: 16px; line-height: 1.6; margin: 0 0 25px 0;">
                                            Vui lòng nhấn vào nút bên dưới để xác thực địa chỉ email của bạn:
                                        </p>
                                        <table width="100%%" cellpadding="0" cellspacing="0">
                                            <tr>
                                                <td align="center" style="padding: 10px 0 30px 0;">
                                                    <a href="%s" 
                                                       style="display: inline-block; 
                                                              padding: 15px 40px; 
                                                              background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                                                              color: #ffffff; 
                                                              text-decoration: none; 
                                                              border-radius: 50px; 
                                                              font-size: 16px; 
                                                              font-weight: 600;
                                                              box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
                                                              transition: all 0.3s ease;">
                                                        ✉️ Xác thực Email
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>
                                        <div style="background-color: #fff3cd; 
                                                    border-left: 4px solid #ffc107; 
                                                    padding: 15px; 
                                                    border-radius: 8px; 
                                                    margin: 0 0 20px 0;">
                                            <p style="margin: 0; color: #856404; font-size: 14px; line-height: 1.6;">
                                                ⚠️ <strong>Lưu ý:</strong> Link xác thực này chỉ có hiệu lực trong <strong>24 giờ</strong>.
                                            </p>
                                        </div>
                                        <p style="color: #999999; font-size: 14px; line-height: 1.6; margin: 0;">
                                            Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.
                                        </p>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #eeeeee;">
                                        <p style="margin: 0 0 10px 0; color: #666666; font-size: 14px;">
                                            Trân trọng,<br>
                                            <strong style="color: #667eea;">Đội ngũ Bookstore</strong>
                                        </p>
                                        
                                        <p style="margin: 15px 0 0 0; color: #999999; font-size: 12px;">
                                            © 2025 Bookstore. All rights reserved.
                                        </p>
                                    </td>
                                </tr>
                                
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """.formatted(verifyLink);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
