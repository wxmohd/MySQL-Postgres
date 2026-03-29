-- PostgreSQL Large Dataset Setup (5 Million Users)
-- Note: Run CREATE DATABASE test_db; separately if needed
-- \c test_db;

-- Drop existing table if it exists
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(150),
    age INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert initial test data
INSERT INTO users (name, email, age) VALUES 
('John Doe', 'john@email.com', 25),
('Jane Smith', 'jane@email.com', 30),
('Bob Johnson', 'bob@email.com', 35),
('Alice Brown', 'alice@email.com', 28),
('Charlie Wilson', 'charlie@email.com', 42);

-- Add 5 MILLION records (PostgreSQL version)
INSERT INTO users (name, email, age)
SELECT 
    'User' || generate_series(1, 5000000),
    'user' || generate_series(1, 5000000) || '@test.com',
    20 + (generate_series(1, 5000000) % 50);

-- Create indexes for better performance
CREATE INDEX idx_users_age ON users(age);
CREATE INDEX idx_users_name ON users(name);

-- Verify data
SELECT COUNT(*) as total_users FROM users;
