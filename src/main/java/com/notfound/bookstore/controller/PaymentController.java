package com.notfound.bookstore.controller;

import com.notfound.bookstore.model.dto.request.paymentrequest.PaymentRequest;
import com.notfound.bookstore.model.dto.request.paymentrequest.VNPayCallbackRequest;
import com.notfound.bookstore.model.dto.request.paymentrequest.ZaloPayCallbackRequest;
import com.notfound.bookstore.model.dto.response.ApiResponse;
import com.notfound.bookstore.model.dto.response.paymentresponse.CreatePaymentResponse;
import com.notfound.bookstore.model.dto.response.paymentresponse.PaymentResponse;
import com.notfound.bookstore.model.dto.response.paymentresponse.ZaloPayCallBackResponseDTO;
import com.notfound.bookstore.payment.VNPayService;
import com.notfound.bookstore.service.impl.VNPayServiceImpl;
import com.notfound.bookstore.service.impl.ZaloPayServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayServiceImpl vnPayService;
    private final ZaloPayServiceImpl zaloPayService;

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

    /**
     * Tạo đơn hàng thanh toán ZaloPay
     *
     * @param request PaymentRequest chứa orderId và amount
     * @return CreatePaymentResponse chứa order_url để redirect user
     */
    @PostMapping("/zalopay/create")
    public ApiResponse<CreatePaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request
    ) {
        CreatePaymentResponse response = zaloPayService.createOrderTransaction(request);
        return ApiResponse.<CreatePaymentResponse>builder()
                        .code(HttpStatus.OK.value())
                        .message("Payment order created successfully")
                        .result(response)
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

    @PostMapping("/zalopay/callback")
    public ZaloPayCallBackResponseDTO callbackZaloPay(@RequestBody ZaloPayCallbackRequest body) {
        return zaloPayService.processCallback(body);
    }

    @GetMapping("/zalopay/return")
    public ApiResponse<PaymentResponse> handleZaloPayReturn(){
        return ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Thành công")
                .build();
    };
}
