# Tính năng Tự động Hủy Đơn Hàng Khi Quá Hạn Thanh Toán

## Tổng quan

Tính năng này sử dụng **Redis Key Expiration** để tự động hủy các đơn hàng chưa thanh toán sau 15 phút.

## Kiến trúc

```
┌─────────────┐          ┌─────────────┐          ┌─────────────┐
│   Payment   │  Create  │    Redis    │  Expire  │   Listener  │
│   Service   ├─────────►│ (TTL=15min) ├─────────►│   Handler   │
└─────────────┘          └─────────────┘          └──────┬──────┘
                                                          │
                                                          ▼
                                                   ┌─────────────┐
                                                   │ Cancel Order│
                                                   │ Restore Book│
                                                   └─────────────┘
```

## Thành phần chính

### 1. **RedisConfig** (`config/RedisConfig.java`)
- Cấu hình kết nối Redis với Jedis
- Tạo RedisTemplate và RedisMessageListenerContainer
- Enable keyspace notifications

### 2. **OrderTimeoutService** (`service/OrderTimeoutService.java`)
Interface định nghĩa các phương thức:
- `scheduleOrderTimeout()` - Lập lịch timeout cho order
- `cancelOrderTimeout()` - Hủy timeout khi thanh toán thành công
- `handleOrderTimeout()` - Xử lý khi order timeout
- `isOrderScheduledForTimeout()` - Kiểm tra trạng thái timeout

### 3. **OrderTimeoutServiceImpl** (`service/impl/OrderTimeoutServiceImpl.java`)
Implementation xử lý logic timeout:
- Lưu key vào Redis với TTL 15 phút
- Hủy đơn hàng khi timeout
- Hoàn lại số lượng sách vào kho
- Update trạng thái Payment thành FAILED

### 4. **RedisKeyExpirationListener** (`listener/RedisKeyExpirationListener.java`)
Lắng nghe sự kiện key expiration từ Redis:
- Extend `KeyExpirationEventMessageListener`
- Parse orderId từ expired key
- Gọi `OrderTimeoutService.handleOrderTimeout()`

## Luồng hoạt động

### Khi tạo thanh toán (MoMo/VNPay/ZaloPay):

```java
// 1. Tạo Payment với status PENDING
Payment payment = Payment.builder()
    .order(order)
    .status(PaymentStatus.PENDING)
    // ... other fields
    .build();
paymentRepository.save(payment);

// 2. Schedule timeout
orderTimeoutService.scheduleOrderTimeout(order.getOrderID(), 15);
```

Redis lưu key: `order_timeout:{orderId}` với TTL = 15 phút

### Khi thanh toán thành công:

```java
// Cancel timeout để ngăn order bị hủy
orderTimeoutService.cancelOrderTimeout(order.getOrderID());

// Update order status
order.setStatus(OrderStatus.PROCESSING);
```

### Khi key hết hạn (15 phút):

```java
// 1. Redis bắn event __keyevent@0__:expired
// 2. RedisKeyExpirationListener nhận event
// 3. Parse orderId từ key
// 4. Gọi handleOrderTimeout()

handleOrderTimeout(orderId) {
    // Kiểm tra order vẫn PENDING
    if (order.getStatus() == PENDING) {
        // Hủy đơn
        order.setStatus(CANCELLED);
        
        // Update payment
        payment.setStatus(FAILED);
        
        // Hoàn lại số lượng sách
        for (item : orderItems) {
            bookRepository.updateQuantityOnCancelOrder(
                item.getBook().getId(),
                item.getQuantity()
            );
        }
    }
}
```

## Tích hợp vào Payment Services

### MoMoServiceImpl
```java
@Service
@RequiredArgsConstructor
public class MoMoServiceImpl implements PaymentGateway {
    private final OrderTimeoutService orderTimeoutService;
    private static final int PAYMENT_TIMEOUT_MINUTES = 15;
    
    // Khi tạo payment
    payment = paymentRepository.save(payment);
    orderTimeoutService.scheduleOrderTimeout(order.getOrderID(), PAYMENT_TIMEOUT_MINUTES);
    
    // Khi callback thành công
    orderTimeoutService.cancelOrderTimeout(order.getOrderID());
}
```

### VNPayServiceImpl
```java
// Tương tự MoMo
@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements PaymentGateway {
    private final OrderTimeoutService orderTimeoutService;
    private static final int PAYMENT_TIMEOUT_MINUTES = 15;
    
    // Schedule khi tạo, cancel khi thành công
}
```

### ZaloPayServiceImpl
```java
// Tương tự MoMo và VNPay
@Service
@RequiredArgsConstructor
public class ZaloPayServiceImpl implements PaymentGateway {
    private final OrderTimeoutService orderTimeoutService;
    private static final int PAYMENT_TIMEOUT_MINUTES = 15;
}
```

## Cấu hình Redis

### application-develop.yml
```yaml
spring:
  data:
    redis:
      host: redis-15646.c277.us-east-1-3.ec2.cloud.redislabs.com
      port: 15646
      username: default
      password: igZNXMGnoLtmAPWDJYe0UyJSCGDbDAVA
      timeout: 60000
      jedis:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
```

### Enable keyspace notifications
Redis cần được cấu hình để bật keyspace events:

```bash
# Trong Redis server
CONFIG SET notify-keyspace-events Ex
```

Hoặc trong redis.conf:
```
notify-keyspace-events Ex
```

## Database Changes

### BookRepository
Thêm method để hoàn lại số lượng sách:

```java
@Modifying
@Query("UPDATE Book b SET b.stockQuantity = b.stockQuantity + :quantity WHERE b.id = :bookId")
int updateQuantityOnCancelOrder(@Param("bookId") UUID bookId, @Param("quantity") Integer quantity);
```

## Ưu điểm

1. ✅ **Chính xác thời gian**: Redis TTL chính xác từng giây
2. ✅ **Giảm tải Database**: Không cần scheduled job polling DB
3. ✅ **Tự động**: Không cần can thiệp thủ công
4. ✅ **Reliable**: Transaction @Transactional đảm bảo rollback
5. ✅ **Scalable**: Redis xử lý tốt với hàng triệu keys

## Nhược điểm & Lưu ý

1. ⚠️ **Redis dependency**: Cần Redis server luôn chạy
2. ⚠️ **Event loss risk**: Nếu Redis crash đúng lúc key expire (hiếm)
3. ⚠️ **Config required**: Phải enable notify-keyspace-events
4. ⚠️ **Single thread**: Redis event listener chạy single thread

## Xử lý sự cố

### Nếu Redis bị crash
- Order sẽ KHÔNG tự động hủy
- Cần có backup job kiểm tra PENDING orders > 15 phút

### Nếu event bị mất
- Implement fallback scheduler (mỗi 1 giờ check PENDING orders cũ)

### Monitor
```java
log.info("Scheduled timeout for order {} in {} minutes", orderId, PAYMENT_TIMEOUT_MINUTES);
log.info("Cancelled timeout for order {} - payment successful", orderId);
log.info("Order {} has been cancelled due to payment timeout", orderId);
```

## Testing

### Test timeout flow
1. Tạo order với payment method MoMo/VNPay/ZaloPay
2. KHÔNG thanh toán
3. Đợi 15 phút
4. Kiểm tra order.status = CANCELLED
5. Kiểm tra payment.status = FAILED
6. Kiểm tra book.stockQuantity đã được hoàn lại

### Test cancel timeout
1. Tạo order
2. Thanh toán thành công ngay
3. Kiểm tra Redis key đã bị xóa
4. Đợi 15 phút
5. Order vẫn giữ status PROCESSING (không bị hủy)

## Tích hợp với COD

COD (Cash on Delivery) **KHÔNG** cần timeout vì:
- Thanh toán khi nhận hàng
- Order ngay lập tức chuyển sang PROCESSING
- Không có delay 15 phút

## Summary

Tính năng tự động hủy đơn hàng sử dụng Redis Key Expiration cung cấp giải pháp hiệu quả, chính xác cho vấn đề quản lý timeout payment. Đã được tích hợp hoàn chỉnh vào MoMo, VNPay, và ZaloPay payment services.

---
**Version**: 1.0  
**Date**: 2025-12-09  
**Author**: AI Assistant

