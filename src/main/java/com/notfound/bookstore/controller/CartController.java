package com.notfound.bookstore.controller;


import com.notfound.bookstore.model.dto.request.cartrequest.AddToCartRequest;
import com.notfound.bookstore.model.dto.request.cartrequest.UpdateCartItemRequest;
import com.notfound.bookstore.model.dto.request.orderrequest.CheckoutRequest;
import com.notfound.bookstore.model.dto.response.cartresponse.CartItemResponse;
import com.notfound.bookstore.model.dto.response.cartresponse.CartResponse;
import com.notfound.bookstore.model.dto.response.orderresponse.OrderResponse;
import com.notfound.bookstore.model.entity.Cart;
import com.notfound.bookstore.model.entity.CartItem;
import com.notfound.bookstore.model.entity.User;
import com.notfound.bookstore.model.mapper.CartMapper;
import com.notfound.bookstore.repository.UserRepository;
import com.notfound.bookstore.service.CartService;
import com.notfound.bookstore.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartMapper cartMapper;
    private final UserRepository userRepository;
    private final OrderService orderService;

    /**
     * Lấy thông tin giỏ hàng
     */
    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }

        String username = jwt.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Cart cart = cartService.getCartByUserId(user.getId());
        List<CartItem> items = cartService.getCartItems(user.getId());

        if (items.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Không có sản phẩm trong giỏ hàng",
                    "cart", CartResponse.builder()
                            .cartId(cart.getCartID())
                            .userId(user.getId())
                            .items(List.of())
                            .itemCount(0L)
                            .totalPrice(0.0)
                            .build()
            ));
        }

        CartResponse cartResponse = cartMapper.toCartResponse(cart, items);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lấy giỏ hàng thành công",
                "cart", cartResponse
        ));
    }

    /**
     * Thêm sách vào giỏ hàng
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddToCartRequest request) {

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }

        try {
            String username = jwt.getSubject();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            CartItem cartItem = cartService.addBookToCart(
                    user.getId(),
                    request.getBookId(),
                    request.getQuantity()
            );

            CartItemResponse response = cartMapper.toCartItemResponse(cartItem);
            Long itemCount = cartService.getCartItemCount(user.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Đã thêm sản phẩm vào giỏ hàng");
            result.put("cartItem", response);
            result.put("cartItemCount", itemCount);

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ
     */
    @PutMapping("/update/{bookId}")
    public ResponseEntity<?> updateCartItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID bookId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }

        try {
            String username = jwt.getSubject();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            CartItem cartItem = cartService.updateCartItemQuantity(
                    user.getId(),
                    bookId,
                    request.getQuantity()
            );

            CartItemResponse response = cartMapper.toCartItemResponse(cartItem);
            Double totalPrice = cartService.getCartTotal(user.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Đã cập nhật số lượng");
            result.put("cartItem", response);
            result.put("totalPrice", totalPrice);

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @DeleteMapping("/remove/{bookId}")
    public ResponseEntity<?> removeFromCart(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID bookId) {

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }

        try {
            String username = jwt.getSubject();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            cartService.removeBookFromCart(user.getId(), bookId);

            Long itemCount = cartService.getCartItemCount(user.getId());
            Double totalPrice = cartService.getCartTotal(user.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);

            if (itemCount == 0) {
                result.put("message", "Đã xóa sản phẩm. Giỏ hàng hiện đang trống");
            } else {
                result.put("message", "Đã xóa sản phẩm khỏi giỏ hàng");
            }

            result.put("cartItemCount", itemCount);
            result.put("totalPrice", totalPrice);

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }

        try {
            String username = jwt.getSubject();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            Long itemCount = cartService.getCartItemCount(user.getId());

            if (itemCount == 0) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Giỏ hàng đã trống"
                ));
            }

            cartService.clearCart(user.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã xóa toàn bộ giỏ hàng"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Lấy số lượng items trong giỏ
     */
    @GetMapping("/count")
    public ResponseEntity<?> getCartItemCount(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.ok(Map.of("count", 0));
        }

        String username = jwt.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Long count = cartService.getCartItemCount(user.getId());

        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Kiểm tra sách có trong giỏ không
     */
    @GetMapping("/check/{bookId}")
    public ResponseEntity<?> checkBookInCart(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID bookId) {

        if (jwt == null) {
            return ResponseEntity.ok(Map.of("inCart", false));
        }

        String username = jwt.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        boolean inCart = cartService.isBookInCart(user.getId(), bookId);

        return ResponseEntity.ok(Map.of("inCart", inCart));
    }

    /**
     * Checkout giỏ hàng (tạo đơn hàng từ giỏ hàng)
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody(required = false) CheckoutRequest request) {

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }

        try {
            String username = jwt.getSubject();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // Nếu không có request body, tạo mặc định
            if (request == null) {
                request = CheckoutRequest.builder()
                        .paymentMethod("COD")
                        .build();
            }

            // Validate paymentMethod
            if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Vui lòng chọn phương thức thanh toán"));
            }

            OrderResponse orderResponse = orderService.checkout(user.getId(), request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Đặt hàng thành công");
            result.put("order", orderResponse);

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
