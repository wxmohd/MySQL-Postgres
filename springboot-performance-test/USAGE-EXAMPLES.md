# Examples of Manual Query Testing

This file shows examples of how to use the CLI testing components.

## Using the new testing classes

All the testing is now automated through the `CliRunner` component when the application starts.

### Starting the Application

#### Interactive Mode (Recommended)
```bash
java -jar target/db-performance-test-1.0.0.jar
```
This will show you the interactive menu where you can choose tests to run.

#### Direct Test Execution
```bash
# Run all ID generation tests with 150 records each
java -jar target/db-performance-test-1.0.0.jar --test=all-id --count=150

# Run MySQL IDENTITY test with 300 records
java -jar target/db-performance-test-1.0.0.jar --test=mysql-identity --count=300

# Run PostgreSQL SERIAL test with 250 records
java -jar target/db-performance-test-1.0.0.jar --test=postgres-serial --count=250

# Run PostgreSQL UUID test with 100 records
java -jar target/db-performance-test-1.0.0.jar --test=postgres-uuid
```

#### Windows Batch File
```bash
# From the project directory
run-cli.bat

# Or with arguments
run-cli.bat --test=all-id --count=200
```

## Example Test Outputs

### MySQL IDENTITY Test Output
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
  Inserted 30 records
  Inserted 40 records
  Inserted 50 records
  Inserted 60 records
  Inserted 70 records
  Inserted 80 records
  Inserted 90 records
  Inserted 100 records
  Duration: 245ms
  Average per insert: 2.45ms
  Records/second: 408
Step 3: Retrieving generated IDs
  First ID: 1
  Last ID: 100
  ID Range: 1 to 100
========================================
```

### PostgreSQL SEQUENCE with Allocation Size Output
```
========================================
TEST: POSTGRESQL SEQUENCE (Allocation Size: 50)
Record Count: 100
========================================
Step 1: Cleaning up old data
  ✓ Data deleted and sequence reset
Step 2: Inserting 100 records with allocation size 50
  Allocation batch 1: fetching 50 IDs from sequence
  Allocation batch 2: fetching 50 IDs from sequence
  Duration: 156ms
  Average per insert: 1.56ms
  Records/second: 641
Step 3: Sequence Call Analysis
  Total allocation batches: 2
  Sequence calls per allocation: 1 (for 50 IDs)
========================================
```

### Concurrent Insert Output
```
Database: MYSQL | Threads: 10 | Records/Thread: 100

  Total time: 1245ms
  Avg thread time: 125.00ms
  Min thread time: 115ms
  Max thread time: 145ms
  Total records inserted: 1000
  Records/sec: 803
```

### Manual Query Execution Output
```
========================================
MANUAL QUERY EXECUTION
========================================
Database: MYSQL
Query: SELECT * FROM test_identity WHERE id > 50 LIMIT 5;
----------------------------------------
Execution Time: 12ms
Rows Returned: 5
----------------------------------------
RESULTS:
Row 1: {id=51, name=User_50, created_at=2024-01-15 10:30:45.123}
Row 2: {id=52, name=User_51, created_at=2024-01-15 10:30:45.234}
Row 3: {id=53, name=User_52, created_at=2024-01-15 10:30:45.345}
Row 4: {id=54, name=User_53, created_at=2024-01-15 10:30:45.456}
Row 5: {id=55, name=User_54, created_at=2024-01-15 10:30:45.567}
========================================
```

### Performance Comparison Output
```
========================================
PERFORMANCE COMPARISON
========================================
Query: SELECT COUNT(*) FROM test_identity;
========================================
... (MySQL execution details)
... (PostgreSQL execution details)
----------------------------------------
RESULTS:
MySQL Time: 5ms
PostgreSQL Time: 7ms
Winner: MySQL (1.40x faster)
========================================
```

## Available Test Methods

### IdGenerationManualTester Methods

#### MySQL Tests
- `testMysqlIdentity(int count)` - Test AUTO_INCREMENT
- `testMysqlUUID(int count)` - Test client-side UUID generation

#### PostgreSQL Tests
- `testPostgresSerial(int count)` - Test SERIAL type
- `testPostgresIdentity(int count)` - Test GENERATED ALWAYS AS IDENTITY
- `testPostgresUUID(int count)` - Test server-side UUID generation
- `testPostgresSequenceWithAllocationSize(int count, int allocationSize)` - Test SEQUENCE with custom allocation

#### Comparison Tests
- `testBatchInsertComparison(int count)` - Compare batch insert performance
- `testConcurrentInserts(int threadCount, int recordsPerThread)` - Test concurrent inserts

### ManualQueryTester Methods

- `executeManualQuery(String database, String sql)` - Execute SELECT query with logging
- `executeUpdate(String database, String sql, Object... params)` - Execute UPDATE with logging
- `comparePerformance(String sql)` - Compare query performance between databases

## Logging

### Log File Location
```
logs/db-performance.log
```

### Sample Log Entries
```
10:23:45.123 [main] DEBUG c.e.d.cli.IdGenerationManualTester : Step 1: Cleaning up old data
10:23:45.234 [main] INFO  c.e.d.cli.ManualQueryTester : Execution Time: 12ms
10:23:45.345 [main] INFO  c.e.d.cli.ManualQueryTester : Rows Returned: 5
10:23:45.456 [pool-1-thread-1] DEBUG c.e.d.cli.IdGenerationManualTester : Thread 0 error: Connection timeout
```

### Adjusting Log Levels

Edit `src/main/resources/application.properties`:

```properties
# For more verbose output
logging.level.com.example.dbperformance=DEBUG
logging.level.com.example.dbperformance.cli=DEBUG

# For less verbose output
logging.level.com.example.dbperformance=INFO
logging.level.com.example.dbperformance.cli=INFO
```

## Quick Testing Workflow

### 1. Setup Databases
```bash
# MySQL
mysql -u root -D test_db < setup-id-mysql.sql

# PostgreSQL
psql -U postgres -d test_db -f setup-id-postgres.sql
```

### 2. Build Application
```bash
mvn clean package
```

### 3. Run Tests
```bash
# Interactive menu
java -jar target/db-performance-test-1.0.0.jar

# Or direct tests
java -jar target/db-performance-test-1.0.0.jar --test=all-id --count=1000
```

### 4. Analyze Results
- Check console output for real-time results
- Review `logs/db-performance.log` for detailed analysis
- Compare metrics across different ID strategies

## Performance Analysis Tips

### What to Look For

1. **Inserts per Second**
   - Higher = better performance
   - Compare across different ID strategies

2. **Average Time per Insert**
   - Lower = better performance
   - Look for consistency (min/max times close to average)

3. **Allocation Size Impact (PostgreSQL)**
   - Larger allocation = fewer DB round-trips = faster for many inserts
   - Trade-off: More memory usage

4. **Concurrent Performance**
   - Look at thread time variance
   - If max time >> min time, there's contention
   - Total time > (avg time × thread count) indicates contention

### Example Analysis

Comparing ID strategies with 1000 inserts:

```
MySQL IDENTITY:     1000 inserts in 2450ms → 408 inserts/sec
PostgreSQL SERIAL:  1000 inserts in 3125ms → 320 inserts/sec
PostgreSQL UUID:    1000 inserts in 2560ms → 391 inserts/sec
PostgreSQL SEQL-50: 1000 inserts in 1560ms → 641 inserts/sec
```

**Observation**: PostgreSQL SEQUENCE with allocation size 50 is fastest because it reduces database round-trips.

## Custom Query Testing

### Test Connection
```
Database: mysql
Query: SELECT 1;
```

### Count Records
```
Database: postgres
Query: SELECT COUNT(*) FROM test_serial;
```

### View Sample Data
```
Database: mysql
Query: SELECT id, name, created_at FROM test_identity LIMIT 10;
```

### Performance Test - Complex Query
```
Database: postgres
Query: SELECT id, name, created_at FROM test_uuid WHERE EXTRACT(YEAR FROM created_at) = 2024 LIMIT 50;
```

## Troubleshooting Tests

### If tests fail, check:

1. **Connection Error**: Ensure MySQL/PostgreSQL are running
2. **Table Not Found**: Run setup SQL scripts
3. **Permission Denied**: Ensure user has CREATE/DROP TABLE permissions
4. **Timeout**: Increase execution timeout or reduce record count
5. **Out of Memory**: Reduce record count or thread count

### Enable Debug Logging

Add to `application.properties`:
```properties
logging.level.org.springframework.jdbc=DEBUG
logging.level.com.zaxxer.hikari=DEBUG
```

Then check `logs/db-performance.log` for verbose SQL output.
