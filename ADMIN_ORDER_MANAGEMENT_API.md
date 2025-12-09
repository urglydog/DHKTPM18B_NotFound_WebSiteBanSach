# 📋 Admin Order Management API Documentation

## Overview

Đã thêm các REST API endpoints cho Admin quản lý đơn hàng, bao gồm xác nhận COD, cập nhật trạng thái, tìm kiếm và thống kê.

---

## 📌 Luồng Trạng Thái Đơn Hàng

```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED → COMPLETED
   ↓           ↓            ↓           ↓          ↓
CANCELLED  CANCELLED    CANCELLED   CANCELLED  ❌ (không thể hủy)
```

---

## 🔐 Authentication

Tất cả endpoints đều yêu cầu:
- ✅ Authentication (JWT token)
- ✅ Role: ADMIN

---

## 📝 Admin Endpoints

### 1. **Lấy Tất Cả Đơn Hàng** (Phân trang)
```http
GET /api/orders/admin/all?page=0&size=10
```

**Query Parameters:**
- `page` (optional, default: 0)
- `size` (optional, default: 10)

**Response:**
```json
{
  "code": 1000,
  "message": "Lấy danh sách đơn hàng thành công",
  "result": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0
  }
}
```

---

### 2. **Xác Nhận Đơn COD** ⭐ (PENDING → CONFIRMED)
```http
POST /api/orders/admin/{orderId}/confirm
```

**Mô tả:**
- Chỉ áp dụng cho đơn hàng COD
- Chuyển từ PENDING → CONFIRMED

**Validation:**
- ✅ Phải là đơn COD
- ✅ Trạng thái hiện tại phải là PENDING

**Response Success:**
```json
{
  "code": 1000,
  "message": "Xác nhận đơn hàng COD thành công",
  "result": {
    "id": "uuid",
    "status": "CONFIRMED",
    "paymentMethod": "COD",
    ...
  }
}
```

**Response Error:**
```json
{
  "code": 4003,
  "message": "Chỉ có thể xác nhận đơn hàng COD"
}
```

---

### 3. **Bắt Đầu Xử Lý** (CONFIRMED/PENDING → PROCESSING)
```http
POST /api/orders/admin/{orderId}/process
```

**Mô tả:**
- Bắt đầu xử lý đơn hàng (đóng gói, chuẩn bị hàng)
- Chuyển từ CONFIRMED hoặc PENDING → PROCESSING

**Validation:**
- ✅ Trạng thái phải là CONFIRMED hoặc PENDING

---

### 4. **Giao Cho Shipper** (PROCESSING → SHIPPED)
```http
POST /api/orders/admin/{orderId}/ship
```

**Mô tả:**
- Đánh dấu đơn đã giao cho shipper
- Chuyển từ PROCESSING → SHIPPED

**Validation:**
- ✅ Trạng thái phải là PROCESSING

---

### 5. **Xác Nhận Đã Giao** (SHIPPED → DELIVERED)
```http
POST /api/orders/admin/{orderId}/deliver
```

**Mô tả:**
- Xác nhận đơn hàng đã giao thành công đến khách
- Chuyển từ SHIPPED → DELIVERED

**Validation:**
- ✅ Trạng thái phải là SHIPPED

---

### 6. **Hoàn Thành** (DELIVERED → COMPLETED)
```http
POST /api/orders/admin/{orderId}/complete
```

**Mô tả:**
- Hoàn tất đơn hàng (khách đã nhận và hài lòng)
- Chuyển từ DELIVERED → COMPLETED

**Validation:**
- ✅ Trạng thái phải là DELIVERED

---

### 7. **Hủy Đơn Hàng** (Admin)
```http
POST /api/orders/admin/{orderId}/cancel
```

**Mô tả:**
- Admin hủy đơn hàng
- Chuyển sang CANCELLED

**Validation:**
- ❌ Không thể hủy đơn đã DELIVERED hoặc COMPLETED

**Response:**
```json
{
  "code": 1000,
  "message": "Đã hủy đơn hàng",
  "result": {
    "id": "uuid",
    "status": "CANCELLED",
    ...
  }
}
```

---

### 8. **Lấy Chi Tiết Đơn Hàng**
```http
GET /api/orders/admin/{orderId}/details
```

**Mô tả:**
- Lấy chi tiết đầy đủ của đơn hàng
- Không cần kiểm tra quyền sở hữu (khác với user endpoint)

---

### 9. **Lấy Đơn COD Chưa Xác Nhận**
```http
GET /api/orders/admin/cod/pending
```

**Mô tả:**
- Lấy tất cả đơn COD đang ở trạng thái PENDING
- Để admin duyệt đơn

**Response:**
```json
{
  "code": 1000,
  "message": "Lấy danh sách đơn COD chưa xác nhận thành công",
  "result": [
    {
      "id": "uuid",
      "status": "PENDING",
      "paymentMethod": "COD",
      "total": 250000,
      "customerName": "Nguyễn Văn A",
      ...
    }
  ]
}
```

---

### 10. **Tìm Kiếm Đơn Hàng**
```http
GET /api/orders/admin/search?keyword=John&page=0&size=10
```

**Query Parameters:**
- `keyword` (required) - Tìm theo Order ID, Customer Name, Recipient Name
- `page` (optional, default: 0)
- `size` (optional, default: 10)

**Tìm kiếm theo:**
- Order ID
- Customer Name
- Recipient Name (người nhận)

---

### 11. **Thống Kê Tổng Quan**
```http
GET /api/orders/admin/statistics?startDate=2024-01-01&endDate=2024-01-31
```

**Query Parameters:**
- `startDate` (optional) - Format: yyyy-MM-dd
- `endDate` (optional) - Format: yyyy-MM-dd

**Response:**
```json
{
  "code": 1000,
  "message": "Lấy thống kê thành công",
  "result": {
    "pending": 15,
    "confirmed": 8,
    "processing": 12,
    "shipped": 20,
    "delivered": 30,
    "completed": 100,
    "cancelled": 5,
    "totalRevenue": 50000000.0,
    "totalOrders": 190
  }
}
```

---

### 12. **Cập Nhật Trạng Thái** (Flexible)
```http
PUT /api/orders/admin/{orderId}/status?status=CONFIRMED
```

**Query Parameters:**
- `status` - PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, COMPLETED

**Mô tả:**
- Cho phép admin chuyển trạng thái tự do (không validate flow)
- Dùng trong trường hợp đặc biệt

---

### 13. **Lấy Đơn Theo Trạng Thái**
```http
GET /api/orders/admin/status/{status}?startDate=2024-01-01&endDate=2024-01-31
```

**Path Parameters:**
- `status` - PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, COMPLETED

**Query Parameters:**
- `startDate` (optional)
- `endDate` (optional)

**Response:**
```json
{
  "code": 1000,
  "message": "Lấy danh sách đơn hàng theo trạng thái thành công",
  "result": [...]
}
```

---

### 14. **Tổng Doanh Thu**
```http
GET /api/orders/admin/revenue?startDate=2024-01-01&endDate=2024-01-31
```

**Query Parameters:**
- `startDate` (optional)
- `endDate` (optional)

**Response:**
```json
{
  "code": 1000,
  "message": "Lấy tổng doanh thu thành công",
  "result": 50000000.0
}
```

---

## 🔄 Use Cases

### Use Case 1: Xử Lý Đơn COD Mới
```
1. Admin vào /api/orders/admin/cod/pending
2. Xem danh sách đơn COD chưa xác nhận
3. Gọi POST /api/orders/admin/{orderId}/confirm
4. Đơn chuyển sang CONFIRMED
5. Tiếp tục xử lý: /process → /ship → /deliver → /complete
```

### Use Case 2: Xử Lý Đơn Online (VNPay/MoMo/ZaloPay)
```
1. Đơn online tự động chuyển sang PROCESSING sau khi thanh toán thành công
2. Admin vào /api/orders/admin/status/PROCESSING
3. Đóng gói hàng
4. Gọi POST /api/orders/admin/{orderId}/ship
5. Shipper giao hàng
6. Gọi POST /api/orders/admin/{orderId}/deliver
7. Khách hàng xác nhận OK
8. Gọi POST /api/orders/admin/{orderId}/complete
```

### Use Case 3: Hủy Đơn
```
- Khách yêu cầu hủy: POST /api/orders/admin/{orderId}/cancel
- Hết hàng: POST /api/orders/admin/{orderId}/cancel
- Chỉ hủy được khi chưa DELIVERED/COMPLETED
```

### Use Case 4: Xem Thống Kê
```
1. Vào dashboard: GET /api/orders/admin/statistics
2. Xem số đơn theo trạng thái
3. Xem tổng doanh thu
4. Filter theo date range nếu cần
```

---

## 📊 Order Status Workflow

### COD Flow
```
1. User checkout COD
   ↓
2. Order status = PENDING
   ↓
3. Admin confirm: POST /admin/{id}/confirm
   ↓ 
4. Order status = CONFIRMED
   ↓
5. Admin process: POST /admin/{id}/process
   ↓
6. Order status = PROCESSING
   ↓
7. Admin ship: POST /admin/{id}/ship
   ↓
8. Order status = SHIPPED
   ↓
9. Admin deliver: POST /admin/{id}/deliver
   ↓
10. Order status = DELIVERED
   ↓
11. Admin complete: POST /admin/{id}/complete
   ↓
12. Order status = COMPLETED ✅
```

### Online Payment Flow (VNPay/MoMo/ZaloPay)
```
1. User checkout với VNPay/MoMo/ZaloPay
   ↓
2. Order status = PENDING
   ↓
3. User thanh toán thành công
   ↓
4. Order status = PROCESSING (tự động)
   ↓
5. Admin ship: POST /admin/{id}/ship
   ↓
... (giống COD từ bước 7)
```

---

## ⚠️ Validation Rules

### Confirm COD
- ✅ Payment method MUST be "COD"
- ✅ Status MUST be "PENDING"

### Process Order
- ✅ Status MUST be "CONFIRMED" OR "PENDING"

### Ship Order
- ✅ Status MUST be "PROCESSING"

### Deliver Order
- ✅ Status MUST be "SHIPPED"

### Complete Order
- ✅ Status MUST be "DELIVERED"

### Cancel Order
- ❌ CANNOT cancel if "DELIVERED" or "COMPLETED"

---

## 🧪 Testing

### Postman Collection

```javascript
// 1. Get all orders
GET http://localhost:8080/api/orders/admin/all?page=0&size=10
Headers: Authorization: Bearer {admin_token}

// 2. Confirm COD order
POST http://localhost:8080/api/orders/admin/{orderId}/confirm
Headers: Authorization: Bearer {admin_token}

// 3. Get pending COD orders
GET http://localhost:8080/api/orders/admin/cod/pending
Headers: Authorization: Bearer {admin_token}

// 4. Search orders
GET http://localhost:8080/api/orders/admin/search?keyword=John
Headers: Authorization: Bearer {admin_token}

// 5. Get statistics
GET http://localhost:8080/api/orders/admin/statistics
Headers: Authorization: Bearer {admin_token}

// 6. Get orders by status
GET http://localhost:8080/api/orders/admin/status/PENDING
Headers: Authorization: Bearer {admin_token}
```

---

## 🎯 Summary

**Đã thêm 14 admin endpoints:**

1. ✅ Get all orders (paginated)
2. ✅ Confirm COD order
3. ✅ Process order
4. ✅ Ship order
5. ✅ Deliver order
6. ✅ Complete order
7. ✅ Cancel order
8. ✅ Get order details
9. ✅ Get pending COD orders
10. ✅ Search orders
11. ✅ Get statistics
12. ✅ Update order status (flexible)
13. ✅ Get orders by status
14. ✅ Get total revenue

**Tất cả đều:**
- ✅ Có validation
- ✅ Có error handling
- ✅ Return consistent ApiResponse format
- ✅ Support date range filtering

---

**Author:** AI Assistant  
**Date:** 2025-12-09  
**Status:** ✅ READY FOR USE

