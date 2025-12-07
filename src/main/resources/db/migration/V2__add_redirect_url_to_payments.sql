-- Migration: Add redirect_url column to payments table
-- Author: System
-- Date: 2024-12-08
-- Description: Add redirect_url field to store Frontend redirect URL after payment

ALTER TABLE payments
ADD COLUMN redirect_url VARCHAR(500) NULL
COMMENT 'URL để redirect về Frontend sau khi thanh toán';

