package com.notfound.bookstore.controller;

import com.notfound.bookstore.model.dto.request.paymentrequest.PaymentRequest;
import com.notfound.bookstore.model.dto.request.paymentrequest.VNPayCallbackRequest;
import com.notfound.bookstore.model.dto.response.ApiResponse;
import com.notfound.bookstore.model.dto.response.paymentresponse.CreatePaymentResponse;
import com.notfound.bookstore.model.dto.response.paymentresponse.PaymentResponse;
import com.notfound.bookstore.payment.VNPayService;
import com.notfound.bookstore.service.impl.VNPayServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    @PostMapping("/vnpay/create")
    public ApiResponse<CreatePaymentResponse> createVNPayPayment(
            @RequestBody @Valid PaymentRequest request,
            HttpServletRequest httpServletRequest
    ) {
        CreatePaymentResponse vnPayPaymentUrl = vnPayService.createVNPayPaymentUrl(request, httpServletRequest);
        return ApiResponse.<CreatePaymentResponse>builder()
                .code(200)
                .result(vnPayPaymentUrl)
                .message("Đã tạo đường dẫn thanh toán thành công")
                .build();
    }

    @GetMapping("/vnpay/callback")
    public ApiResponse<PaymentResponse> handleVNPayReturn(VNPayCallbackRequest vnpParams) {
        PaymentResponse paymentResponse = vnPayService.handleVNPayReturn(vnpParams);
        return ApiResponse.<PaymentResponse>builder()
                .code(200)
                .result(paymentResponse)
                .message("Thanh toán thành công")
                .build();
    }

}
