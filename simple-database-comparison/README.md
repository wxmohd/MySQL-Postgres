# MySQL vs PostgreSQL Performance Comparison - Complete Guide

A comprehensive performance comparison between MySQL (MariaDB) and PostgreSQL using 5 million records to demonstrate real-world performance differences.

## Final Results Summary

**With 5 Million Records:**
- **PostgreSQL: 218ms total (55% faster overall)**
- **MySQL: 484ms total**

PostgreSQL showed significant advantages at scale, particularly for COUNT operations (198ms vs 461ms).

---

## Prerequisites

- **MariaDB 12.2** (MySQL-compatible)
- **PostgreSQL 17**
- **Java 17+** (Eclipse Adoptium JDK 25)
- **MySQL Workbench** (for MariaDB management)
- **pgAdmin 4** (for PostgreSQL management)

---

## Complete Setup Process

### Step 1: Environment Setup

1. **Install MariaDB 12.2** (MySQL-compatible alternative)
2. **Install PostgreSQL 17** with pgAdmin 4
3. **Install Java 17+** (we used Eclipse Adoptium JDK 25)
4. **Verify all services are running**

### Step 2: Download JDBC Drivers

Download these JAR files to your project directory:
- `mariadb-java-client-3.1.4.jar` 
- `postgresql-42.6.0.jar`

### Step 3: Database Setup Scripts

#### MySQL/MariaDB Setup (`setup-mysql.sql`):
```sql
-- MySQL/MariaDB Large Dataset Setup (5 Million Users)
CREATE DATABASE IF NOT EXISTS test_db;
USE test_db;

-- Drop existing table if it exists
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(150),
    age INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- All 5 million users follow consistent pattern:
-- Insert 5 million records using MariaDB compatible method
INSERT INTO users (name, email, age)
SELECT 
    CONCAT('User', n) as name,
    CONCAT('user', n, '@test.com') as email,
    20 + (n % 50) as age
FROM (
    SELECT a.N + b.N * 10 + c.N * 100 + d.N * 1000 + e.N * 10000 + f.N * 100000 + g.N * 1000000 + 1 as n
    FROM 
    (SELECT 0 N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a
    CROSS JOIN 
    (SELECT 0 N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b
    CROSS JOIN 
    (SELECT 0 N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) c
    CROSS JOIN 
    (SELECT 0 N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) d
    CROSS JOIN 
    (SELECT 0 N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) e
    CROSS JOIN 
    (SELECT 0 N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) f
    CROSS JOIN 
    (SELECT 0 N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) g
) numbers
WHERE n <= 5000000;

-- Create indexes for better performance
CREATE INDEX idx_users_age ON users(age);
CREATE INDEX idx_users_name ON users(name);

-- Verify data
SELECT COUNT(*) as total_users FROM users;
```

#### PostgreSQL Setup (`setup-postgres.sql`):
```sql
-- PostgreSQL Large Dataset Setup (5 Million Users)
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
```

### Step 4: Execute Database Setup

#### MySQL/MariaDB:
1. Open **MySQL Workbench**
2. Connect to your local MariaDB server
3. Execute the entire `setup-mysql.sql` script
4. **Wait 10-20 minutes** for 5M records to generate
5. Verify final count: **5,000,005 users**

#### PostgreSQL:
1. Open **pgAdmin 4**
2. Connect to PostgreSQL 17 server
3. Open Query Tool for `test_db` database
4. Execute the entire `setup-postgres.sql` script
5. **Wait 10-20 minutes** for 5M records to generate
6. Verify final count: **5,000,005 users**

### Step 5: Java Performance Test

#### Compile and Run:
```cmd
# Compile
"C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\javac.exe" -cp ".;mariadb-java-client-3.1.4.jar;postgresql-42.6.0.jar" SimpleTest.java

# Run
"C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\java.exe" -cp ".;mariadb-java-client-3.1.4.jar;postgresql-42.6.0.jar" SimpleTest
```

---

## Performance Results

### Small Dataset (1,005 records):
```
MySQL:    Total time: 19ms
PostgreSQL: Total time: 20ms
Difference: Negligible (1ms)
```

### Large Dataset (5,000,005 records):
```
MySQL:
  Simple SELECT: 18ms
  COUNT query: 461ms
  Filtered query: 5ms
  Total time: 484ms

PostgreSQL:
  Simple SELECT: 17ms
  COUNT query: 198ms
  Filtered query: 3ms
  Total time: 218ms

Performance Improvement: PostgreSQL 55% faster overall
```

---

## Key Insights

### Scale Matters
- **Small datasets**: Performance differences are negligible
- **Large datasets**: PostgreSQL shows significant advantages

### PostgreSQL Advantages at Scale:
- **COUNT operations**: 57% faster (198ms vs 461ms)
- **Filtered queries**: 40% faster (3ms vs 5ms)
- **Superior query optimization** for large datasets
- **Better MVCC implementation**

### MySQL/MariaDB Strengths:
- **Simple operations**: Virtually identical performance
- **Easier setup and configuration**
- **Lower memory footprint**
- **Better for simple web applications**

---

## Troubleshooting

### Common Issues Encountered:

1. **MariaDB Recursive CTE Error**: 
   - **Problem**: `WITH RECURSIVE` not supported in MariaDB
   - **Solution**: Used cross join method instead

2. **Java Path Issues**:
   - **Problem**: `javac` not found in PATH
   - **Solution**: Used full path to Java executables

3. **PostgreSQL Connection Issues**:
   - **Problem**: Service not running
   - **Solution**: Started postgresql-x64-17 service manually

4. **PowerShell Command Syntax**:
   - **Problem**: Command parsing errors
   - **Solution**: Used `&` operator for executable paths with spaces

---

## Lessons Learned

1. **Database choice matters more at scale** - differences become pronounced with millions of records
2. **PostgreSQL's query optimizer** excels with large datasets
3. **Index strategy is crucial** for both databases
4. **Testing methodology** must use realistic data volumes
5. **MariaDB compatibility** requires different syntax than MySQL 8.0+

---

## Recommendations

### Choose MySQL/MariaDB for:
- Web applications with simple queries
- Small to medium datasets (< 1M records)
- Rapid prototyping and development
- When setup simplicity is priority

### Choose PostgreSQL for:
- Large datasets (millions of records)
- Complex analytical queries
- Enterprise applications
- When query performance at scale is critical

---

## Project Structure

```
simple-database-comparison/
├── README.md                          # This comprehensive guide
├── SimpleTest.java                    # Java performance test
├── setup.sql                         # Original small dataset setup
├── mariadb-java-client-3.1.4.jar    # MariaDB JDBC driver
└── postgresql-42.6.0.jar            # PostgreSQL JDBC driver

database-performance-comparison/
├── setup-mysql.sql                   # Large dataset MySQL setup
└── setup-postgres.sql               # Large dataset PostgreSQL setup
```

---

## Hardware Specifications

- **CPU**: AMD Ryzen AI 9 HX 370 (12 cores, 24 threads)
- **RAM**: 32GB DDR5
- **Storage**: NVMe SSD
- **OS**: Windows 11

---

## Conclusion

This comparison demonstrates that **database performance characteristics change significantly at scale**. While both MySQL and PostgreSQL perform similarly with small datasets, PostgreSQL's advanced query optimizer and MVCC implementation provide substantial advantages when working with millions of records.

The 55% performance improvement with PostgreSQL at scale (218ms vs 484ms) represents the difference between a responsive application and one that frustrates users in real-world scenarios.
