# Database Performance Test - CLI Manual Query Testing Guide

## Overview

This guide explains how to use the new **CLI-based manual query testing system** that replaces the dashboard approach. You can now:
- ✅ Write custom queries manually
- ✅ See detailed logs for each operation
- ✅ Test different ID generation strategies
- ✅ Compare performance between MySQL and PostgreSQL
- ✅ Run concurrent insert tests with allocation size variations

## Quick Start

### Prerequisites

1. **Database Setup**: Run the setup SQL scripts first

   **MySQL/MariaDB:**
   ```bash
   mysql -u root -D test_db < setup-id-mysql.sql
   ```

   **PostgreSQL:**
   ```bash
   psql -U postgres -d test_db -f setup-id-postgres.sql
   ```

2. **Build the Application:**
   ```bash
   mvn clean package
   ```

3. **Run the Application:**
   ```bash
   java -jar target/db-performance-test-1.0.0.jar
   ```

## Interactive CLI Menu

When you start the application, you'll see an interactive menu:

```
╔════════════════════════════════════════╗
║      DATABASE PERFORMANCE TEST CLI     ║
╚════════════════════════════════════════╝
1. Test Single ID Generation Strategy
2. Execute Manual Queries
3. Test All ID Generation Strategies
4. Exit
════════════════════════════════════════
```

### Menu Option 1: Test Single ID Generation Strategy

Choose which ID generation strategy to test:

```
════ ID GENERATION TESTS ════
1. MySQL IDENTITY (AUTO_INCREMENT)
2. MySQL UUID
3. PostgreSQL SERIAL
4. PostgreSQL IDENTITY
5. PostgreSQL UUID
6. PostgreSQL SEQUENCE (Allocation Size 1)
7. PostgreSQL SEQUENCE (Allocation Size 50)
8. Batch Insert Comparison
9. Concurrent Inserts (10 threads, 100 records each)
0. Back to Main Menu
```

**Example Output:**
```
========================================
TEST: MYSQL IDENTITY (AUTO_INCREMENT)
Record Count: 100
========================================
Step 1: Cleaning up old data
  ✓ Data deleted
Step 2: Inserting 100 records
  Inserted 10 records
  Inserted 20 records
  ... (progress output)
  Duration: 245ms
  Average per insert: 2.45ms
  Records/second: 408
Step 3: Retrieving generated IDs
  First ID: 1
  Last ID: 100
  ID Range: 1 to 100
========================================
```

### Menu Option 2: Execute Manual Queries

Execute custom SQL queries with detailed logging:

**For SELECT queries:**
```
Enter database (mysql/postgres): mysql
Enter SQL query: SELECT * FROM test_identity LIMIT 5;

========================================
MANUAL QUERY EXECUTION
========================================
Database: MYSQL
Query: SELECT * FROM test_identity LIMIT 5;
----------------------------------------
Execution Time: 12ms
Rows Returned: 5
----------------------------------------
RESULTS:
Row 1: {id=1, name=User_0, created_at=...}
Row 2: {id=2, name=User_1, created_at=...}
...
========================================
```

**For UPDATE queries:**
```
Enter database (mysql/postgres): postgres
Enter SQL UPDATE query: UPDATE test_serial SET name = 'Updated' WHERE id = 1;

========================================
MANUAL UPDATE EXECUTION
========================================
Database: POSTGRES
Query: UPDATE test_serial SET name = 'Updated' WHERE id = 1;
Parameters: []
----------------------------------------
Execution Time: 8ms
Rows Affected: 1
========================================
```

**For Performance Comparison:**
```
Enter SQL query: SELECT COUNT(*) FROM test_identity;

========================================
PERFORMANCE COMPARISON
========================================
Query: SELECT COUNT(*) FROM test_identity;
========================================
... (MySQL execution)
... (PostgreSQL execution)
----------------------------------------
RESULTS:
MySQL Time: 5ms
PostgreSQL Time: 7ms
Winner: MySQL (1.40x faster)
========================================
```

### Menu Option 3: Test All ID Generation Strategies

Runs all tests sequentially with specified record count:
```
Enter record count for each test (default 100): 200

════ RUNNING ALL ID GENERATION TESTS ════

• MySQL IDENTITY Test
[detailed output...]

• MySQL UUID Test
[detailed output...]

• PostgreSQL SERIAL Test
[detailed output...]

• PostgreSQL IDENTITY Test
[detailed output...]

• PostgreSQL UUID Test
[detailed output...]

• PostgreSQL SEQUENCE (Allocation 1)
[detailed output...]

• PostgreSQL SEQUENCE (Allocation 50)
[detailed output...]

• Batch Insert Comparison
[detailed output...]

• Concurrent Inserts Test
[detailed output...]

════ ALL TESTS COMPLETED ════
```

## Command-Line Arguments

You can also run specific tests directly without the interactive menu:

```bash
# Run all ID tests with 100 records each
java -jar target/db-performance-test-1.0.0.jar --test=all-id --count=100

# Run specific test
java -jar target/db-performance-test-1.0.0.jar --test=mysql-identity --count=200

# Available tests:
# --test=all-id              (all ID generation tests)
# --test=mysql-identity      (MySQL AUTO_INCREMENT)
# --test=mysql-uuid          (MySQL UUID)
# --test=postgres-serial     (PostgreSQL SERIAL)
# --test=postgres-identity   (PostgreSQL IDENTITY)
# --test=postgres-uuid       (PostgreSQL UUID)
```

## ID Generation Strategies Explained

### MySQL IDENTITY (AUTO_INCREMENT)
- **Test Name**: MySQL IDENTITY (AUTO_INCREMENT)
- **How it works**: Database automatically generates sequential integer IDs
- **Pros**: Fast, simple, automatic
- **Cons**: Not suitable for distributed systems
- **Use case**: Single database, simple applications

### MySQL UUID
- **Test Name**: MySQL UUID
- **How it works**: Generates UUID on client side (Java)
- **Pros**: Globally unique, suitable for distribution
- **Cons**: Slower than AUTO_INCREMENT, larger storage
- **Use case**: Microservices, distributed systems

### PostgreSQL SERIAL
- **Test Name**: PostgreSQL SERIAL
- **How it works**: Creates a sequence that generates sequential integers
- **Pros**: Similar to MySQL AUTO_INCREMENT
- **Cons**: Not truly concurrent-safe
- **Use case**: Single database applications

### PostgreSQL IDENTITY
- **Test Name**: PostgreSQL IDENTITY
- **How it works**: SQL standard GENERATED ALWAYS AS IDENTITY
- **Pros**: SQL standard, cleaner syntax than SERIAL
- **Cons**: PostgreSQL 10+
- **Use case**: Modern PostgreSQL applications

### PostgreSQL UUID
- **Test Name**: PostgreSQL UUID
- **How it works**: Generates UUID on server side using gen_random_uuid()
- **Pros**: Server-side generation, globally unique
- **Cons**: Slower than SERIAL
- **Use case**: Distributed systems

### PostgreSQL SEQUENCE with Allocation Size
- **Test Names**: 
  - PostgreSQL SEQUENCE (Allocation Size 1) - Single value per sequence call
  - PostgreSQL SEQUENCE (Allocation Size 50) - Batch of 50 values per sequence call
- **How it works**: Uses explicit SEQUENCE with nextval()
- **Pros**: Fine-grained control over allocation caching
- **Cons**: More manual control required
- **Use case**: Performance tuning distributed ID generation
- **Impact**: Larger allocation sizes = fewer DB round-trips but more memory

## Logging Output

All operations are logged to both console and file:

**Console Output** (real-time):
- Detailed information about each test step
- Performance metrics
- Query results (first 10 rows)

**File Output** (`logs/db-performance.log`):
- Complete test history
- Error details with stack traces
- Timestamps for all operations
- Thread information for concurrent tests

### Log Levels

Currently configured:
- `DEBUG`: Application-specific components (cli, service, controller)
- `INFO`: General framework information
- `WARN`: Connection pools, drivers
- `ERROR`: Failed operations

Adjust in `application.properties`:
```properties
logging.level.root=INFO
logging.level.com.example.dbperformance=DEBUG
logging.level.com.example.dbperformance.cli=DEBUG
```

## Advanced Usage

### Performance Comparison Workflow

1. **Prepare test data**:
   ```
   Option 1: MySQL IDENTITY with 1000 records
   Option 3: Test All with count=1000
   ```

2. **Run manual queries on both databases**:
   ```
   Manual query: SELECT COUNT(*) FROM test_identity
   Manual query: SELECT COUNT(*) FROM test_serial
   ```

3. **Compare performance**:
   ```
   Performance comparison on: SELECT * FROM test_identity LIMIT 100
   Performance comparison on: SELECT * FROM test_serial LIMIT 100
   ```

### Testing Allocation Size Impact

1. Run `PostgreSQL SEQUENCE (Allocation Size 1)` with 100 records
2. Note the execution time
3. Run `PostgreSQL SEQUENCE (Allocation Size 50)` with 100 records
4. Compare results - larger allocation size should be faster

The difference shows how batching affects performance:
- **Size 1**: 100 sequence calls (100 DB round-trips)
- **Size 50**: 2 sequence calls (2 DB round-trips)

### Concurrent Insert Testing

Tests how each database handles concurrent inserts:
- 10 threads simultaneously inserting records
- Each thread inserts 100 records
- Total: 1000 records

Metrics captured:
- Total time for all threads to complete
- Average thread execution time
- Min/max thread times
- Records per second

## Database Connections

The application maintains separate connection pools for each database:

**MySQL/MariaDB:**
- JDBC URL: `jdbc:mariadb://localhost:3306/test_db`
- User: `root`
- Pool Size: 20 (max), 5 (min-idle)

**PostgreSQL:**
- JDBC URL: `jdbc:postgresql://localhost:5432/test_db`
- User: `postgres`
- Pool Size: 20 (max), 5 (min-idle)

Configure in `application.properties` if your database settings differ.

## Troubleshooting

### Connection Refused
```
ERROR: Connection refused
```
**Solution**: Ensure MySQL and PostgreSQL are running
```bash
# Check MySQL
mysql -u root -e "SELECT 1"

# Check PostgreSQL
psql -U postgres -c "SELECT 1"
```

### Table Not Found
```
ERROR: Table test_identity does not exist
```
**Solution**: Run the setup SQL script first
```bash
# MySQL
mysql -u root -D test_db < setup-id-mysql.sql

# PostgreSQL
psql -U postgres -d test_db -f setup-id-postgres.sql
```

### Permission Denied
```
ERROR: Permission denied creating tables
```
**Solution**: Ensure your database user has CREATE/DROP TABLE permissions

## Example Test Scenarios

### Scenario 1: Comparing ID Generation Speed
```
1. Test Single ID Generation Strategy
   Option 1: MySQL IDENTITY with 500 records
2. Test Single ID Generation Strategy
   Option 3: PostgreSQL SERIAL with 500 records
Compare the execution times and records/second
```

### Scenario 2: Testing Allocation Size Impact
```
1. Test Single ID Generation Strategy
   Option 6: PostgreSQL SEQUENCE (Allocation Size 1) with 200 records
2. Test Single ID Generation Strategy
   Option 7: PostgreSQL SEQUENCE (Allocation Size 50) with 200 records
Note the time difference - allocation affects performance
```

### Scenario 3: Custom Query Performance Testing
```
2. Execute Manual Queries
   Option 3: Performance Comparison
   Query: SELECT * FROM test_identity WHERE id > 50 AND id < 150
```

## Logs Directory

All logs are stored in: `logs/db-performance.log`

The logs include:
- Timestamp of each operation
- Thread name (useful for concurrent tests)
- Log level (DEBUG, INFO, WARN, ERROR)
- Logger name (component that generated the log)
- Actual log message

Example log entries:
```
11:23:45.123 [main] DEBUG c.e.d.cli.IdGenerationManualTester : Step 1: Cleaning up old data
11:23:45.234 [main] DEBUG c.e.d.cli.IdGenerationManualTester :   ✓ Data deleted
11:23:45.235 [main] DEBUG c.e.d.cli.IdGenerationManualTester : Step 2: Inserting 100 records
11:23:45.456 [main] DEBUG c.e.d.cli.IdGenerationManualTester :   Inserted 10 records
```

## Next Steps

1. Run the application: `java -jar target/db-performance-test-1.0.0.jar`
2. Try each test type to understand the behavior
3. Write your own manual queries to test specific scenarios
4. Use the logs to analyze performance and identify bottlenecks

## Dashboard (Optional)

The web dashboard is still available at `http://localhost:8080/` if needed, but the CLI interface is now the recommended approach for manual testing with detailed logs.
