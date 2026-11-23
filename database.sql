
-- =============================================
-- GROCERY MANAGEMENT SYSTEM
-- DATABASE INITIALIZATION
-- =============================================

CREATE DATABASE IF NOT EXISTS grocery_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE grocery_management;


-- =============================================
-- 1. USERS TABLE
-- =============================================

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'STAFF') NOT NULL DEFAULT 'STAFF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;


-- =============================================
-- 2. GROCERY ITEMS TABLE
-- =============================================

CREATE TABLE IF NOT EXISTS grocery_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    unit VARCHAR(20) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    min_stock INT NOT NULL DEFAULT 5,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_item_quantity
        CHECK (quantity >= 0),

    CONSTRAINT chk_item_price
        CHECK (price >= 0),

    CONSTRAINT chk_item_min_stock
        CHECK (min_stock >= 0),

    INDEX idx_item_name (item_name),
    INDEX idx_item_category (category)

) ENGINE=InnoDB;


-- =============================================
-- 3. STOCK TRANSACTIONS TABLE
-- =============================================

CREATE TABLE IF NOT EXISTS stock_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    user_id INT NOT NULL,

    transaction_type ENUM('STOCK_IN', 'STOCK_OUT') NOT NULL,
    quantity INT NOT NULL,
    notes VARCHAR(255),

    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_transaction_quantity
        CHECK (quantity > 0),

    CONSTRAINT fk_transaction_item
        FOREIGN KEY (item_id)
        REFERENCES grocery_items(item_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_transaction_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    INDEX idx_transaction_date (transaction_date),
    INDEX idx_transaction_item (item_id)

) ENGINE=InnoDB;
