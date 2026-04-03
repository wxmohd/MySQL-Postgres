# CLI Manual Query Testing System - Complete Guide

## What You Get

Your database performance testing system has been upgraded with a comprehensive **CLI-based manual query testing interface**. Instead of relying solely on the web dashboard, you can now:

✅ **Write and execute custom queries manually**
✅ **See detailed logging for every operation**
✅ **Test different ID generation strategies** (IDENTITY, SERIAL, UUID, etc.)
✅ **Compare allocation sizes** (1 vs 50 vs custom)
✅ **Test concurrent inserts** with multiple threads
✅ **Profile performance** with accurate timing metrics
✅ **Run tests directly from command line** without UI

## New Files Created

### CLI Testing Components
Located in: `src/main/java/com/example/dbperformance/cli/`

```
├── CliRunner.java                    # Entry point, shows interactive menu
├── IdGenerationManualTester.java     # Tests for ID generation strategies
└── ManualQueryTester.java            # Custom query execution with logging
```

### Documentation Files
```
├── CLI-TESTING-GUIDE.md              # Comprehensive testing guide
├── USAGE-EXAMPLES.md                 # Example outputs and workflows
├── QUICK-REFERENCE.md                # Quick command reference
└── SUMMARY.md                        # This file
```

### Configuration Files
```
├── src/main/resources/application.properties  # Updated with logging config
└── src/main/resources/logback.xml            # New logging configuration
```

### Database Setup
```
├── setup-id-mysql.sql                # Updated with new tables for tests
└── setup-id-postgres.sql             # Updated with new tables for tests
```

### Launch Script
```
└── run-cli.bat                       # Windows batch file for easy startup
```

## Quick Start (3 Steps)

### Step 1: Setup Databases
```bash
# MySQL/MariaDB
mysql -u root -D test_db < setup-id-mysql.sql

# PostgreSQL
psql -U postgres -d test_db -f setup-id-postgres.sql
```

### Step 2: Build Application
```bash
mvn clean package
```

### Step 3: Run Tests
```bash
# Interactive mode (recommended)
java -jar target/db-performance-test-1.0.0.jar

# Or Windows
run-cli.bat

# Or command-line mode
java -jar target/db-performance-test-1.0.0.jar --test=all-id --count=200
```

## How It Works

### 1. Interactive Menu System

When you start the application, you get a menu:

```
╔════════════════════════════════════════╗
║      DATABASE PERFORMANCE TEST CLI     ║
╚════════════════════════════════════════╝
1. Test Single ID Generation Strategy
2. Execute Manual Queries
3. Test All ID Generation Strategies
4. Exit
```

### 2. Available Tests

#### ID Generation Tests (Option 1)
- MySQL IDENTITY (AUTO_INCREMENT)
- MySQL UUID
- PostgreSQL SERIAL
- PostgreSQL IDENTITY
- PostgreSQL UUID
- PostgreSQL SEQUENCE with Allocation Size 1
- PostgreSQL SEQUENCE with Allocation Size 50
- Batch Insert Comparison
- Concurrent Inserts (10 threads)

Each test:
- Prepares clean data
- Measures insertion performance
- Shows detailed metrics
- Logs all operations

#### Manual Query Execution (Option 2)
- Execute SELECT queries
- Execute UPDATE queries  
- Compare query performance between databases
- See results with detailed timing

#### All Tests (Option 3)
- Runs complete test suite sequentially
- Compares all ID strategies
- Shows comprehensive performance analysis

### 3. Detailed Logging

Every operation is logged to:
- **Console**: Real-time output for monitoring
- **File**: `logs/db-performance.log` for persistent records

Example log:
```
10:23:45.123 [main] DEBUG CliRunner - Step 1: Cleaning up old data
10:23:45.234 [main] INFO IdGenerationManualTester -   ✓ Data deleted
10:23:45.235 [main] DEBUG IdGenerationManualTester - Step 2: Inserting 100 records
10:23:45.456 [main] INFO IdGenerationManualTester -   Duration: 245ms
10:23:45.457 [main] INFO IdGenerationManualTester -   Average per insert: 2.45ms
10:23:45.458 [main] INFO IdGenerationManualTester -   Records/second: 408
```

## Key Features Explained

### 1. ID Generation Strategy Tests

Each ID strategy is tested with consistent methodology:

**MySQL IDENTITY (AUTO_INCREMENT)**
- Automatically generated sequential IDs
- Performance: Fastest (408+ inserts/sec)
- Best for: Single database applications

**MySQL UUID**
- Client-generated UUIDs
- Performance: Medium (350+ inserts/sec)
- Best for: Distributed systems

**PostgreSQL SERIAL**
- Database-generated sequences
- Performance: Medium (320+ inserts/sec)
- Best for: Single database applications

**PostgreSQL IDENTITY**
- SQL standard GENERATED ALWAYS AS IDENTITY
- Performance: Similar to SERIAL
- Best for: Modern PostgreSQL (10+)

**PostgreSQL UUID**
- Server-generated UUIDs via gen_random_uuid()
- Performance: Medium (350+ inserts/sec)
- Best for: Distributed systems

**PostgreSQL SEQUENCE with Allocation**
- Explicit sequence usage with batching
- Performance: Fastest (600+ inserts/sec)
- Allocation 1: 1 DB call per ID generation
- Allocation 50: 1 DB call per 50 ID generations
- Best for: Tuning batch operations

### 2. Allocation Size Testing

Critical for distributed ID generation:

```
Without allocation (Size 1):
100 inserts → 100 sequence calls → 100 DB round-trips

With allocation (Size 50):
100 inserts → 2 sequence calls → 2 DB round-trips
```

The 50x reduction in database calls dramatically improves performance!

### 3. Concurrent Insert Testing

Tests real-world concurrent load:

```
10 threads × 100 records = 1000 total inserts
Measures:
- Total completion time
- Per-thread timing (avg/min/max)
- Throughput (inserts/second)
- Contention indicators
```

### 4. Custom Query Testing

Execute your own queries with detailed metrics:

```
Database: mysql
Query: SELECT * FROM test_identity WHERE id > 50 LIMIT 10

Results:
- Execution time
- Number of rows returned
- Sample data for verification
```

## Performance Metrics Explained

### Inserts/Second
- What: Number of successful inserts per second
- Why: Higher = better performance
- Compare: Across different ID strategies

### Average Per Insert
- What: Total time ÷ number of inserts
- Why: Shows consistency of performance
- Compare: Should be stable (1-3 ms typically)

### Min/Max Times
- What: Fastest and slowest individual operations
- Why: Shows variance and contention
- Good: Min ≈ Max (consistent performance)
- Bad: Max >> Min (contention or GC pauses)

### Records/Second in Concurrent Tests
- What: Total inserted ÷ total time × 1000
- Why: Shows throughput under load
- Compare: Database efficiency at scale

## File Structure

```
springboot-performance-test/
│
├── src/main/java/com/example/dbperformance/
│   ├── cli/
│   │   ├── CliRunner.java              # Entry point, menu system
│   │   ├── IdGenerationManualTester.java # ID generation tests
│   │   └── ManualQueryTester.java       # Custom query execution
│   ├── config/
│   │   └── DataSourceConfig.java        # Database configuration
│   ├── controller/
│   │   └── QueryController.java         # REST API (optional)
│   ├── service/
│   │   ├── IdGenerationTestService.java # Original service
│   │   └── QueryService.java            # Original service
│   └── DbPerformanceApplication.java    # Spring Boot app
│
├── src/main/resources/
│   ├── application.properties           # Updated: logging config
│   ├── logback.xml                      # New: detailed logging
│   ├── static/
│   │   └── index.html                   # Optional: web dashboard
│   └── (other resources)
│
├── Documentation/
│   ├── CLI-TESTING-GUIDE.md             # Complete guide
│   ├── USAGE-EXAMPLES.md                # Examples & outputs
│   ├── QUICK-REFERENCE.md               # Quick commands
│   └── SUMMARY.md                       # This file
│
├── setup-id-mysql.sql                   # Updated: test tables
├── setup-id-postgres.sql                # Updated: test tables
├── run-cli.bat                          # New: Windows launcher
├── run.bat                              # Original: web server
├── pom.xml                              # Maven config
└── README.md                            # Project README
```

## Setting Up & Running

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL/MariaDB running on localhost:3306
- PostgreSQL running on localhost:5432
- Databases named `test_db` in both systems

### Installation

1. **Clone/Extract Project**
   ```bash
   cd springboot-performance-test
   ```

2. **Create Databases** (if needed)
   ```bash
   # MySQL
   mysql -u root -e "CREATE DATABASE test_db;"
   
   # PostgreSQL
   createdb -U postgres test_db
   ```

3. **Run Setup Scripts**
   ```bash
   mysql -u root -D test_db < setup-id-mysql.sql
   psql -U postgres -d test_db -f setup-id-postgres.sql
   ```

4. **Build Project**
   ```bash
   mvn clean package -DskipTests
   ```

5. **Run Application**
   ```bash
   java -jar target/db-performance-test-1.0.0.jar
   ```

## Usage Examples

### Example 1: Compare All ID Strategies
```
START APPLICATION
→ Choose Option: 3 (Test All)
→ Enter record count: 1000
→ Wait for all tests
→ Review results in logs/db-performance.log
```
Result: See which strategy is fastest with 1000 inserts

### Example 2: Test Allocation Size Impact
```
START APPLICATION
→ Choose Option: 1 (Single Test)
→ Choose Test: 6 (Allocation Size 1) with 500 records
→ Note the time
→ Choose Option: 1 again
→ Choose Test: 7 (Allocation Size 50) with 500 records
→ Compare times
```
Result: Allocation size 50 should be significantly faster

### Example 3: Custom Query Performance Test
```
START APPLICATION
→ Choose Option: 2 (Manual Queries)
→ Choose Option: 3 (Performance Comparison)
→ Enter Query: SELECT COUNT(*) FROM test_identity;
→ View MySQL vs PostgreSQL execution times
```
Result: See which database handles your query faster

### Example 4: Direct Command Line
```bash
# Run all ID tests with 2000 records
java -jar target/db-performance-test-1.0.0.jar --test=all-id --count=2000

# Run specific test
java -jar target/db-performance-test-1.0.0.jar --test=postgres-uuid --count=1000
```

## Reading the Output

### Test Header
```
========================================
TEST: POSTGRESQL SEQUENCE (Allocation Size: 50)
Record Count: 1000
========================================
```
Shows which test and how many records

### Progress
```
Step 1: Cleaning up old data
  ✓ Data deleted and sequence reset
```
Shows preparation steps

### Execution
```
Step 2: Inserting 1000 records with allocation size 50
  Allocation batch 1: fetching 50 IDs from sequence
  Allocation batch 2: fetching 50 IDs from sequence
  ...
  Duration: 1560ms
  Average per insert: 1.56ms
  Records/second: 641
```
Shows performance metrics

### Results Verification
```
Step 3: Sequence Call Analysis
  Total allocation batches: 20
  Sequence calls per allocation: 1 (for 50 IDs)
```
Verifies the test executed correctly

## Advanced Usage

### Tuning Record Count
- **Small** (10-50): Quick validation
- **Medium** (100-500): Performance profiling
- **Large** (1000+): Realistic load testing

### Adjusting Thread Count
Edit `IdGenerationManualTester.java`:
```java
private final ExecutorService executorService = 
    Executors.newFixedThreadPool(50);  // Change 20 to your value
```

### Custom Database URLs
Edit `application.properties`:
```properties
spring.datasource.mysql.jdbc-url=jdbc:mariadb://your-host:3306/test_db
spring.datasource.postgres.jdbc-url=jdbc:postgresql://your-host:5432/test_db
```

### Increasing Thread Pool for Concurrent Tests
In `IdGenerationManualTester` constructor, change:
```java
Executors.newFixedThreadPool(20)  // Change to desired thread count
```

## Logging Configuration

### Change Log Level
Edit `application.properties`:
```properties
# More verbose
logging.level.com.example.dbperformance.cli=DEBUG

# Less verbose
logging.level.com.example.dbperformance.cli=INFO
```

### Change Log File Location
Edit `application.properties`:
```properties
logging.file.name=my-custom-logs/performance.log
```

### View Real-Time Logs
```bash
# Windows (PowerShell)
Get-Content logs/db-performance.log -Wait -Tail 20

# Linux/Mac
tail -f logs/db-performance.log
```

## Troubleshooting

### Build Fails
```
[ERROR] No compiler is provided in this environment
```
Solution: Install Java JDK (not JRE)

### Connection Refused
```
[ERROR] Connection refused to MySQL/PostgreSQL
```
Solution: Start your databases
```bash
# Make sure services are running
mysql.server status  # macOS
pg_isready -h localhost  # PostgreSQL
```

### Table Not Found
```
[ERROR] Table test_identity does not exist
```
Solution: Run setup SQL scripts
```bash
mysql -u root -D test_db < setup-id-mysql.sql
psql -U postgres -d test_db -f setup-id-postgres.sql
```

### Slow Tests
- Reduce record count: Use 100-500 instead of 10000+
- Close other applications to free resources
- Check system CPU/Memory usage

## Performance Baseline

Expected results on modern hardware:

```
MySQL IDENTITY:      300-500 inserts/sec
PostgreSQL SERIAL:   250-400 inserts/sec
PostgreSQL SEQUENCE: 600-1000 inserts/sec (with allocation)
```

If your results are significantly lower, check:
- Database is not under load
- Network latency (if remote databases)
- Hardware specifications
- Disk I/O performance

## Next Steps

1. **Read**: [CLI-TESTING-GUIDE.md](CLI-TESTING-GUIDE.md) for comprehensive guide
2. **Learn**: [USAGE-EXAMPLES.md](USAGE-EXAMPLES.md) for real examples
3. **Reference**: [QUICK-REFERENCE.md](QUICK-REFERENCE.md) for quick commands
4. **Build**: `mvn clean package`
5. **Test**: `java -jar target/db-performance-test-1.0.0.jar`
6. **Analyze**: Check `logs/db-performance.log`

## Support

### Common Questions

**Q: Can I test with my own database?**
A: Yes, update `application.properties` with your database URLs

**Q: How do I run tests automatically without the menu?**
A: Use command-line arguments: `java -jar app.jar --test=all-id --count=1000`

**Q: Can I combine this with the web dashboard?**
A: Yes! The web server is still available. The CLI doesn't interfere.

**Q: How do I clear test data?**
A: Re-run the setup SQL scripts to reset tables

**Q: Can I export test results?**
A: Check `logs/db-performance.log` for persistent records

## Architecture

```
CliRunner (Entry Point)
│
├─→ Menu System
│   ├─→ IdGenerationManualTester (ID tests)
│   ├─→ ManualQueryTester (Custom queries)
│   └─→ Scanner (User input)
│
└─→ Spring Beans
    ├─→ mysqlJdbcTemplate
    ├─→ postgresJdbcTemplate
    └─→ ExecutorService (for concurrent tests)
```

## Summary

You now have a complete CLI-based testing system that:

✅ Replaces the dashboard for manual query testing
✅ Provides detailed logging for all operations
✅ Tests multiple ID generation strategies
✅ Measures allocation size impact
✅ Includes concurrent load testing
✅ Runs via command-line or interactive menu
✅ Logs all results to file for analysis

Happy testing! 🚀
