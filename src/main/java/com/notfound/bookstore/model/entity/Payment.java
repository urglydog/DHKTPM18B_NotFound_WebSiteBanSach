package com.notfound.bookstore.model.entity;

import com.notfound.bookstore.model.enums.PaymentStatus;
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
@Builder
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

    String transactionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    public Payment(String paymentMethod, Double amount, Order order) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.order = order;
        this.status = PaymentStatus.PENDING;
    }
}
