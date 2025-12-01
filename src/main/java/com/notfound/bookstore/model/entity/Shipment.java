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

    // --- THÔNG TIN GHN (Giao Hàng Nhanh) ---

    @Column(name = "carrier", nullable = false)
    String carrier = "GHN"; // Mặc định là GHN

    @Column(name = "ghn_order_code", unique = true)
    String ghnOrderCode; // Mã đơn hàng phía GHN (vd: L8CC2...) dùng để in bill

    @Column(name = "ghn_service_type_id")
    Integer serviceTypeId; // Loại dịch vụ: 1 (Bay), 2 (Bộ), 5 (Thường)...

    @Column(name = "ghn_total_fee")
    Double ghnTotalFee; // Phí ship thực tế GHN thu của Shop

    @Column(name = "expected_delivery_time")
    LocalDateTime expectedDeliveryTime; // Thời gian dự kiến giao (GHN trả về)

    @Column(name = "sorting_code")
    String sortingCode; // Mã phân loại hàng (vd: 10-A-01) để in lên tem

    // --- THÔNG TIN BÊN GỬI (Shop) ---

    @Column(name = "from_name")
    String fromName; // Tên shop

    @Column(name = "from_phone")
    String fromPhone; // SĐT shop

    @Column(name = "from_address")
    String fromAddress; // Địa chỉ lấy hàng

    @Column(name = "from_ward_code")
    String fromWardCode; // Mã phường shop

    @Column(name = "from_district_id")
    Integer fromDistrictId; // Mã quận shop

    // --- THÔNG TIN BÊN NHẬN (Customer) - Đã lưu snapshot ---

    @Column(name = "to_name")
    String toName; // Tên người nhận

    @Column(name = "to_phone")
    String toPhone; // SĐT người nhận

    @Column(name = "to_address")
    String toAddress; // Địa chỉ đầy đủ

    @Column(name = "to_ward_code")
    String toWardCode; // Mã phường người nhận

    @Column(name = "to_district_id")
    Integer toDistrictId; // Mã quận người nhận

    // --- TRẠNG THÁI & TRACKING ---

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ShipmentStatus status = ShipmentStatus.PENDING; // PENDING, READY_TO_PICK, PICKING, DELIVERING, DELIVERED, RETURNED, CANCELLED

    @Column(name = "cod_amount")
    Double codAmount; // Số tiền GHN cần thu hộ (COD)

    @Column(name = "note", columnDefinition = "TEXT")
    String note; // Ghi chú giao hàng

    @Column(name = "weight")
    Integer weight; // Khối lượng (gram)

    @Column(name = "length")
    Integer length; // Chiều dài (cm)

    @Column(name = "width")
    Integer width; // Chiều rộng (cm)

    @Column(name = "height")
    Integer height; // Chiều cao (cm)

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
