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

/**
 * Controller xử lý các chức năng thanh toán
 * Tích hợp với các cổng thanh toán: VNPay và ZaloPay
 * Bao gồm tạo đơn thanh toán và xử lý callback từ cổng thanh toán
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayServiceImpl vnPayService;
    private final ZaloPayServiceImpl zaloPayService;
    private final MoMoServiceImpl moMoService;

    /**
     * Tạo URL thanh toán VNPay
     * Người dùng sẽ được chuyển hướng đến trang thanh toán của VNPay
     *
     * @param request Thông tin thanh toán bao gồm orderId và amount
     * @param httpServletRequest Request từ client để lấy thông tin IP và domain
     * @return URL thanh toán VNPay để redirect người dùng
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
     * Khởi tạo giao dịch và trả về URL để redirect người dùng đến trang thanh toán ZaloPay
     *
     * @param request Thông tin thanh toán bao gồm orderId và amount
     * @return URL thanh toán ZaloPay (order_url) để redirect người dùng
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

    /**
     * Xử lý callback từ VNPay sau khi thanh toán
     * Endpoint này được VNPay gọi sau khi người dùng hoàn tất thanh toán
     * Xác thực chữ ký và cập nhật trạng thái đơn hàng
     *
     * @param vnpParams Các tham số callback từ VNPay bao gồm vnp_ResponseCode, vnp_TransactionNo, vnp_SecureHash
     * @return Kết quả xử lý thanh toán bao gồm trạng thái và thông tin giao dịch
     */
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
     * Xử lý callback từ ZaloPay sau khi thanh toán
     * Endpoint này được ZaloPay server gọi để thông báo kết quả thanh toán
     * Xác thực MAC và cập nhật trạng thái đơn hàng
     *
     * @param body Dữ liệu callback từ ZaloPay bao gồm data (JSON string) và mac (chữ ký)
     * @return Response theo format của ZaloPay (return_code, return_message)
     */
    @PostMapping("/zalopay/callback")
    public ZaloPayCallBackResponseDTO callbackZaloPay(@RequestBody ZaloPayCallbackRequest body) {
        return zaloPayService.processCallback(body);
    }


    /**
     * Xử lý return URL từ ZaloPay
     * Endpoint này được gọi khi người dùng quay lại website sau khi thanh toán
     * Hiển thị kết quả thanh toán cho người dùng
     *
     * @return Thông báo kết quả thanh toán
     */
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
