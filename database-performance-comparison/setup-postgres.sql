DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(150),
    age INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO users (name, email, age)
SELECT 
    'User' || generate_series(1, 5000000),
    'user' || generate_series(1, 5000000) || '@test.com',
    20 + (generate_series(1, 5000000) % 50);

CREATE INDEX idx_users_age ON users(age);
CREATE INDEX idx_users_name ON users(name);

SELECT COUNT(*) as total_users FROM users;
