package com.notfound.bookstore.config;

import com.notfound.bookstore.util.VNPayUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayConfig {
    @Value("${payment.vnPay.url}")
    String vnp_PayUrl;
    @Value("${payment.vnPay.returnUrl}")
    String vnp_ReturnUrl;
    @Value("${payment.vnPay.tmnCode}")
    String vnp_TmnCode ;
    @Value("${payment.vnPay.secretKey}")
    String secretKey;
    @Value("${payment.vnPay.version}")
    String vnp_Version;
    @Value("${payment.vnPay.command}")
    String vnp_Command;
    @Value("${payment.vnPay.orderType}")
    String orderType;

    /**
     * Tạo Map chứa các tham số cho thanh toán VNPAY
     */
    public Map<String, String> getVNPayConfig(long amount) {
        Map<String, String> vnpParamsMap = new HashMap<>();
        vnpParamsMap.put("vnp_Version", vnp_Version);
        vnpParamsMap.put("vnp_Command", vnp_Command);
        vnpParamsMap.put("vnp_TmnCode", vnp_TmnCode);
        vnpParamsMap.put("vnp_CurrCode", "VND");

        vnpParamsMap.put("vnp_TxnRef",  VNPayUtil.getRandomNumber(8));
        vnpParamsMap.put("vnp_OrderInfo", "Thanh toán số tiền: " + amount + " VNĐ");
        vnpParamsMap.put("vnp_OrderType", orderType);
        vnpParamsMap.put("vnp_Locale", "vn");
        vnpParamsMap.put("vnp_ReturnUrl", vnp_ReturnUrl);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        Instant now = Instant.now();
        vnpParamsMap.put("vnp_CreateDate", formatter.format(now));
        vnpParamsMap.put("vnp_ExpireDate", formatter.format(now.plusSeconds(5 * 60))); // Thêm 5 phút

        return vnpParamsMap;
    }
}
