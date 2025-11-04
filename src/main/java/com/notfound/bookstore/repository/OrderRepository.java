package com.notfound.bookstore.repository;

import com.notfound.bookstore.model.entity.CartItem;
import com.notfound.bookstore.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    // Tìm orders theo customer
    List<Order> findByCustomerId(UUID customerId);

    // Tìm orders theo status
    List<Order> findByStatus(Order.OrderStatus status);

    // Tìm orders theo customer và status
    List<Order> findByCustomerIdAndStatus(UUID customerId, Order.OrderStatus status);

    // Tính tổng tiền theo customer
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.customer.id = :customerId")
    Double getTotalAmountByCustomer(@Param("customerId") UUID customerId);

    // Đếm số orders theo customer
    Long countByCustomerId(UUID customerId);
}
