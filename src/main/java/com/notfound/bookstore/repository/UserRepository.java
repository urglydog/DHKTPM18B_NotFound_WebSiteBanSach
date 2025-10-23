package com.notfound.bookstore.repository;

import com.notfound.bookstore.model.entity.Book;
import com.notfound.bookstore.model.entity.User;
import com.notfound.bookstore.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Đăng nhập
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // Kiểm tra trùng lặp
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Admin: Quản lý user
    Page<User> findByRole(Role role, Pageable pageable);

    // Tìm kiếm user
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email, Pageable pageable);

    // Kiểm tra user đã từng đặt hàng chưa (để xóa)
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o WHERE o.customer.id = :userId")
    boolean hasOrders(@Param("userId") UUID userId);

}
