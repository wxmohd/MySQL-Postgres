-- MySQL ID Generation Test Tables
-- Run this in MySQL/MariaDB

-- Table 1: AUTO_INCREMENT (IDENTITY)
DROP TABLE IF EXISTS test_identity;
CREATE TABLE test_identity (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table 2: UUID
DROP TABLE IF EXISTS test_uuid;
CREATE TABLE test_uuid (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table 3: Manual sequence simulation (auto_increment with offset)
DROP TABLE IF EXISTS test_sequence;
CREATE TABLE test_sequence (
    id INT PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- For allocation size test
DROP TABLE IF EXISTS test_batch_identity;
CREATE TABLE test_batch_identity (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- For sequence allocation size test
DROP TABLE IF EXISTS test_sequence_alloc;
CREATE TABLE test_sequence_alloc (
    id INT PRIMARY KEY,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_test_identity_created_at ON test_identity(created_at);
CREATE INDEX idx_test_uuid_created_at ON test_uuid(created_at);
CREATE INDEX idx_test_serial_created_at ON test_serial(created_at);
CREATE INDEX idx_test_sequence_created_at ON test_sequence(created_at);
