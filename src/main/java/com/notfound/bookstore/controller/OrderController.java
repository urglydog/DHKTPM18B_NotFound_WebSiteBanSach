package com.notfound.bookstore.controller;

import com.notfound.bookstore.model.dto.request.orderrequest.CheckoutRequest;
import com.notfound.bookstore.model.dto.request.paymentrequest.PaymentRequest;
import com.notfound.bookstore.model.dto.response.ApiResponse;
import com.notfound.bookstore.model.dto.response.orderresponse.OrderResponse;
import com.notfound.bookstore.model.dto.response.paymentresponse.CreatePaymentResponse;
import com.notfound.bookstore.model.entity.User;
import com.notfound.bookstore.model.enums.OrderStatus;
import com.notfound.bookstore.repository.UserRepository;
import com.notfound.bookstore.service.OrderService;
import com.notfound.bookstore.service.impl.VNPayServiceImpl;
import com.notfound.bookstore.service.impl.ZaloPayServiceImpl;
import com.notfound.bookstore.service.impl.MoMoServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final VNPayServiceImpl vnPayService;
    private final ZaloPayServiceImpl zaloPayService;
    private final MoMoServiceImpl moMoService;

    /**
     * Lấy danh sách đơn hàng của user đang đăng nhập
     * GET /api/orders
     */
    @GetMapping
    public ApiResponse<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ApiResponse.<List<OrderResponse>>builder()
                    .code(4001)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        String username = jwt.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        List<OrderResponse> orders = orderService.getOrdersByUserId(user.getId());

        return ApiResponse.<List<OrderResponse>>builder()
                .code(1000)
                .message("Lấy danh sách đơn hàng thành công")
                .result(orders)
                .build();
    }

    /**
     * Lấy chi tiết đơn hàng theo ID
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId) {

        if (jwt == null) {
            return ApiResponse.<OrderResponse>builder()
                    .code(4001)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        try {
            OrderResponse order = orderService.getOrderById(orderId);

            return ApiResponse.<OrderResponse>builder()
                    .code(1000)
                    .message("Lấy thông tin đơn hàng thành công")
                    .result(order)
                    .build();
        } catch (RuntimeException e) {
            return ApiResponse.<OrderResponse>builder()
                    .code(4004)
                    .message(e.getMessage())
                    .build();
        }
    }

    /**
     * Hủy đơn hàng
     * POST /api/orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId) {

        if (jwt == null) {
            return ApiResponse.<OrderResponse>builder()
                    .code(4001)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        try {
            String username = jwt.getSubject();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            OrderResponse order = orderService.cancelOrder(orderId, user.getId());

            return ApiResponse.<OrderResponse>builder()
                    .code(1000)
                    .message("Hủy đơn hàng thành công")
                    .result(order)
                    .build();
        } catch (RuntimeException e) {
            return ApiResponse.<OrderResponse>builder()
                    .code(4005)
                    .message(e.getMessage())
                    .build();
        }
    }

    /**
     * ADMIN: Lấy tất cả đơn hàng (có phân trang)
     * GET /api/orders/admin/all?page=0&size=10
     */
    @GetMapping("/admin/all")
    public ApiResponse<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> orders = orderService.getAllOrders(pageable);

        return ApiResponse.<Page<OrderResponse>>builder()
                .code(1000)
                .message("Lấy danh sách đơn hàng thành công")
                .result(orders)
                .build();
    }

    /**
     * ADMIN: Cập nhật trạng thái đơn hàng
     * PUT /api/orders/admin/{orderId}/status?status=CONFIRMED
     */
    @PutMapping("/admin/{orderId}/status")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam String status) {

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            OrderResponse order = orderService.updateOrderStatus(orderId, orderStatus);

            return ApiResponse.<OrderResponse>builder()
                    .code(1000)
                    .message("Cập nhật trạng thái đơn hàng thành công")
                    .result(order)
                    .build();
        } catch (IllegalArgumentException e) {
            return ApiResponse.<OrderResponse>builder()
                    .code(4003)
                    .message("Trạng thái không hợp lệ. Các trạng thái: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, COMPLETED")
                    .build();
        } catch (RuntimeException e) {
            return ApiResponse.<OrderResponse>builder()
                    .code(4004)
                    .message(e.getMessage())
                    .build();
        }
    }

    /**
     * ADMIN: Lấy đơn hàng theo trạng thái
     * GET /api/orders/admin/status/{status}
     */
    @GetMapping("/admin/status/{status}")
    public ApiResponse<List<OrderResponse>> getOrdersByStatus(@PathVariable String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<OrderResponse> orders = orderService.getOrdersByStatus(orderStatus);

            return ApiResponse.<List<OrderResponse>>builder()
                    .code(1000)
                    .message("Lấy danh sách đơn hàng theo trạng thái thành công")
                    .result(orders)
                    .build();
        } catch (IllegalArgumentException e) {
            return ApiResponse.<List<OrderResponse>>builder()
                    .code(4003)
                    .message("Trạng thái không hợp lệ")
                    .build();
        }
    }

    /**
     * ADMIN: Lấy tổng doanh thu
     * GET /api/orders/admin/revenue
     */
    @GetMapping("/admin/revenue")
    public ApiResponse<Double> getTotalRevenue() {
        Double revenue = orderService.getTotalRevenue();

        return ApiResponse.<Double>builder()
                .code(1000)
                .message("Lấy tổng doanh thu thành công")
                .result(revenue)
                .build();
    }

    /**
     * Đếm số đơn hàng của user
     * GET /api/orders/count
     */
    @GetMapping("/count")
    public ApiResponse<Long> countMyOrders(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ApiResponse.<Long>builder()
                    .code(1000)
                    .result(0L)
                    .build();
        }

        String username = jwt.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Long count = orderService.countOrdersByUserId(user.getId());

        return ApiResponse.<Long>builder()
                .code(1000)
                .message("Đếm số đơn hàng thành công")
                .result(count)
                .build();
    }

    /**
     * Checkout và tạo đơn hàng từ giỏ hàng (COD)
     * POST /api/orders/checkout
     */
    @PostMapping("/checkout")
    public ApiResponse<OrderResponse> checkout(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) CheckoutRequest request) {

        if (jwt == null) {
            return ApiResponse.<OrderResponse>builder()
                    .code(4001)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        try {
            String username = jwt.getSubject();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // Nếu không có request body, tạo mặc định COD
            if (request == null) {
                request = CheckoutRequest.builder()
                        .paymentMethod("COD")
                        .build();
            }

            // Validate paymentMethod
            if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
                return ApiResponse.<OrderResponse>builder()
                        .code(4003)
                        .message("Vui lòng chọn phương thức thanh toán")
                        .build();
            }

            OrderResponse orderResponse = orderService.checkout(user.getId(), request);

            return ApiResponse.<OrderResponse>builder()
                    .code(1000)
                    .message("Đặt hàng thành công")
                    .result(orderResponse)
                    .build();
        } catch (RuntimeException e) {
            return ApiResponse.<OrderResponse>builder()
                    .code(4005)
                    .message(e.getMessage())
                    .build();
        }
    }

    /**
     * Checkout và thanh toán online qua VNPay
     * POST /api/orders/checkout/vnpay
     */
    @PostMapping("/checkout/vnpay")
    public ApiResponse<CreatePaymentResponse> checkoutWithVNPay(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckoutRequest request,
            HttpServletRequest httpServletRequest) {

        if (jwt == null) {
            return ApiResponse.<CreatePaymentResponse>builder()
                    .code(4001)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        try {
            String username = jwt.getSubject();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // Set payment method to VNPay
            request.setPaymentMethod("VNPay");

            // 1. Tạo đơn hàng trước
            OrderResponse orderResponse = orderService.checkout(user.getId(), request);

            // 2. Tạo payment request
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .orderId(orderResponse.getId())
                    .amount(orderResponse.getTotal().longValue())
                    .build();

            // 3. Tạo VNPay payment URL
            CreatePaymentResponse paymentResponse = vnPayService.createVNPayPaymentUrl(
                    paymentRequest,
                    httpServletRequest
            );

            return ApiResponse.<CreatePaymentResponse>builder()
                    .code(1000)
                    .message("Đã tạo đơn hàng và đường dẫn thanh toán VNPay thành công")
                    .result(paymentResponse)
                    .build();

        } catch (RuntimeException e) {
            return ApiResponse.<CreatePaymentResponse>builder()
                    .code(4005)
                    .message(e.getMessage())
                    .build();
        }
    }

    /**
     * Checkout và thanh toán online qua ZaloPay
     * POST /api/orders/checkout/zalopay
     */
    @PostMapping("/checkout/zalopay")
    public ApiResponse<CreatePaymentResponse> checkoutWithZaloPay(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckoutRequest request) {

        if (jwt == null) {
            return ApiResponse.<CreatePaymentResponse>builder()
                    .code(4001)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        try {
            String username = jwt.getSubject();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // Set payment method to ZaloPay
            request.setPaymentMethod("ZaloPay");

            // 1. Tạo đơn hàng trước
            OrderResponse orderResponse = orderService.checkout(user.getId(), request);

            // 2. Tạo payment request
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .orderId(orderResponse.getId())
                    .amount(orderResponse.getTotal().longValue())
                    .build();

            // 3. Tạo ZaloPay payment URL
            CreatePaymentResponse paymentResponse = zaloPayService.createOrderTransaction(paymentRequest);

            return ApiResponse.<CreatePaymentResponse>builder()
                    .code(1000)
                    .message("Đã tạo đơn hàng và đường dẫn thanh toán ZaloPay thành công")
                    .result(paymentResponse)
                    .build();

        } catch (RuntimeException e) {
            return ApiResponse.<CreatePaymentResponse>builder()
                    .code(4005)
                    .message(e.getMessage())
                    .build();
        }
    }

    /**
     * Checkout và thanh toán online qua MoMo
     * POST /api/orders/checkout/momo
     */
    @Transactional
    @PostMapping("/checkout/momo")
    public ApiResponse<CreatePaymentResponse> checkoutWithMoMo(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckoutRequest request) {

        if (jwt == null) {
            return ApiResponse.<CreatePaymentResponse>builder()
                    .code(4001)
                    .message("Vui lòng đăng nhập")
                    .build();
        }

        try {
            String username = jwt.getSubject();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // Set payment method to MoMo
            request.setPaymentMethod("MoMo");

            // 1. Tạo đơn hàng trước
            OrderResponse orderResponse = orderService.checkout(user.getId(), request);

            // Log để debug
            log.info("Created order with ID: {}", orderResponse.getId());

            // 2. Tạo payment request
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .orderId(orderResponse.getId())
                    .amount(orderResponse.getTotal().longValue())
                    .build();

            // 3. Tạo MoMo payment URL
            CreatePaymentResponse paymentResponse = moMoService.createMoMoPayment(paymentRequest);

            return ApiResponse.<CreatePaymentResponse>builder()
                    .code(1000)
                    .message("Đã tạo đơn hàng và đường dẫn thanh toán MoMo thành công")
                    .result(paymentResponse)
                    .build();

        } catch (RuntimeException e) {
            log.error("Error in checkoutWithMoMo: {}", e.getMessage(), e);
            return ApiResponse.<CreatePaymentResponse>builder()
                    .code(4005)
                    .message(e.getMessage())
                    .build();
        }
    }
}
