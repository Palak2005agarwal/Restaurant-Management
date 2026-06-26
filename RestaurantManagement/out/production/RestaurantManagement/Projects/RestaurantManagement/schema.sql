-- ==========================================
-- DDL Script for Restaurant Management System
-- Database: restaurant_db
-- ==========================================

CREATE DATABASE IF NOT EXISTS restaurant_db;
USE restaurant_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS Users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'WAITER', 'CHEF') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Customers Table
CREATE TABLE IF NOT EXISTS Customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL UNIQUE,
    loyalty_points INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Tables Table
CREATE TABLE IF NOT EXISTS Tables (
    id INT AUTO_INCREMENT PRIMARY KEY,
    table_number INT NOT NULL UNIQUE,
    capacity INT NOT NULL,
    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED') DEFAULT 'AVAILABLE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Menu Items Table
CREATE TABLE IF NOT EXISTS Menu_Items (
    id INT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Inventory Table
CREATE TABLE IF NOT EXISTS Inventory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ingredient_name VARCHAR(100) NOT NULL UNIQUE,
    quantity DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    unit VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Menu Item Ingredients Table (Recipes)
CREATE TABLE IF NOT EXISTS Menu_Item_Ingredients (
    menu_item_id INT NOT NULL,
    inventory_id INT NOT NULL,
    quantity_needed DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (menu_item_id, inventory_id),
    FOREIGN KEY (menu_item_id) REFERENCES Menu_Items(id) ON DELETE CASCADE,
    FOREIGN KEY (inventory_id) REFERENCES Inventory(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Orders Table
CREATE TABLE IF NOT EXISTS Orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NULL,
    table_id INT NOT NULL,
    served_by_user_id INT NOT NULL,
    order_status ENUM('PENDING', 'COOKING', 'READY', 'PAID', 'CANCELLED') DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) DEFAULT 0.00,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customers(id) ON DELETE SET NULL,
    FOREIGN KEY (table_id) REFERENCES Tables(id),
    FOREIGN KEY (served_by_user_id) REFERENCES Users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Order Items Table
CREATE TABLE IF NOT EXISTS Order_Items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT NOT NULL,
    special_instructions VARCHAR(255) DEFAULT 'None',
    FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES Menu_Items(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- Seed Sample Data
-- ==========================================

-- Seed Users (Passwords are plaintext for demonstration in this console environment)
INSERT INTO Users (username, password_hash, role) VALUES
('waiter1', 'waiter123', 'WAITER'),
('chef1', 'chef123', 'CHEF'),
('admin1', 'admin123', 'ADMIN')
ON DUPLICATE KEY UPDATE role=role;

-- Seed Tables
INSERT INTO Tables (table_number, capacity, status) VALUES
(1, 2, 'AVAILABLE'),
(2, 2, 'AVAILABLE'),
(3, 4, 'AVAILABLE'),
(4, 4, 'AVAILABLE'),
(5, 6, 'AVAILABLE'),
(6, 6, 'AVAILABLE'),
(7, 8, 'AVAILABLE'),
(8, 8, 'AVAILABLE'),
(9, 10, 'AVAILABLE'),
(10, 12, 'AVAILABLE')
ON DUPLICATE KEY UPDATE capacity=capacity;

-- Seed Customers
INSERT INTO Customers (name, phone, loyalty_points) VALUES
('Rajesh Kumar', '9876543210', 150),
('Sneha Sharma', '8765432109', 50),
('Amit Patel', '7654321098', 0)
ON DUPLICATE KEY UPDATE loyalty_points=loyalty_points;

-- Seed Menu Items
INSERT INTO Menu_Items (id, item_name, price, category, is_available) VALUES
(101, 'Paneer Tikka', 240.00, 'Starters', TRUE),
(102, 'Spring Rolls', 180.00, 'Starters', TRUE),
(201, 'Dal Makhani', 260.00, 'Main Course', TRUE),
(202, 'Butter Roti', 30.00, 'Main Course', TRUE),
(203, 'Kadhai Paneer', 290.00, 'Main Course', TRUE),
(301, 'Choco Lava Cake', 150.00, 'Desserts', TRUE),
(302, 'Iced Americano', 120.00, 'Beverages', TRUE)
ON DUPLICATE KEY UPDATE price=price, is_available=is_available;

-- Seed Inventory Ingredients
INSERT INTO Inventory (id, ingredient_name, quantity, unit) VALUES
(1, 'Paneer', 5000.00, 'g'),
(2, 'Flour', 10000.00, 'g'),
(3, 'Butter', 2000.00, 'g'),
(4, 'Chocolate', 1500.00, 'g'),
(5, 'Coffee Beans', 1000.00, 'g'),
(6, 'Spices', 3000.00, 'g'),
(7, 'Veggies', 8000.00, 'g')
ON DUPLICATE KEY UPDATE quantity=quantity;

-- Seed Recipes (Menu Item Ingredients Mapping)
INSERT INTO Menu_Item_Ingredients (menu_item_id, inventory_id, quantity_needed) VALUES
-- Paneer Tikka (101): 200g Paneer, 50g Veggies, 10g Spices
(101, 1, 200.00),
(101, 7, 50.00),
(101, 6, 10.00),
-- Spring Rolls (102): 100g Flour, 100g Veggies, 5g Spices
(102, 2, 100.00),
(102, 7, 100.00),
(102, 6, 5.00),
-- Dal Makhani (201): 50g Butter, 10g Spices
(201, 3, 50.00),
(201, 6, 10.00),
-- Butter Roti (202): 50g Flour, 10g Butter
(202, 2, 50.00),
(202, 3, 10.00),
-- Kadhai Paneer (203): 150g Paneer, 80g Veggies, 15g Spices
(203, 1, 150.00),
(203, 7, 80.00),
(203, 6, 15.00),
-- Choco Lava Cake (301): 80g Flour, 50g Chocolate, 20g Butter
(301, 2, 80.00),
(301, 4, 50.00),
(301, 3, 20.00),
-- Iced Americano (302): 15g Coffee Beans
(302, 5, 15.00)
ON DUPLICATE KEY UPDATE quantity_needed=quantity_needed;
