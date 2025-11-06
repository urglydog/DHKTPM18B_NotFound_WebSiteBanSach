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
@ToString(exclude = "applicableBooks")
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

    @Column(name = "discount_percent", nullable = false)
    Double discountPercent;

    @Column(name = "start_date", nullable = false)
    LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "promotion_books",
        joinColumns = @JoinColumn(name = "promotion_id"),
        inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    List<Book> applicableBooks;

    public enum Status {
        ACTIVE, INACTIVE, EXPIRED
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
}
