package com.notfound.bookstore.controller;

import com.notfound.bookstore.model.dto.request.paymentrequest.PaymentRequest;
import com.notfound.bookstore.model.dto.response.ApiResponse;
import com.notfound.bookstore.model.dto.response.paymentresponse.PaymentResponse;
import com.notfound.bookstore.payment.VNPayService;
import com.notfound.bookstore.service.impl.VNPayServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayServiceImpl vnPayService;

    /**
     * Tạo payment URL
     * POST /api/payment/vnpay/create
     */
    @GetMapping("/create")
    public ApiResponse<PaymentResponse> pay(@RequestBody PaymentRequest request) {
        return ApiResponse.<PaymentResponse>builder()
                .message("ok")
                .result(vnPayService.createVNPayPayment((HttpServletRequest) request))
                .build();
    }
}
