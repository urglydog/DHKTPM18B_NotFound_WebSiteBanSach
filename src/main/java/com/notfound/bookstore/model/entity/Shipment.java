package com.notfound.bookstore.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(exclude = "order")
public class Shipment {

    @Id
    @UuidGenerator
    UUID shipmentID;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @Column(name = "carrier", nullable = false)
    String carrier = "GHN";

    @Column(name = "ghn_order_code", unique = true)
    String ghnOrderCode;

    @Column(name = "ghn_service_type_id")
    Integer serviceTypeId;

    @Column(name = "ghn_total_fee")
    Double ghnTotalFee;

    @Column(name = "expected_delivery_time")
    LocalDateTime expectedDeliveryTime;

    @Column(name = "sorting_code")
    String sortingCode;

    @Column(name = "from_name")
    String fromName;

    @Column(name = "from_phone")
    String fromPhone;

    @Column(name = "from_address")
    String fromAddress;

    @Column(name = "from_ward_code")
    String fromWardCode;

    @Column(name = "from_district_id")
    Integer fromDistrictId;

    @Column(name = "to_name")
    String toName;

    @Column(name = "to_phone")
    String toPhone;

    @Column(name = "to_address")
    String toAddress;

    @Column(name = "to_ward_code")
    String toWardCode;

    @Column(name = "to_district_id")
    Integer toDistrictId;

    // --- TRẠNG THÁI & TRACKING ---

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ShipmentStatus status = ShipmentStatus.PENDING; // PENDING, READY_TO_PICK, PICKING, DELIVERING, DELIVERED, RETURNED, CANCELLED

    @Column(name = "cod_amount")
    Double codAmount; // Số tiền GHN cần thu hộ (COD)

    @Column(name = "note", columnDefinition = "TEXT")
    String note; // Ghi chú giao hàng

    @Column(name = "weight")
    Integer weight;

    @Column(name = "length")
    Integer length;

    @Column(name = "width")
    Integer width;

    @Column(name = "height")
    Integer height;

    @Column(name = "insurance_value")
    Double insuranceValue; // Giá trị hàng hóa để bảo hiểm

    // --- TRACKING INFORMATION ---

    @Column(name = "tracking_number")
    String trackingNumber; // Mã vận đơn để khách tra cứu

    @Column(name = "picked_at")
    LocalDateTime pickedAt; // Thời gian lấy hàng

    @Column(name = "delivered_at")
    LocalDateTime deliveredAt; // Thời gian giao hàng thành công

    @Column(name = "returned_at")
    LocalDateTime returnedAt; // Thời gian hoàn hàng

    @Column(name = "cancelled_at")
    LocalDateTime cancelledAt; // Thời gian hủy đơn

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    String failureReason; // Lý do giao hàng thất bại

    // --- AUDIT ---

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    // Enum định nghĩa trạng thái vận chuyển
    public enum ShipmentStatus {
        PENDING,        // Chờ tạo đơn GHN
        READY_TO_PICK,  // Chờ lấy hàng
        PICKING,        // Đang lấy hàng
        PICKED,         // Đã lấy hàng
        STORING,        // Nhập kho
        DELIVERING,     // Đang giao
        DELIVERED,      // Đã giao thành công
        RETURNED,       // Hoàn hàng
        CANCELLED,      // Đã hủy
        EXCEPTION       // Gặp sự cố
    }
}
