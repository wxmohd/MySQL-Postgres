# Architecture & System Flow

## Overall System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                      │
│                   (db-performance-test-1.0.0.jar)              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  CliRunner (CommandLineRunner)                            │  │
│  │  • Detects startup mode (interactive or CLI)              │  │
│  │  • Starts appropriate interface                           │  │
│  └──────────┬───────────────────────────────────────────────┘  │
│             │                                                   │
│      ┌──────┴──────┐                                           │
│      │             │                                           │
│      ▼             ▼                                           │
│ ┌─────────────┐  ┌─────────────────┐                         │
│ │ Interactive │  │  Command Line   │                         │
│ │   Menu      │  │  Arguments      │                         │
│ │  Scanner    │  │  Parsing        │                         │
│ └────┬────────┘  └────┬────────────┘                         │
│      │                │                                       │
│      └────────────────┴──────────┐                           │
│                                  ▼                            │
│                  ┌──────────────────────────────┐            │
│                  │  Test Execution             │            │
│                  ├──────────────────────────────┤            │
│                  │ IdGenerationManualTester    │            │
│                  │ ManualQueryTester           │            │
│                  └────────────┬─────────────────┘            │
│                               │                               │
│      ┌────────────────────────┼────────────────────────┐     │
│      │                        │                        │     │
│      ▼                        ▼                        ▼     │
│  ┌─────────┐         ┌──────────────┐         ┌──────────┐ │
│  │Database │         │   Logging    │         │  Metrics │ │
│  │Queries  │         │              │         │Collection│ │
│  └────┬────┘         │ • Console    │         └──────────┘ │
│       │              │ • File       │                      │
│       │              │ • SLF4J      │                      │
│       ▼              └──────────────┘                      │
│  Execution                  │                               │
│                             ▼                               │
└──────────────────────────logs/db-performance.log────────────┘
       │                              │
       ▼                              ▼
   MySQL/MariaDB            PostgreSQL
   Database                 Database
```

## Component Interaction Flow

### 1. Application Startup

```
JVM Start
   │
   ▼
Spring Boot Initialization
   │
   ├─→ Load application.properties
   ├─→ Configure DataSources (MySQL & PostgreSQL)
   ├─→ Setup Connection Pools (HikariCP)
   ├─→ Initialize Beans
   │   ├─→ JdbcTemplate (MySQL)
   │   ├─→ JdbcTemplate (PostgreSQL)
   │   ├─→ IdGenerationManualTester
   │   ├─→ ManualQueryTester
   │   └─→ CliRunner
   │
   ▼
CliRunner.run() Called
   │
   ├─→ Check command-line arguments
   │   ├─→ If none: Show interactive menu
   │   └─→ If present: Execute specific test
   │
   ▼
Ready for user interaction or auto-execution
```

### 2. Test Execution Flow

```
User Selection or CLI Argument
   │
   ▼
IdGenerationManualTester gets called
   │
   ├─→ testMysqlIdentity(count)
   │   ├─→ DELETE old data
   │   ├─→ Start timer
   │   ├─→ Loop: INSERT count times
   │   │   ├─→ Execute SQL
   │   │   ├─→ Log progress
   │   │   └─→ Catch exceptions
   │   ├─→ Stop timer
   │   ├─→ Calculate metrics (time, avg, throughput)
   │   ├─→ Query generated IDs
   │   ├─→ Log results
   │   └─→ Return to menu
   │
   └─→ (All other tests follow similar pattern)
```

### 3. Logging Flow

```
Test Code
   │
   ├─→ logger.info("message")
   │   ├─→ SLF4J (Generic Logging Facade)
   │   │   │
   │   │   ▼
   │   │   Logback (SLF4J Implementation)
   │   │   │
   │   │   └─→ Compare with configured level
   │   │       ├─→ DEBUG: Both console & file logs - VERBOSE
   │   │       ├─→ INFO:  Both console & file logs - NORMAL
   │   │       ├─→ WARN:  Both console & file logs - IMPORTANT
   │   │       └─→ ERROR: Both console & file logs - CRITICAL
   │   │
   │   ├─→ Append to Console (CONSOLE Appender)
   │   │   └─→ Terminal output with formatting
   │   │
   │   └─→ Append to File (FILE Appender)
   │       └─→ logs/db-performance.log with rolling policy
   │
   └─→ Return from logger.info()
```

## Database Connection Flow

```
┌─────────────────────────────────────────┐
│       DataSourceConfig Component        │
└──────────────┬──────────────────────────┘
               │
      ┌────────┴────────┐
      │                 │
      ▼                 ▼
┌──────────────┐  ┌──────────────┐
│   MySQL      │  │ PostgreSQL   │
│ DataSource   │  │ DataSource   │
└──────┬───────┘  └──────┬───────┘
       │                 │
       ▼                 ▼
┌──────────────┐  ┌──────────────┐
│  HikariCP    │  │  HikariCP    │
│  Connection  │  │  Connection  │
│  Pool        │  │  Pool        │
│ (max: 20)    │  │ (max: 20)    │
└──────┬───────┘  └──────┬───────┘
       │                 │
       ▼                 ▼
┌──────────────────────────────┐
│   JdbcTemplate Bean          │
│ (for query execution)        │
└──────────┬───────────────────┘
           │
           ▼
    Query Execution
    (SELECT, INSERT, UPDATE, DELETE)
```

## ID Generation Test Sequence

### MySQL AUTO_INCREMENT Flow
```
Test Start
   │
   ▼
1. Clear old data (DELETE FROM test_identity)
   │
   ▼
2. Start timing
   │
   ▼
3. Execute 100 times:
   INSERT INTO test_identity (name) VALUES (?)
   ├─→ MySQL Auto-generates ID
   ├─→ Creates new row
   ├─→ Returns
   └─→ Log progress
   │
   ▼
4. Stop timing - Calculate metrics
   │
   ├─→ Total time
   ├─→ Average per insert
   ├─→ Inserts/second
   │
   ▼
5. Query validation
   SELECT MAX(id), MIN(id) FROM test_identity
   │
   ▼
6. Log results and return
```

### PostgreSQL SEQUENCE with Allocation Flow
```
Test Start
   │
   ▼
1. Clear old data
2. Reset sequence (ALTER SEQUENCE test_seq_alloc RESTART WITH 1)
   │
   ▼
3. Start timing
   │
   ▼
4. Execute 100 times:
   │
   ├─→ Batch 1 (records 1-50):
   │   ├─→ SELECT nextval('test_seq_alloc') → IDs 1-50 pre-allocated
   │   └─→ INSERT 50 records using allocated IDs
   │
   ├─→ Batch 2 (records 51-100):
   │   ├─→ SELECT nextval('test_seq_alloc') → IDs 51-100 pre-allocated
   │   └─→ INSERT 50 records using allocated IDs
   │
   └─→ Total: Only 2 sequence calls vs 100 calls!
   │
   ▼
5. Stop timing - Calculate metrics
   │
   ▼
6. Log allocation analysis
   ├─→ Total batches
   ├─→ Allocation size
   └─→ Time saved vs single allocation
```

## Concurrent Test Execution

```
Concurrent Insert Test Start (10 threads, 100 records each)
   │
   ▼
1. Clear data
   │
   ▼
2. Create thread pool (size: 10)
3. Start countdown latch (count: 10)
4. Start timer
   │
   ▼
5. Execute for each thread:
   │
   Thread-1 ┐                  Time
   Thread-2 ├─ All run        │───────────→
   Thread-3 │  simultaneously │
   ...      │
   Thread-10 ┘
   │
   Each thread:
   ├─→ Start thread timer
   ├─→ Insert 100 records
   ├─→ Catch exceptions
   ├─→ Decrement countdown
   └─→ Return thread time
   │
   ▼
6. Wait for all threads (countdown = 0)
   │
   ▼
7. Stop global timer
   │
   ▼
8. Calculate metrics:
   ├─→ Total time (from start to last thread finish)
   ├─→ Average thread time
   ├─→ Min/Max thread times
   ├─→ Records/second overall
   └─→ Contention analysis
```

## Query Execution with Timing

```
User Input: SELECT * FROM test_identity LIMIT 5
   │
   ▼
ManualQueryTester.executeManualQuery()
   │
   ├─→ Log header and query
   │
   ├─→ Start timer (System.currentTimeMillis())
   │
   ├─→ TRY:
   │   ├─→ Get JdbcTemplate for selected database
   │   ├─→ Execute queryForList(sql)
   │   ├─→ Collect results
   │   │
   │   ├─→ Stop timer
   │   │
   │   ├─→ Log success metrics:
   │   │   ├─→ Execution time in ms
   │   │   └─→ Rows returned count
   │   │
   │   └─→ Log up to 10 result rows
   │
   ├─→ CATCH Exception:
   │   ├─→ Stop timer
   │   ├─→ Log error with execution time
   │   └─→ Log exception details
   │
   └─→ Return to menu
```

## Performance Comparison Flow

```
User Input: SELECT COUNT(*) FROM table
   │
   ▼
ManualQueryTester.comparePerformance()
   │
   ├─→ Log header
   │
   ├─→ Execute on MySQL:
   │   ├─→ Start timer
   │   ├─→ Query execution
   │   ├─→ Stop timer → mysqlTime
   │   └─→ Log result
   │
   ├─→ Execute on PostgreSQL:
   │   ├─→ Start timer
   │   ├─→ Query execution
   │   ├─→ Stop timer → postgresTime
   │   └─→ Log result
   │
   ├─→ Calculate comparison:
   │   ├─→ Determine faster
   │   ├─→ Calculate speedup factor
   │   └─→ Display winner
   │
   └─→ Return to menu
```

## File I/O and Logging Flow

```
Test Execution
   │
   ├─→ Log Statement
   │   │
   │   └─→ SLF4J Logger.info("message")
   │       │
   │       └─→ Logback Configuration
   │           │
   │           ├─→ ConsoleAppender
   │           │   │
   │           │   ├─→ Format: %d{HH:mm:ss.SSS} [%thread] ...
   │           │   │
   │           │   └─→ Write to System.out (Terminal)
   │           │
   │           └─→ RollingFileAppender
   │               │
   │               ├─→ File: logs/db-performance.log
   │               │
   │               ├─→ Format: %d{yyyy-MM-dd HH:mm:ss.SSS} ...
   │               │
   │               ├─→ Rolling Policy:
   │               │   ├─→ Max file size: 10MB
   │               │   ├─→ Max history: 10 files
   │               │   └─→ Total cap: 100MB
   │               │
   │               └─→ Write to disk
   │
   └─→ Continue execution
```

## Command-Line Argument Processing

```
java -jar app.jar --test=all-id --count=500
   │
   ▼
CliRunner.run(args)
   │
   ├─→ Check if args.length == 0
   │   └─→ Yes: Call showInteractiveMenu()
   │   └─→ No: Call executeCommandLineTest(args)
   │
   ▼
executeCommandLineTest(args)
   │
   ├─→ Parse first argument: --test=all-id
   ├─→ Parse count: --count=500 (or default 100)
   │
   ├─→ Switch on test type:
   │   ├─→ all-id → runAllIdTests(500)
   │   ├─→ mysql-identity → testMysqlIdentity(500)
   │   ├─→ postgres-serial → testPostgresSerial(500)
   │   └─→ ... (other tests)
   │
   └─→ Execute selected test and exit
```

## Data Flow for Single Insert

```
Test Code
INSERT INTO test_identity (name) VALUES (?)
   │
   ▼
JdbcTemplate.update(sql, params)
   │
   ▼
Connection Pool (HikariCP)
   │
   ├─→ Get available connection from pool
   │   (or create new if under max)
   │
   ▼
Database Driver
   │
   ├─→ MariaDB/Postgres Driver
   │
   ▼
Network/Socket
   │
   ├─→ Send SQL + params to database
   ├─→ Wait for response
   │
   ▼
MySQL/PostgreSQL
   │
   ├─→ Parse SQL
   ├─→ Generate ID (AUTO_INCREMENT / SEQUENCE)
   ├─→ Insert row with ID
   ├─→ Return confirmation
   │
   ▼
Return to application
   │
   ├─→ Release connection to pool
   ├─→ Record timing
   ├─→ Update metrics
   │
   └─→ Next iteration
```

## Error Handling Flow

```
Execute Query
   │
   ▼
TRY Block:
   ├─→ Query Execution
   │   ├─→ Connection Error?
   │   │   └─→ Catch SQLException
   │   │
   │   ├─→ Syntax Error?
   │   │   └─→ Catch SQLSyntaxErrorException
   │   │
   │   ├─→ Table Not Found?
   │   │   └─→ Catch SQLSyntaxErrorException
   │   │
   │   └─→ Query Timeout?
   │       └─→ Catch QueryTimeoutException
   │
   ▼
CATCH Block:
   │
   ├─→ Get exception type
   ├─→ Log error message
   ├─→ Log execution time before failure
   ├─→ Log root cause
   │
   └─→ Return gracefully to menu
       (Application stays running)
```

## Interactive Menu Flow Chart

```
START
  │
  ▼
Show Main Menu
  │
  ├─→ User selects 1: Test ID Generation
  │   │
  │   ├─→ Show ID Generation Tests menu
  │   ├─→ User selects test (1-9)
  │   ├─→ User enters record count
  │   ├─→ Execute test
  │   ├─→ Display results
  │   │
  │   └─→ Return to Main Menu
  │
  ├─→ User selects 2: Manual Queries
  │   │
  │   ├─→ Show query menu (SELECT/UPDATE/COMPARE)
  │   ├─→ User enters database and query
  │   ├─→ Execute with logging
  │   ├─→ Display results
  │   │
  │   └─→ Return to Main Menu
  │
  ├─→ User selects 3: Test All
  │   │
  │   ├─→ User enters record count
  │   ├─→ Run all tests sequentially
  │   │   ├─→ testMysqlIdentity()
  │   │   ├─→ testMysqlUUID()
  │   │   ├─→ testPostgresSerial()
  │   │   ├─→ ... (all tests)
  │   │
  │   └─→ Return to Main Menu
  │
  ├─→ User selects 4: Exit
  │   │
  │   ├─→ Close scanner
  │   ├─→ Close database connections
  │   └─→ Application termination
  │
  └─→ Invalid selection → Show menu again
```

## Database Pool Management

```
Application Startup
   │
   ├─→ Create MySQL Connection Pool
   │   ├─→ Driver: MariaDB
   │   ├─→ Connection URL: jdbc:mariadb://localhost:3306/test_db
   │   ├─→ Pool Size: min=5, max=20
   │   ├─→ Idle Timeout: 300s
   │   └─→ Connection Timeout: 20s
   │
   ├─→ Create PostgreSQL Connection Pool
   │   ├─→ Driver: PostgreSQL
   │   ├─→ Connection URL: jdbc:postgresql://localhost:5432/test_db
   │   ├─→ Pool Size: min=5, max=20
   │   ├─→ Idle Timeout: 300s
   │   └─→ Connection Timeout: 20s
   │
   └─→ Both pools ready for use

During Test Execution:
   ├─→ Need connection
   ├─→ Check available connections in pool
   ├─→ If available → Use existing
   ├─→ If > max connections → Wait (timeout 20s)
   ├─→ If < max connections → Create new
   │
   ├─→ Use connection for query
   ├─→ Return connection to pool
   │
   └─→ Connection stays alive until idle timeout

Application Shutdown:
   ├─→ Return all borrowed connections
   ├─→ Close idle connections
   ├─→ Terminate pools
   └─→ Release resources
```

This architecture ensures:
- ✅ Scalable connection management
- ✅ Detailed logging at all levels
- ✅ Flexible test execution
- ✅ Graceful error handling
- ✅ Performance metrics collection
- ✅ Easy integration with both databases
