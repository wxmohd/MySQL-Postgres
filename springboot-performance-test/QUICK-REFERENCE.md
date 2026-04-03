# Quick Reference - CLI Testing Commands

## Build & Run

```bash
# Build once
mvn clean package

# Start interactive CLI
java -jar target/db-performance-test-1.0.0.jar

# Or use batch file (Windows)
run-cli.bat
```

## Quick Commands

```bash
# Run all ID tests (100 records)
java -jar target/db-performance-test-1.0.0.jar --test=all-id

# Run all ID tests (custom count)
java -jar target/db-performance-test-1.0.0.jar --test=all-id --count=500

# Run specific test
java -jar target/db-performance-test-1.0.0.jar --test=mysql-identity --count=200
```

## Test Categories

### ID Generation Tests
| Test Name | Command | Best For |
|-----------|---------|----------|
| MySQL AUTO_INCREMENT | `--test=mysql-identity` | Single DB apps |
| MySQL UUID | `--test=mysql-uuid` | Distributed systems |
| PostgreSQL SERIAL | `--test=postgres-serial` | Single DB apps |
| PostgreSQL IDENTITY | `--test=postgres-identity` | Modern PostgreSQL |
| PostgreSQL UUID | `--test=postgres-uuid` | Distributed systems |

### Performance Analysis Tests
| Feature | Menu Option | What It Shows |
|---------|-----------|---------------|
| Batch Inserts | Option 8 | Single vs batch performance |
| Concurrent Inserts | Option 9 | Concurrent load handling |
| Allocation Size | Option 6-7 | Impact of allocation batching |
| Query Performance | Option 3 | MySQL vs PostgreSQL speed |

## Interactive Menu Navigation

```
Main Menu (1-4)
  1: Single Test → Choose test (1-9)
  2: Manual Queries → SELECT/UPDATE/COMPARE
  3: All Tests → Run complete test suite
  4: Exit

Enter record count when prompted (press Enter for default 100)
```

## Expected Performance Metrics

### Per Insert Timing
```
MySQL AUTO_INCREMENT:     0.5-2.0 ms/insert   (faster)
PostgreSQL SERIAL:        0.8-2.5 ms/insert   (slower)
PostgreSQL SEQUENCE-50:   0.1-0.5 ms/insert   (fastest)
UUID (any DB):            1.0-3.0 ms/insert   (slower due to size)
```

### Records Per Second
```
MySQL AUTO_INCREMENT:     400+ inserts/sec
PostgreSQL SERIAL:        300-350 inserts/sec
PostgreSQL SEQUENCE-50:   2000+ inserts/sec
UUID (any DB):            300-400 inserts/sec
```

### Concurrent Inserts (10 threads, 100 records each)
```
MySQL:     500-800 inserts/sec total
PostgreSQL: 400-700 inserts/sec total
```

## Custom Query Templates

### Check Database Connection
```sql
mysql> SELECT 1;
postgres# SELECT 1;
```

### View Table Structure
```sql
mysql> DESCRIBE test_identity;
postgres# \d test_identity;
```

### Count Records
```sql
SELECT COUNT(*) FROM test_identity;
SELECT COUNT(*) FROM test_serial;
SELECT COUNT(*) FROM test_uuid;
```

### View Recent Inserts
```sql
SELECT * FROM test_identity ORDER BY created_at DESC LIMIT 10;
SELECT * FROM test_serial ORDER BY created_at DESC LIMIT 10;
SELECT * FROM test_uuid ORDER BY created_at DESC LIMIT 10;
```

### Check ID Distribution
```sql
-- MySQL
SELECT MIN(id), MAX(id), COUNT(*) FROM test_identity;
-- PostgreSQL
SELECT MIN(id), MAX(id), COUNT(*) FROM test_serial;
```

### Performance - Sort with ID
```sql
SELECT id, name FROM test_identity WHERE id > 500 ORDER BY id DESC LIMIT 20;
SELECT id, name FROM test_serial WHERE id > 500 ORDER BY id DESC LIMIT 20;
```

## Common Workflows

### 1. Compare All ID Strategies
```
START → Option 3 (Test All)
→ Enter 500 (for 500 records each)
→ Wait for completion
→ Review logs/db-performance.log
```

### 2. Test Allocation Size Impact
```
START → Option 1
→ Select Option 6 (Allocation 1), count=1000, note time
→ Option 1 again
→ Select Option 7 (Allocation 50), count=1000, compare time
→ Larger allocation should be faster
```

### 3. Custom Performance Test
```
START → Option 2
→ Select Option 3 (Performance Comparison)
→ Enter: SELECT id, name FROM test_identity LIMIT 100;
→ View MySQL vs PostgreSQL time difference
```

### 4. Concurrent Load Test
```
START → Option 1
→ Select Option 9 (Concurrent Inserts)
→ Check "Records/sec" metric for each database
→ Compare total time and thread distribution
```

## Logs Location

```
logs/db-performance.log
```

## Important Directories

```
springboot-performance-test/
├── src/main/java/com/example/dbperformance/
│   ├── cli/                 # New CLI testing components
│   │   ├── CliRunner.java
│   │   ├── ManualQueryTester.java
│   │   └── IdGenerationManualTester.java
│   ├── service/             # Existing services
│   └── controller/          # REST endpoints (optional)
├── src/main/resources/
│   ├── application.properties  # Configure logging here
│   └── logback.xml            # Logging configuration
├── CLI-TESTING-GUIDE.md       # Detailed guide
├── USAGE-EXAMPLES.md          # Examples and outputs
├── setup-id-mysql.sql         # MySQL setup
├── setup-id-postgres.sql      # PostgreSQL setup
└── QUICK-REFERENCE.md         # This file!
```

## Troubleshooting Quick Fixes

| Error | Fix |
|-------|-----|
| "Connection refused" | Start MySQL/PostgreSQL |
| "Table not found" | Run setup SQL scripts |
| "Timeout exceeded" | Reduce record count |
| "Permission denied" | Check database user permissions |
| "No output" | Check if databases are running |

## Database Quick Check

```bash
# MySQL
mysql -u root -e "SELECT version();"

# PostgreSQL
psql -U postgres -c "SELECT version();"
```

## File Size & Cleanup

If `logs/db-performance.log` gets too large:
```bash
# Windows
del logs\db-performance.log
del logs\*.*.log

# Linux/Mac
rm logs/db-performance.log
rm logs/*.*.log
```

## Pro Tips

- ✅ Warm up JVM with small test before large test
- ✅ Run tests multiple times for consistent results
- ✅ Test with realistic record counts (100-5000)
- ✅ Monitor system resources during concurrent tests
- ✅ Check logs after failures for detailed error messages
- ✅ Use `--count=1000` for reliable performance metrics
- ✅ Turn off other applications for accurate results

## Next Steps

1. Build: `mvn clean package`
2. Setup DBs: Run setup SQL scripts
3. Run: `java -jar target/db-performance-test-1.0.0.jar`
4. Choose: Option 3 for comprehensive test
5. Analyze: Check `logs/db-performance.log`
