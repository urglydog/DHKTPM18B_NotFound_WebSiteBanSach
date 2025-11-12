# Hướng dẫn sử dụng Admin API

## Tổng quan

- **Base URL**: `http://localhost:8080/api/admin`
- **Yêu cầu**: JWT token với quyền `ADMIN`
- **Header**: `Authorization: Bearer <token>`

## 1. Quản lý Sách (Books)

### 1.1. Tạo sách mới
**POST** `/api/admin/books`
```json
{
  "title": "Tên sách",
  "isbn": "978-0-123456-78-9",
  "price": 150000,
  "discountPrice": 120000,
  "stockQuantity": 100,
  "publishDate": "2024-01-01",
  "description": "Mô tả",
  "status": "AVAILABLE",
  "authorIds": ["uuid-author"],
  "categoryIds": ["uuid-category"]
}
```

### 1.2. Cập nhật sách
**PUT** `/api/admin/books/{bookId}`

Body (các trường optional):
```json
{
  "title": "Tên mới",
  "price": 160000,
  "stockQuantity": 50,
  "status": "OUT_OF_STOCK",
  "authorIds": [],
  "categoryIds": []
}
```

### 1.3. Xóa sách
**DELETE** `/api/admin/books/{bookId}`

### 1.4. Xem chi tiết sách
**GET** `/api/admin/books/{bookId}`

### 1.5. Danh sách sách (phân trang)
**GET** `/api/admin/books?page=0&size=10`

### 1.6. Upload ảnh cho sách
**POST** `/api/admin/books/{bookId}/images`
- Form-data: `images` (có thể gửi nhiều file)

### 1.7. Xóa ảnh
**DELETE** `/api/admin/books/{bookId}/images/{imageId}`

## 2. Quản lý Thể loại (Categories)

### 2.1. Tạo thể loại
**POST** `/api/admin/categories`
```json
{
  "name": "Tiểu thuyết",
  "description": "Mô tả"
}
```

### 2.2. Cập nhật thể loại
**PUT** `/api/admin/categories/{categoryId}`
```json
{
  "name": "Tên mới",
  "description": "Mô tả mới",
  "parentCategoryId": "uuid-parent"
}
```

### 2.3. Xóa thể loại
**DELETE** `/api/admin/categories/{categoryId}`

### 2.4. Xem chi tiết thể loại
**GET** `/api/admin/categories/{categoryId}`

### 2.5. Danh sách tất cả thể loại
**GET** `/api/admin/categories`

### 2.6. Danh sách thể loại (phân trang)
**GET** `/api/admin/categories/paged?page=0&size=10`

## 3. Ví dụ cURL

### Đăng nhập
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

### Tạo sách
```bash
curl -X POST http://localhost:8080/api/admin/books \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Sách mới","price":150000,"stockQuantity":100}'
```

### Upload ảnh
```bash
curl -X POST http://localhost:8080/api/admin/books/<id>/images \
  -H "Authorization: Bearer <token>" \
  -F "images=@cover.jpg"
```

## 4. Lưu ý

- Cloudinary đã cấu hình sẵn (`djla3uhz2`, `899262967322476`, secret `5wYVX4l_ldTPDRAUORYAIyxk__I`).
- ISBN và tên thể loại phải unique.
- Ảnh được lưu trong folder `bookstore/books` trên Cloudinary.
- Khi xóa sách, hệ thống xóa luôn ảnh trên Cloudinary.
- `Book.Status`: `AVAILABLE`, `OUT_OF_STOCK`, `DISCONTINUED`.
