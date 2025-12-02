-- ============================================
-- TEST DATA FOR USER MANAGEMENT - ADMIN
-- ============================================
-- Date: 2025-12-01
-- Purpose: Insert test users, addresses, orders for testing admin user management APIs
-- Run this in HeidiSQL to populate test data

-- ============================================
-- STEP 1: Add status column if not exists
-- ============================================
-- Check and add status column (compatible with older MySQL versions)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = DATABASE() 
                   AND TABLE_NAME = 'users' 
                   AND COLUMN_NAME = 'status');

SET @query = IF(@col_exists = 0, 
    'ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT ''active''', 
    'SELECT ''Column status already exists'' AS message');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add indexes if not exist
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
                     WHERE TABLE_SCHEMA = DATABASE() 
                     AND TABLE_NAME = 'users' 
                     AND INDEX_NAME = 'idx_users_status');

SET @query = IF(@index_exists = 0, 
    'CREATE INDEX idx_users_status ON users(status)', 
    'SELECT ''Index idx_users_status already exists'' AS message');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
                     WHERE TABLE_SCHEMA = DATABASE() 
                     AND TABLE_NAME = 'users' 
                     AND INDEX_NAME = 'idx_users_role');

SET @query = IF(@index_exists = 0, 
    'CREATE INDEX idx_users_role ON users(role)', 
    'SELECT ''Index idx_users_role already exists'' AS message');
PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================
-- STEP 2: Clear existing test data
-- ============================================
-- IMPORTANT: Run this query first to check actual column names in your database:
-- SHOW COLUMNS FROM orders;
-- SHOW COLUMNS FROM order_items;

-- Delete old test data before inserting new data
-- Simple approach: Delete by username pattern only

-- Store test user IDs in temp variable (simpler approach)
SET @test_users = (SELECT GROUP_CONCAT(id) FROM users WHERE username LIKE 'testuser%' OR username LIKE 'admin_%');

-- If no test users exist yet, skip deletion
-- Otherwise, delete related data

-- Delete order_items for test users' orders
DELETE FROM order_items 
WHERE order_id IN (
    SELECT orderid FROM orders 
    WHERE customer_id IN (
        SELECT id FROM users WHERE username LIKE 'testuser%' OR username LIKE 'admin_%'
    )
);

-- Delete orders for test users
DELETE FROM orders 
WHERE customer_id IN (
    SELECT id FROM users WHERE username LIKE 'testuser%' OR username LIKE 'admin_%'
);

-- Delete addresses for test users
DELETE FROM addresses 
WHERE user_id IN (
    SELECT id FROM users WHERE username LIKE 'testuser%' OR username LIKE 'admin_%'
);

-- Delete reviews for test users (if table exists)
DELETE FROM reviews 
WHERE user_id IN (
    SELECT id FROM users WHERE username LIKE 'testuser%' OR username LIKE 'admin_%'
);

-- Finally delete test users
DELETE FROM users WHERE username LIKE 'testuser%' OR username LIKE 'admin_%';

-- ============================================
-- STEP 3: Insert Test Users (20 users)
-- ============================================

-- Admin Users (2)
INSERT INTO users (id, username, password, email, full_name, phone_number, gender, avatar_url, role, status) VALUES
(UNHEX(REPLACE(UUID(), '-', '')), 'admin_john', '$2a$10$1234567890123456789012345678901234567890123456', 'admin.john@bookstore.com', 'John Admin', '0901234567', 'Male', 'https://i.pravatar.cc/150?img=1', 'ADMIN', 'active'),
(UNHEX(REPLACE(UUID(), '-', '')), 'admin_jane', '$2a$10$1234567890123456789012345678901234567890123456', 'admin.jane@bookstore.com', 'Jane Admin', '0901234568', 'Female', 'https://i.pravatar.cc/150?img=2', 'ADMIN', 'active');

-- Active Customers (12)
INSERT INTO users (id, username, password, email, full_name, phone_number, gender, avatar_url, role, status) VALUES
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser001', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser001@example.com', 'Nguyễn Văn An', '0901111111', 'Male', 'https://i.pravatar.cc/150?img=11', 'CUSTOMER', 'active'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser002', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser002@example.com', 'Trần Thị Bình', '0902222222', 'Female', 'https://i.pravatar.cc/150?img=12', 'CUSTOMER', 'active'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser003', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser003@example.com', 'Lê Văn Cường', '0903333333', 'Male', 'https://i.pravatar.cc/150?img=13', 'CUSTOMER', 'active'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser004', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser004@example.com', 'Phạm Thị Dung', '0904444444', 'Female', 'https://i.pravatar.cc/150?img=14', 'CUSTOMER', 'active'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser005', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser005@example.com', 'Hoàng Văn Em', '0905555555', 'Male', 'https://i.pravatar.cc/150?img=15', 'CUSTOMER', 'active'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser006', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser006@example.com', 'Vũ Thị Fiona', '0906666666', 'Female', 'https://i.pravatar.cc/150?img=16', 'CUSTOMER', 'active'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser007', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser007@example.com', 'Đỗ Văn Giang', '0907777777', 'Male', 'https://i.pravatar.cc/150?img=17', 'CUSTOMER', 'active'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser008', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser008@example.com', 'Bùi Thị Hà', '0908888888', 'Female', 'https://i.pravatar.cc/150?img=18', 'CUSTOMER', 'active'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser009', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser009@example.com', 'Đinh Văn Ích', '0909999999', 'Male', 'https://i.pravatar.cc/150?img=19', 'CUSTOMER', 'active'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser010', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser010@example.com', 'Mai Thị Kim', '0910101010', 'Female', 'https://i.pravatar.cc/150?img=20', 'CUSTOMER', 'active'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser011', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser011@example.com', 'Ngô Văn Long', '0911111111', 'Male', 'https://i.pravatar.cc/150?img=21', 'CUSTOMER', 'active'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser012', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser012@example.com', 'Dương Thị Mai', '0912121212', 'Female', 'https://i.pravatar.cc/150?img=22', 'CUSTOMER', 'active');

-- Inactive Customers (3)
INSERT INTO users (id, username, password, email, full_name, phone_number, gender, avatar_url, role, status) VALUES
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser013', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser013@example.com', 'Trịnh Văn Nam', '0913131313', 'Male', 'https://i.pravatar.cc/150?img=23', 'CUSTOMER', 'inactive'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser014', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser014@example.com', 'Lý Thị Nga', '0914141414', 'Female', 'https://i.pravatar.cc/150?img=24', 'CUSTOMER', 'inactive'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser015', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser015@example.com', 'Phan Văn Ông', '0915151515', 'Male', 'https://i.pravatar.cc/150?img=25', 'CUSTOMER', 'inactive');

-- Banned Customers (3)
INSERT INTO users (id, username, password, email, full_name, phone_number, gender, avatar_url, role, status) VALUES
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser016', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser016@example.com', 'Tô Thị Phương', '0916161616', 'Female', 'https://i.pravatar.cc/150?img=26', 'CUSTOMER', 'banned'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser017', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser017@example.com', 'Hồ Văn Quân', '0917171717', 'Male', 'https://i.pravatar.cc/150?img=27', 'CUSTOMER', 'banned'),
(UNHEX(REPLACE(UUID(), '-', '')), 'testuser018', '$2a$10$1234567890123456789012345678901234567890123456', 'testuser018@example.com', 'Cao Thị Rất', '0918181818', 'Female', 'https://i.pravatar.cc/150?img=28', 'CUSTOMER', 'banned');

-- ============================================
-- STEP 4: Insert Addresses for Active Users
-- ============================================
INSERT INTO addresses (id, recipient_name, phone_number, street, ward, district, province, latitude, longitude, user_id)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    u.full_name,
    u.phone_number,
    CONCAT('Số ', FLOOR(RAND() * 100) + 1, ' Đường ', CASE FLOOR(RAND() * 5)
        WHEN 0 THEN 'Lê Lợi'
        WHEN 1 THEN 'Nguyễn Huệ'
        WHEN 2 THEN 'Trần Hưng Đạo'
        WHEN 3 THEN 'Hùng Vương'
        ELSE 'Quang Trung'
    END),
    CASE FLOOR(RAND() * 3)
        WHEN 0 THEN 'Phường Bến Nghé'
        WHEN 1 THEN 'Phường Bến Thành'
        ELSE 'Phường Nguyễn Thái Bình'
    END,
    CASE FLOOR(RAND() * 3)
        WHEN 0 THEN 'Quận 1'
        WHEN 1 THEN 'Quận 3'
        ELSE 'Quận 5'
    END,
    'TP. Hồ Chí Minh',
    10.762622 + (RAND() * 0.1 - 0.05),
    106.660172 + (RAND() * 0.1 - 0.05),
    u.id
FROM users u
WHERE u.username LIKE 'testuser%' AND u.status = 'active'
LIMIT 12;

-- ============================================
-- STEP 5: Insert Orders for Users with varied amounts
-- ============================================

-- Helper: Get book IDs (assuming books table exists)
SET @book_count = (SELECT COUNT(*) FROM books);

-- Orders for Top Spenders (testuser001-003: high value orders)
INSERT INTO orders (orderid, order_date, status, total_amount, payment_method, tax_amount, customer_id)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 90) DAY),
    CASE FLOOR(RAND() * 4)
        WHEN 0 THEN 'PENDING'
        WHEN 1 THEN 'PROCESSING'
        WHEN 2 THEN 'DELIVERED'
        ELSE 'COMPLETED'
    END,
    ROUND(500000 + (RAND() * 3000000), -3), -- 500k to 3.5M VND
    CASE FLOOR(RAND() * 3)
        WHEN 0 THEN 'COD'
        WHEN 1 THEN 'BANK_TRANSFER'
        ELSE 'VNPAY'
    END,
    0,
    u.id
FROM users u
CROSS JOIN (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) AS numbers
WHERE u.username IN ('testuser001', 'testuser002', 'testuser003')
LIMIT 15;

-- Orders for Medium Spenders (testuser004-008: medium value)
INSERT INTO orders (orderid, order_date, status, total_amount, payment_method, tax_amount, customer_id)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 60) DAY),
    CASE FLOOR(RAND() * 4)
        WHEN 0 THEN 'PENDING'
        WHEN 1 THEN 'PROCESSING'
        WHEN 2 THEN 'DELIVERED'
        ELSE 'COMPLETED'
    END,
    ROUND(200000 + (RAND() * 800000), -3), -- 200k to 1M VND
    CASE FLOOR(RAND() * 3)
        WHEN 0 THEN 'COD'
        WHEN 1 THEN 'BANK_TRANSFER'
        ELSE 'VNPAY'
    END,
    0,
    u.id
FROM users u
CROSS JOIN (SELECT 1 UNION SELECT 2 UNION SELECT 3) AS numbers
WHERE u.username IN ('testuser004', 'testuser005', 'testuser006', 'testuser007', 'testuser008')
LIMIT 15;

-- Orders for Low Spenders (testuser009-012: low value)
INSERT INTO orders (orderid, order_date, status, total_amount, payment_method, tax_amount, customer_id)
SELECT 
    UNHEX(REPLACE(UUID(), '-', '')),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY),
    CASE FLOOR(RAND() * 4)
        WHEN 0 THEN 'PENDING'
        WHEN 1 THEN 'PROCESSING'
        WHEN 2 THEN 'DELIVERED'
        ELSE 'COMPLETED'
    END,
    ROUND(50000 + (RAND() * 200000), -3), -- 50k to 250k VND
    'COD',
    0,
    u.id
FROM users u
CROSS JOIN (SELECT 1 UNION SELECT 2) AS numbers
WHERE u.username IN ('testuser009', 'testuser010', 'testuser011', 'testuser012')
LIMIT 8;

-- ============================================
-- STEP 6: Insert Order Items (if books exist)
-- ============================================
-- Note: This requires books table to have data
-- Uncomment and adjust if you have books in database

/*
INSERT INTO order_items (id, order_id, book_id, quantity, unit_price, discount_amount, subtotal)
SELECT 
    UUID(),
    o.order_id,
    (SELECT id FROM books ORDER BY RAND() LIMIT 1),
    FLOOR(RAND() * 3) + 1,
    o.total_amount / 2,
    0,
    o.total_amount / 2
FROM orders o
WHERE o.customer_id IN (SELECT id FROM users WHERE username LIKE 'testuser%')
LIMIT 50;
*/

-- ============================================
-- STEP 7: Update Statistics Summary
-- ============================================

-- Update user status distribution
UPDATE users SET status = 'active' WHERE status IS NULL;

-- View Results
SELECT 
    'Total Users' AS metric,
    COUNT(*) AS count
FROM users
WHERE username LIKE 'testuser%' OR username LIKE 'admin_%'

UNION ALL

SELECT 
    'Active Users',
    COUNT(*)
FROM users
WHERE (username LIKE 'testuser%' OR username LIKE 'admin_%') AND status = 'active'

UNION ALL

SELECT 
    'Inactive Users',
    COUNT(*)
FROM users
WHERE (username LIKE 'testuser%' OR username LIKE 'admin_%') AND status = 'inactive'

UNION ALL

SELECT 
    'Banned Users',
    COUNT(*)
FROM users
WHERE (username LIKE 'testuser%' OR username LIKE 'admin_%') AND status = 'banned'

UNION ALL

SELECT 
    'Total Orders',
    COUNT(*)
FROM orders
WHERE customer_id IN (SELECT id FROM users WHERE username LIKE 'testuser%')

UNION ALL

SELECT 
    'Total Revenue',
    ROUND(SUM(total_amount), 0)
FROM orders
WHERE customer_id IN (SELECT id FROM users WHERE username LIKE 'testuser%')

UNION ALL

SELECT 
    'Avg Order Value',
    ROUND(AVG(total_amount), 0)
FROM orders
WHERE customer_id IN (SELECT id FROM users WHERE username LIKE 'testuser%');

-- ============================================
-- STEP 8: View Top Spenders
-- ============================================
SELECT 
    u.username,
    u.full_name,
    u.email,
    u.status,
    COUNT(o.orderid) AS total_orders,
    ROUND(SUM(o.total_amount), 0) AS total_spent
FROM users u
LEFT JOIN orders o ON u.id = o.customer_id
WHERE u.username LIKE 'testuser%'
GROUP BY u.id, u.username, u.full_name, u.email, u.status
ORDER BY total_spent DESC
LIMIT 10;

-- ============================================
-- STEP 9: View Top Buyers (by order count)
-- ============================================
SELECT 
    u.username,
    u.full_name,
    u.email,
    u.status,
    COUNT(o.orderid) AS total_orders,
    ROUND(SUM(o.total_amount), 0) AS total_spent
FROM users u
LEFT JOIN orders o ON u.id = o.customer_id
WHERE u.username LIKE 'testuser%'
GROUP BY u.id, u.username, u.full_name, u.email, u.status
ORDER BY total_orders DESC
LIMIT 10;

-- ============================================
-- DONE! Test Data Inserted Successfully
-- ============================================
-- Summary:
-- - 20 test users created (2 ADMIN, 12 active, 3 inactive, 3 banned)
-- - 12 addresses created
-- - ~38 orders created with varied amounts
-- - Ready to test admin user management APIs
-- 
-- Test URLs:
-- GET  http://localhost:8080/api/admin/users
-- GET  http://localhost:8080/api/admin/users/statistics
-- GET  http://localhost:8080/api/admin/users/top-spenders?limit=10
-- GET  http://localhost:8080/api/admin/users/top-buyers?limit=10
-- ============================================
