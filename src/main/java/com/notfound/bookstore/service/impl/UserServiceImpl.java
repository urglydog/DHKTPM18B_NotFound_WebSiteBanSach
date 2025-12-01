package com.notfound.bookstore.service.impl;

import com.notfound.bookstore.exception.AppException;
import com.notfound.bookstore.exception.ErrorCode;
import com.notfound.bookstore.model.dto.request.userrequest.CreateUserRequest;
import com.notfound.bookstore.model.dto.request.userrequest.UpdateUserRequest;
import com.notfound.bookstore.model.dto.request.userrequest.UserFilterRequest;
import com.notfound.bookstore.model.dto.response.userresponse.UserDetailResponse;
import com.notfound.bookstore.model.dto.response.userresponse.UserManagementResponse;
import com.notfound.bookstore.model.dto.response.userresponse.UserResponse;
import com.notfound.bookstore.model.dto.response.userresponse.UserStatsResponse;
import com.notfound.bookstore.model.entity.Order;
import com.notfound.bookstore.model.entity.User;
import com.notfound.bookstore.model.enums.Role;
import com.notfound.bookstore.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements com.notfound.bookstore.service.UserService {

    PasswordEncoder passwordEncoder;
    UserRepository userRepository;

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void resetPassword(String email, String newPassword){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ===== CRUD OPERATIONS =====

    @Override
    @Transactional(readOnly = true)
    public Page<UserManagementResponse> getAllUsers(UserFilterRequest filterRequest) {
        log.info("Getting all users with filters: {}", filterRequest);

        // Tạo Pageable
        int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
        int size = filterRequest.getSize() != null ? filterRequest.getSize() : 10;
        
        String sortBy = filterRequest.getSortBy() != null ? filterRequest.getSortBy() : "username";
        String sortDirection = filterRequest.getSortDirection() != null ? filterRequest.getSortDirection() : "asc";
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);

        // Parse role
        Role role = null;
        if (filterRequest.getRole() != null && !filterRequest.getRole().isEmpty()) {
            try {
                role = Role.valueOf(filterRequest.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role: {}", filterRequest.getRole());
            }
        }

        // Lấy users theo filter
        Page<User> userPage = userRepository.findByFilters(
            filterRequest.getSearch(),
            role,
            filterRequest.getStatus(),
            pageable
        );

        // Map to DTO
        return userPage.map(this::mapToUserManagementResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(UUID id) {
        log.info("Getting user detail by id: {}", id);
        
        User user = userRepository.findByIdWithOrders(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return mapToUserDetailResponse(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user: {}", request.getUsername());

        // Validate username không trùng
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Validate email không trùng
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Create user entity
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .gender(request.getGender())
                .avatar_url(request.getAvatarUrl())
                .role(request.getRole() != null ? Role.valueOf(request.getRole()) : Role.CUSTOMER)
                .status("active") // Default status
                .build();

        User savedUser = userRepository.save(user);
        log.info("Created user successfully: {}", savedUser.getId());

        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        log.info("Updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Update fields
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Check email không trùng
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AppException(ErrorCode.USER_EXISTED);
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        User updatedUser = userRepository.save(user);
        log.info("Updated user successfully: {}", id);

        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra user có đơn hàng không
        if (userRepository.hasOrders(id)) {
            throw new AppException(ErrorCode.USER_HAS_ORDERS);
        }

        // Không cho xóa ADMIN
        if (user.getRole() == Role.ADMIN) {
            throw new AppException(ErrorCode.CANNOT_DELETE_ADMIN);
        }

        userRepository.delete(user);
        log.info("Deleted user successfully: {}", id);
    }

    // ===== STATUS MANAGEMENT =====

    @Override
    @Transactional
    public UserResponse updateUserStatus(UUID id, String status) {
        log.info("Updating user status: {} to {}", id, status);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Validate status
        if (!status.matches("^(active|inactive|banned)$")) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        user.setStatus(status);
        User updatedUser = userRepository.save(user);

        log.info("Updated user status successfully: {}", id);
        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse banUser(UUID id) {
        log.info("Banning user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Không cho ban ADMIN
        if (user.getRole() == Role.ADMIN) {
            throw new AppException(ErrorCode.CANNOT_BAN_ADMIN);
        }

        user.setStatus("banned");
        User bannedUser = userRepository.save(user);

        log.info("Banned user successfully: {}", id);
        return mapToUserResponse(bannedUser);
    }

    @Override
    @Transactional
    public UserResponse unbanUser(UUID id) {
        log.info("Unbanning user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setStatus("active");
        User unbannedUser = userRepository.save(user);

        log.info("Unbanned user successfully: {}", id);
        return mapToUserResponse(unbannedUser);
    }

    // ===== STATISTICS =====

    @Override
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStatistics() {
        log.info("Getting user statistics");

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus("active");
        long inactiveUsers = userRepository.countByStatus("inactive");
        long bannedUsers = userRepository.countByStatus("banned");

        long totalAdmins = userRepository.countByRole(Role.ADMIN);
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
        long totalGuests = userRepository.countByRole(Role.GUEST);

        // Tính thống kê doanh thu
        List<User> allUsers = userRepository.findAll();
        BigDecimal totalRevenue = allUsers.stream()
                .flatMap(u -> u.getOrders() != null ? u.getOrders().stream() : java.util.stream.Stream.empty())
                .map(Order::getTotalAmount)
                .map(amount -> amount != null ? BigDecimal.valueOf(amount) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = allUsers.stream()
                .mapToLong(u -> u.getOrders() != null ? u.getOrders().size() : 0)
                .sum();

        BigDecimal avgRevenuePerUser = totalUsers > 0 
            ? totalRevenue.divide(BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal avgOrderValue = totalOrders > 0
            ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        // Get top users
        List<UserStatsResponse.TopUserResponse> topSpenders = getTopSpenders(5);
        List<UserStatsResponse.TopUserResponse> topBuyers = getTopBuyers(5);

        return UserStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .bannedUsers(bannedUsers)
                .totalAdmins(totalAdmins)
                .totalCustomers(totalCustomers)
                .totalGuests(totalGuests)
                .newUsersThisMonth(0L) // TODO: Implement
                .newUsersThisWeek(0L)  // TODO: Implement
                .newUsersToday(0L)     // TODO: Implement
                .totalRevenue(totalRevenue)
                .avgRevenuePerUser(avgRevenuePerUser)
                .avgOrderValue(avgOrderValue)
                .totalOrders(totalOrders)
                .topSpenders(topSpenders)
                .topBuyers(topBuyers)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStatsResponse.TopUserResponse> getTopSpenders(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<User> topUsers = userRepository.findTopSpenders(pageable);

        return topUsers.stream()
                .map(this::mapToTopUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStatsResponse.TopUserResponse> getTopBuyers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<User> topUsers = userRepository.findTopBuyers(pageable);

        return topUsers.stream()
                .map(this::mapToTopUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getNewUsers(int days) {
        // TODO: Implement với LocalDateTime
        return List.of();
    }

    @Override
    public byte[] exportUsersToExcel() {
        // TODO: Implement Excel export using Apache POI
        throw new UnsupportedOperationException("Export to Excel not implemented yet");
    }

    // ===== MAPPING METHODS =====

    private UserManagementResponse mapToUserManagementResponse(User user) {
        int totalOrders = user.getOrders() != null ? user.getOrders().size() : 0;
        BigDecimal totalSpent = user.getOrders() != null 
            ? user.getOrders().stream()
                .map(Order::getTotalAmount)
                .map(amount -> amount != null ? BigDecimal.valueOf(amount) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
            : BigDecimal.ZERO;

        return UserManagementResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatar_url())
                .role(user.getRole().name())
                .status(user.getStatus() != null ? user.getStatus() : "active")
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .build();
    }

    private UserDetailResponse mapToUserDetailResponse(User user) {
        int totalOrders = user.getOrders() != null ? user.getOrders().size() : 0;
        int totalReviews = user.getReviews() != null ? user.getReviews().size() : 0;
        BigDecimal totalSpent = user.getOrders() != null 
            ? user.getOrders().stream()
                .map(Order::getTotalAmount)
                .map(amount -> amount != null ? BigDecimal.valueOf(amount) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
            : BigDecimal.ZERO;

        LocalDateTime lastOrderDate = user.getOrders() != null && !user.getOrders().isEmpty()
            ? user.getOrders().stream()
                .map(Order::getOrderDate)
                .max(LocalDateTime::compareTo)
                .orElse(null)
            : null;

        return UserDetailResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .avatarUrl(user.getAvatar_url())
                .role(user.getRole().name())
                .status(user.getStatus() != null ? user.getStatus() : "active")
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .totalReviews(totalReviews)
                .lastOrderDate(lastOrderDate)
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .build();
    }

    private UserStatsResponse.TopUserResponse mapToTopUserResponse(User user) {
        int totalOrders = user.getOrders() != null ? user.getOrders().size() : 0;
        BigDecimal totalSpent = user.getOrders() != null 
            ? user.getOrders().stream()
                .map(Order::getTotalAmount)
                .map(amount -> amount != null ? BigDecimal.valueOf(amount) : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
            : BigDecimal.ZERO;

        return UserStatsResponse.TopUserResponse.builder()
                .userId(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatar_url())
                .totalOrders(totalOrders)
                .totalSpent(totalSpent)
                .build();
    }
}
