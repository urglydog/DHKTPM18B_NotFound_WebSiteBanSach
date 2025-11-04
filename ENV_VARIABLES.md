# Hướng dẫn cấu hình Biến Môi Trường

## 📋 Tổng quan

File `application-review.yml` sử dụng **biến môi trường** để bảo mật thông tin nhạy cảm như database password, API keys, etc.

## 🔧 Các biến môi trường cần thiết

### Database (cho profile review)
```bash
DB_HOST=your-database-host          # Ví dụ: your-database-host.com
DB_PORT=3306                        # Port MySQL (mặc định: 3306)
DB_NAME=bookstore_db                # Tên database
DB_USERNAME=your-database-username  # Username database
DB_PASSWORD=your-database-password  # Password database (NHẠY CẢM)
```

### Redis
```bash
REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_USERNAME=your-redis-username
REDIS_PASSWORD=your-redis-password
```

### Mail (Gmail SMTP)
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password     # App Password từ Gmail
```

### JWT
```bash
JWT_SIGNER_KEY=your-jwt-signer-key-here
```

### VNPay Payment
```bash
VNPAY_TMN_CODE=your-vnpay-tmn-code
VNPAY_SECRET_KEY=your-vnpay-secret-key
```

## 🚀 Cách cấu hình

### Cách 1: Tạo file `.env` (Khuyến nghị)

1. Tạo file `.env` trong thư mục root của project
2. Copy nội dung từ file này và điền giá trị thực tế
3. File `.env` đã được thêm vào `.gitignore` nên không bị commit lên Git

**Lưu ý**: Spring Boot không tự đọc file `.env`. Cần dùng thư viện như `dotenv-java` hoặc set trong IntelliJ Run Configuration.

### Cách 2: Set trong IntelliJ IDEA Run Configuration

1. Mở **Run/Debug Configurations**
2. Chọn configuration của bạn
3. Trong tab **Configuration**:
   - Tìm **Environment variables**
   - Thêm từng biến môi trường:
     ```
     DB_HOST=your-host
     DB_USERNAME=your-username
     DB_PASSWORD=your-password
     ...
     ```

### Cách 3: Set trong Terminal (Windows PowerShell)

```powershell
$env:DB_HOST="your-host"
$env:DB_USERNAME="your-username"
$env:DB_PASSWORD="your-password"
$env:DB_PORT="3306"
$env:DB_NAME="bookstore_db"
# ... các biến khác
```

### Cách 4: Set trong Terminal (Linux/Mac)

```bash
export DB_HOST=your-host
export DB_USERNAME=your-username
export DB_PASSWORD=your-password
export DB_PORT=3306
export DB_NAME=bookstore_db
# ... các biến khác
```

## ⚠️ Lưu ý Bảo mật

1. **KHÔNG commit file `.env` lên Git** - đã được thêm vào `.gitignore`
2. **KHÔNG chia sẻ file `.env`** với người khác qua email/message
3. **Sử dụng App Password** cho Gmail thay vì password chính
4. **Rotate keys** định kỳ nếu có nghi ngờ bị lộ

## 📝 Ví dụ file `.env`

```bash
# Database
DB_HOST=your-database-host
DB_PORT=3306
DB_NAME=bookstore_db
DB_USERNAME=admin
DB_PASSWORD=your-secure-password-here

# Redis
REDIS_HOST=redis-15365.crce219.us-east-1-4.ec2.redns.redis-cloud.com
REDIS_PORT=15365
REDIS_USERNAME=default
REDIS_PASSWORD=your-redis-password

# Mail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# JWT
JWT_SIGNER_KEY=your-64-character-hex-key

# VNPay
VNPAY_TMN_CODE=your-tmn-code
VNPAY_SECRET_KEY=your-secret-key
```

## 🔍 Kiểm tra biến môi trường

Khi chạy ứng dụng với profile `review`, nếu thiếu biến môi trường, bạn sẽ thấy lỗi:
```
Could not resolve placeholder 'DB_PASSWORD' in value "${DB_PASSWORD}"
```

Đảm bảo đã set đầy đủ các biến môi trường cần thiết trước khi chạy.

