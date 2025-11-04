package com.notfound.bookstore.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "order")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {

    @Id
    @UuidGenerator
    UUID paymentID;

    @Column(name = "payment_method", nullable = false)
    String paymentMethod;

    @Column(nullable = false)
    Double amount;

    @CreationTimestamp
    @Column(name = "payment_date", nullable = false)
    LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    public enum PaymentStatus {
        COMPLETED, INCOMPLETED, PENDING, FAILED, REFUNDED
    }

    public Payment(String paymentMethod, Double amount, Order order) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.order = order;
        this.status = PaymentStatus.PENDING;
    }
}
