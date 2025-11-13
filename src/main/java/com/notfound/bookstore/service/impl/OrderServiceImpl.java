package com.notfound.bookstore.service.impl;

import com.notfound.bookstore.model.dto.request.orderrequest.CheckoutRequest;
import com.notfound.bookstore.model.dto.response.orderresponse.OrderItemResponse;
import com.notfound.bookstore.model.dto.response.orderresponse.OrderResponse;
import com.notfound.bookstore.model.entity.*;
import com.notfound.bookstore.model.enums.OrderStatus;
import com.notfound.bookstore.repository.*;
import com.notfound.bookstore.service.CartService;
import com.notfound.bookstore.service.OrderService;
import com.notfound.bookstore.service.PromotionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final PromotionService promotionService;

    @Override
    @Transactional
    public OrderResponse checkout(UUID userId, CheckoutRequest request) {
        // 1. Lấy user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 2. Lấy giỏ hàng
        List<CartItem> cartItems = cartService.getCartItems(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        // 3. Tính tổng tiền
        Double subtotal = cartItems.stream()
                .mapToDouble(CartItem::getSubTotal)
                .sum();

        // 4. Áp dụng khuyến mãi nếu có
        Promotion promotion = null;
        Double discountAmount = 0.0;

        if (request.getDiscountCode() != null && !request.getDiscountCode().isEmpty()) {
            promotion = promotionRepository.findValidPromotionByCode(
                    request.getDiscountCode(),
                    LocalDate.now()
            ).orElseThrow(() -> new RuntimeException("Mã khuyến mãi không hợp lệ hoặc đã hết hạn"));

            // Kiểm tra usage limit
            if (!promotion.isValid()) {
                throw new RuntimeException("Mã khuyến mãi đã hết lượt sử dụng");
            }

            // Tính discount
            discountAmount = subtotal * (promotion.getDiscountPercent() / 100.0);

            // Áp dụng mã (tăng usage count)
            promotionService.applyPromotionCode(promotion.getPromotionID());
        }

        // 5. Tính tổng tiền sau giảm giá
        Double totalAmount = subtotal - discountAmount;

        // 6. Tạo đơn hàng
        Order order = new Order();
        order.setCustomer(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPromotion(promotion);
        order.setDiscountAmount(discountAmount);
        order.setTaxAmount(0.0); // Có thể tính thuế nếu cần
        order.setOrderDate(LocalDateTime.now());

        order = orderRepository.save(order);

        // 7. Tạo order items từ cart items
        Order finalOrder = order;
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    Book book = cartItem.getBook();

                    // Kiểm tra tồn kho
                    if (book.getStockQuantity() < cartItem.getQuantity()) {
                        throw new RuntimeException("Sách '" + book.getTitle() + "' không đủ số lượng trong kho");
                    }

                    // Trừ tồn kho
                    book.setStockQuantity(book.getStockQuantity() - cartItem.getQuantity());
                    bookRepository.save(book);

                    // Tạo order item
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(finalOrder);
                    orderItem.setBook(book);
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setUnitPrice(book.getPrice()); // Sửa: Lấy giá từ book
                    orderItem.setSubtotal(cartItem.getSubTotal());

                    return orderItem;
                })
                .collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);

        // 8. Xóa giỏ hàng sau khi checkout thành công
        cartService.clearCart(userId);

        // 9. Tạo response
        return buildOrderResponse(order, orderItems, promotion, discountAmount, subtotal);
    }

    @Override
    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderID(orderId);

        Double subtotal = orderItems.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();

        return buildOrderResponse(order, orderItems, order.getPromotion(),
                order.getDiscountAmount(), subtotal);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        List<Order> orders = orderRepository.findByCustomerId(userId);
        return orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderOrderID(order.getOrderID());
                    Double subtotal = orderItems.stream().mapToDouble(OrderItem::getSubtotal).sum();
                    return buildOrderResponse(order, orderItems, order.getPromotion(),
                            order.getDiscountAmount(), subtotal);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<OrderResponse> getOrdersByUserId(UUID userId, Pageable pageable) {
        // Cần implement với Page - tạm thời throw exception
        throw new UnsupportedOperationException("Chưa implement pagination cho user orders");
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(order -> {
            List<OrderItem> orderItems = orderItemRepository.findByOrderOrderID(order.getOrderID());
            Double subtotal = orderItems.stream().mapToDouble(OrderItem::getSubtotal).sum();
            return buildOrderResponse(order, orderItems, order.getPromotion(),
                    order.getDiscountAmount(), subtotal);
        });
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        order.setStatus(status);
        order = orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderID(orderId);
        Double subtotal = orderItems.stream().mapToDouble(OrderItem::getSubtotal).sum();

        return buildOrderResponse(order, orderItems, order.getPromotion(),
                order.getDiscountAmount(), subtotal);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Kiểm tra quyền hủy đơn
        if (!order.getCustomer().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }

        // Chỉ cho phép hủy đơn ở trạng thái PENDING hoặc CONFIRMED
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái hiện tại");
        }

        // Hoàn lại tồn kho
        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderID(orderId);
        orderItems.forEach(item -> {
            Book book = item.getBook();
            book.setStockQuantity(book.getStockQuantity() + item.getQuantity());
            bookRepository.save(book);
        });

        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        Double subtotal = orderItems.stream().mapToDouble(OrderItem::getSubtotal).sum();
        return buildOrderResponse(order, orderItems, order.getPromotion(),
                order.getDiscountAmount(), subtotal);
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderOrderID(order.getOrderID());
                    Double subtotal = orderItems.stream().mapToDouble(OrderItem::getSubtotal).sum();
                    return buildOrderResponse(order, orderItems, order.getPromotion(),
                            order.getDiscountAmount(), subtotal);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Double getTotalRevenue() {
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED ||
                               order.getStatus() == OrderStatus.DELIVERED)
                .mapToDouble(Order::getTotalAmount)
                .sum();
    }

    @Override
    public Long countOrdersByUserId(UUID userId) {
        return orderRepository.countByCustomerId(userId);
    }

    private OrderResponse buildOrderResponse(Order order, List<OrderItem> orderItems,
                                             Promotion promotion, Double discountAmount,
                                             Double subtotal) {
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getOrderItemID())
                        .bookTitle(item.getBook().getTitle())
                        .quantity(item.getQuantity())
                        .price(BigDecimal.valueOf(item.getUnitPrice()))
                        .build())
                .collect(Collectors.toList());

        OrderResponse.OrderResponseBuilder builder = OrderResponse.builder()
                .id(order.getOrderID())
                .orderCode("ORD-" + order.getOrderID().toString().substring(0, 8).toUpperCase())
                .orderDate(order.getOrderDate())
                .status(order.getStatus().name())
                .subtotal(BigDecimal.valueOf(subtotal))
                .total(BigDecimal.valueOf(order.getTotalAmount()))
                .paymentMethod(order.getPaymentMethod())
                .items(itemResponses);

        // Thêm thông tin khuyến mãi nếu có
        if (promotion != null) {
            builder.promotionCode(promotion.getCode())
                   .promotionName(promotion.getName())
                   .discountPercent(promotion.getDiscountPercent())
                   .discountAmount(BigDecimal.valueOf(discountAmount));
        } else {
            builder.discountAmount(BigDecimal.ZERO);
        }

        return builder.build();
    }
}
