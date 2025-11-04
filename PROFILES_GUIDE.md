# Hướng dẫn sử dụng Spring Profiles

## 📁 Cấu trúc file

```
src/main/resources/
├── application.yml              # Config chung + profile mặc định
├── application-develop.yml      # Config cho Development (Local)
└── application-review.yml       # Config cho Review/Staging (AWS)
```

## 🔄 Các Profiles

### 1. **develop** (Development - Local)
- **Database**: MySQL localhost (`localhost:3306`)
- **Username**: `root`
- **Password**: `root`
- **ddl-auto**: `update` (không xóa dữ liệu khi restart)
- **Dùng cho**: Development local

### 2. **review** (Review/Staging - AWS)
- **Database**: AWS MySQL (`13.54.2.223:3306`)
- **Username**: `admin`
- **Password**: `bookstore_aws_not_found`
- **ddl-auto**: `update`
- **Dùng cho**: Testing trên môi trường AWS

## 🚀 Cách chuyển đổi Profile

### Cách 1: Sửa file `application.yml`
```yaml
spring:
  profiles:
    active: review  # Đổi từ "develop" sang "review"
```

### Cách 2: Dùng biến môi trường

**Windows PowerShell:**
```powershell
$env:SPRING_PROFILES_ACTIVE="review"
```

**Windows CMD:**
```cmd
set SPRING_PROFILES_ACTIVE=review
```

**Linux/Mac:**
```bash
export SPRING_PROFILES_ACTIVE=review
```

### Cách 3: Dùng IntelliJ IDEA

1. Mở **Run/Debug Configurations**
2. Chọn configuration của bạn
3. Trong tab **Configuration**:
   - Tìm **Environment variables**
   - Thêm: `SPRING_PROFILES_ACTIVE=review`
   - Hoặc trong **Active profiles**: nhập `review`

### Cách 4: Dùng Maven
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=review
```

### Cách 5: Dùng VM Options
Trong IntelliJ Run Configuration, thêm vào **VM options**:
```
-Dspring.profiles.active=review
```

## 📊 So sánh các Profiles

| Thành phần | develop | review |
|------------|---------|--------|
| **Database Host** | localhost:3306 | 13.54.2.223:3306 |
| **Database User** | root | admin |
| **Database Password** | root | bookstore_aws_not_found |
| **Hibernate Dialect** | MySQLDialect | MySQL8Dialect |
| **ddl-auto** | update | update |
| **VNPay** | Có (sandbox) | Có (sandbox) |

## 🔍 Kiểm tra Profile đang dùng

Khi application chạy, bạn sẽ thấy trong console:
```
The following profiles are active: develop
```
hoặc
```
The following profiles are active: review
```

## ⚠️ Lưu ý

1. **Profile mặc định**: `develop` (được set trong `application.yml`)
2. **Config chung**: JWT, Cloudinary trong `application.yml` dùng chung cho tất cả profiles
3. **Config riêng**: Database, Mail, Redis trong từng profile file riêng
4. **Bảo mật**: Không commit các file này lên Git nếu chứa thông tin nhạy cảm (dùng `.gitignore`)

## 📝 Ví dụ sử dụng

### Development (Local)
```yaml
# application.yml
spring:
  profiles:
    active: develop
```
→ Kết nối database localhost

### Review/Staging (AWS)
```yaml
# application.yml
spring:
  profiles:
    active: review
```
→ Kết nối database AWS

---

**Tạo bởi**: Auto Assistant  
**Ngày**: 2025-11-05

