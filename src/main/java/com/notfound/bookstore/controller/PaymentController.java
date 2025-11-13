package com.notfound.bookstore.controller;

import com.notfound.bookstore.model.dto.request.paymentrequest.MoMoCallbackRequest;
import com.notfound.bookstore.model.dto.request.paymentrequest.PaymentRequest;
import com.notfound.bookstore.model.dto.request.paymentrequest.VNPayCallbackRequest;
import com.notfound.bookstore.model.dto.request.paymentrequest.ZaloPayCallbackRequest;
import com.notfound.bookstore.model.dto.response.ApiResponse;
import com.notfound.bookstore.model.dto.response.paymentresponse.CreatePaymentResponse;
import com.notfound.bookstore.model.dto.response.paymentresponse.PaymentResponse;
import com.notfound.bookstore.model.dto.response.paymentresponse.ZaloPayCallBackResponseDTO;
import com.notfound.bookstore.payment.VNPayService;
import com.notfound.bookstore.service.impl.MoMoServiceImpl;
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
    private final MoMoServiceImpl moMoService;

    /**
     * Tạo payment URL VNPay
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

    /**
     * Tạo đơn hàng thanh toán ZaloPay
     * POST /api/payment/zalopay/create
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
    }

    /**
     * Tạo payment URL MoMo
     * POST /api/payment/momo/create
     */
    @PostMapping("/momo/create")
    public ApiResponse<CreatePaymentResponse> createMoMoPayment(
            @RequestBody @Valid PaymentRequest request
    ) {
        CreatePaymentResponse moMoPaymentUrl = moMoService.createMoMoPayment(request);
        return ApiResponse.<CreatePaymentResponse>builder()
                .code(200)
                .result(moMoPaymentUrl)
                .message("Đã tạo đường dẫn thanh toán MoMo thành công")
                .build();
    }

    /**
     * MoMo callback endpoint
     * POST /api/payment/momo/callback
     */
    @PostMapping("/momo/callback")
    public ApiResponse<PaymentResponse> handleMoMoCallback(
            @RequestBody MoMoCallbackRequest callback
    ) {
        PaymentResponse paymentResponse = moMoService.handleMoMoCallback(callback);
        return ApiResponse.<PaymentResponse>builder()
                .code(200)
                .result(paymentResponse)
                .message("Xử lý callback MoMo thành công")
                .build();
    }

    /**
     * MoMo return URL (user redirect back)
     * GET /api/payment/momo/return
     */
    @GetMapping("/momo/return")
    public ApiResponse<PaymentResponse> handleMoMoReturn() {
        return ApiResponse.<PaymentResponse>builder()
                .code(200)
                .message("Thanh toán MoMo thành công")
                .build();
    }
}
