package com.notfound.bookstore.controller;

import com.notfound.bookstore.model.dto.response.ApiResponse;
import com.notfound.bookstore.model.dto.response.orderresponse.OrderResponse;
import com.notfound.bookstore.model.entity.User;
import com.notfound.bookstore.model.enums.OrderStatus;
import com.notfound.bookstore.repository.UserRepository;
import com.notfound.bookstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    /**
     * Lấy danh sách đơn hàng của user đang đăng nhập
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<?> getMyOrders(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder()
                            .code(4001)
                            .message("Vui lòng đăng nhập")
                            .build());
        }

        String username = jwt.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        List<OrderResponse> orders = orderService.getOrdersByUserId(user.getId());

        return ResponseEntity.ok(ApiResponse.<List<OrderResponse>>builder()
                .code(1000)
                .message("Lấy danh sách đơn hàng thành công")
                .result(orders)
                .build());
    }

    /**
     * Lấy chi tiết đơn hàng theo ID
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId) {

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder()
                            .code(4001)
                            .message("Vui lòng đăng nhập")
                            .build());
        }

        try {
            OrderResponse order = orderService.getOrderById(orderId);

            return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                    .code(1000)
                    .message("Lấy thông tin đơn hàng thành công")
                    .result(order)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .code(4004)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Hủy đơn hàng
     * POST /api/orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId) {

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder()
                            .code(4001)
                            .message("Vui lòng đăng nhập")
                            .build());
        }

        try {
            String username = jwt.getSubject();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            OrderResponse order = orderService.cancelOrder(orderId, user.getId());

            return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                    .code(1000)
                    .message("Hủy đơn hàng thành công")
                    .result(order)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .code(4005)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * ADMIN: Lấy tất cả đơn hàng (có phân trang)
     * GET /api/orders/admin/all?page=0&size=10
     */
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> orders = orderService.getAllOrders(pageable);

        return ResponseEntity.ok(ApiResponse.<Page<OrderResponse>>builder()
                .code(1000)
                .message("Lấy danh sách đơn hàng thành công")
                .result(orders)
                .build());
    }

    /**
     * ADMIN: Cập nhật trạng thái đơn hàng
     * PUT /api/orders/admin/{orderId}/status?status=CONFIRMED
     */
    @PutMapping("/admin/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam String status) {

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            OrderResponse order = orderService.updateOrderStatus(orderId, orderStatus);

            return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                    .code(1000)
                    .message("Cập nhật trạng thái đơn hàng thành công")
                    .result(order)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .code(4003)
                            .message("Trạng thái không hợp lệ. Các trạng thái: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, COMPLETED")
                            .build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .code(4004)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * ADMIN: Lấy đơn hàng theo trạng thái
     * GET /api/orders/admin/status/{status}
     */
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<OrderResponse> orders = orderService.getOrdersByStatus(orderStatus);

            return ResponseEntity.ok(ApiResponse.<List<OrderResponse>>builder()
                    .code(1000)
                    .message("Lấy danh sách đơn hàng theo trạng thái thành công")
                    .result(orders)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .code(4003)
                            .message("Trạng thái không hợp lệ")
                            .build());
        }
    }

    /**
     * ADMIN: Lấy tổng doanh thu
     * GET /api/orders/admin/revenue
     */
    @GetMapping("/admin/revenue")
    public ResponseEntity<?> getTotalRevenue() {
        Double revenue = orderService.getTotalRevenue();

        return ResponseEntity.ok(ApiResponse.<Double>builder()
                .code(1000)
                .message("Lấy tổng doanh thu thành công")
                .result(revenue)
                .build());
    }

    /**
     * Đếm số đơn hàng của user
     * GET /api/orders/count
     */
    @GetMapping("/count")
    public ResponseEntity<?> countMyOrders(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.ok(ApiResponse.<Long>builder()
                    .code(1000)
                    .result(0L)
                    .build());
        }

        String username = jwt.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Long count = orderService.countOrdersByUserId(user.getId());

        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .code(1000)
                .message("Đếm số đơn hàng thành công")
                .result(count)
                .build());
    }
}
