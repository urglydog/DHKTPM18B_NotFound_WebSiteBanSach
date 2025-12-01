package com.notfound.bookstore.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"applicableBooks", "applicableCategories"})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Promotion {

    @Id
    @UuidGenerator
    UUID promotionID;

    @Column(nullable = false)
    String name;

    @Column(unique = true, nullable = false)
    String code;

    @Column(name = "usage_count", nullable = false)
    Integer usageCount = 0;

    @Column(name = "usage_limit", nullable = false)
    Integer usageLimit = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    DiscountType discountType; // PERCENTAGE hoặc FIXED_AMOUNT

    @Column(name = "discount_value", nullable = false)
    Double discountValue; // Lưu % hoặc số tiền tùy vào discountType

    @Column(name = "discount_percent", nullable = false)
    Double discountPercent;

    @Column(name = "max_discount_amount")
    Double maxDiscountAmount; // Chỉ dùng khi discountType = PERCENTAGE

    @Column(name = "min_order_value", nullable = false)
    Double minOrderValue = 0.0; // Đơn hàng phải đạt mức này mới được dùng

    @Column(name = "start_date", nullable = false)
    LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status;

    @Column(name = "created_by")
    String createdBy; // Username của admin tạo promotion

    @Column(name = "updated_by")
    String updatedBy; // Username của admin cập nhật cuối cùng

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "promotion_books",
        joinColumns = @JoinColumn(name = "promotion_id"),
        inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    List<Book> applicableBooks;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "promotion_categories",
        joinColumns = @JoinColumn(name = "promotion_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    List<Category> applicableCategories;

    public enum Status {
        ACTIVE, INACTIVE, EXPIRED
    }

    public enum DiscountType {
        PERCENTAGE,     // Giảm theo % (ví dụ: 10%)
        FIXED_AMOUNT    // Giảm tiền mặt (ví dụ: 50.000đ)
    }

    public Promotion(String name, Double discountPercent, LocalDate startDate, LocalDate endDate, String description) {
        this.name = name;
        this.discountPercent = discountPercent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.status = Status.ACTIVE;
    }

    public void incrementUsageCount() {
        this.usageCount++;
    }

    public boolean isValid() {
        LocalDate now = LocalDate.now();
        return status == Status.ACTIVE
                && !now.isBefore(startDate)
                && !now.isAfter(endDate)
                && usageCount < usageLimit;
    }

    public boolean isValid(Double orderTotal) {
        LocalDate now = LocalDate.now();
        return status == Status.ACTIVE
                && !now.isBefore(startDate)
                && !now.isAfter(endDate)
                && usageCount < usageLimit
                && orderTotal >= minOrderValue; // Check thêm điều kiện này
    }
}
