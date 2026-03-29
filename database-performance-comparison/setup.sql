-- Simple Database Setup for MySQL and PostgreSQL Comparison

-- Create a simple users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(150),
    age INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert some test data
INSERT INTO users (name, email, age) VALUES 
('John Doe', 'john@email.com', 25),
('Jane Smith', 'jane@email.com', 30),
('Bob Johnson', 'bob@email.com', 35),
('Alice Brown', 'alice@email.com', 28),
('Charlie Wilson', 'charlie@email.com', 42);

-- Add more test data (1000 records)
INSERT INTO users (name, email, age)
SELECT 
    'User' || generate_series(1, 1000),
    'user' || generate_series(1, 1000) || '@test.com',
    20 + (generate_series(1, 1000) % 50);

-- Create an index for better performance
CREATE INDEX idx_users_age ON users(age);
