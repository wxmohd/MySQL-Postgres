# Spring Boot Database Performance Test

A simple Spring Boot application with HikariCP connection pooling for MySQL vs PostgreSQL performance testing with concurrency support.

## Features

- **Dual Database Support**: Connects to both MySQL (MariaDB) and PostgreSQL simultaneously
- **HikariCP Connection Pooling**: High-performance connection pooling for both databases
- **Concurrency Testing**: Test database performance under concurrent load
- **REST API**: Simple endpoints for query execution and performance comparison
- **Real-time Performance Metrics**: Get execution times and compare databases instantly

## Project Structure

```
springboot-performance-test/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/dbperformance/
│       │       ├── DbPerformanceApplication.java
│       │       ├── config/
│       │       │   └── DataSourceConfig.java
│       │       ├── controller/
│       │       │   └── QueryController.java
│       │       └── service/
│       │           └── QueryService.java
│       └── resources/
│           └── application.properties
```

## Prerequisites

- Java 17+
- Maven
- MySQL/MariaDB running on localhost:3306 with test_db database
- PostgreSQL running on localhost:5432 with test_db database
- 5 million records in each database (use setup scripts from parent directory)

## Setup

1. **Update database credentials** in `src/main/resources/application.properties` if needed:
   - MySQL: username=root, password=(empty)
   - PostgreSQL: username=postgres, password=postgres

2. **Build the application**:
   ```cmd
   cd springboot-performance-test
   mvn clean install
   ```

3. **Run the application**:
   ```cmd
   mvn spring-boot:run
   ```

   Or run the JAR:
   ```cmd
   java -jar target/db-performance-test-1.0.0.jar
   ```

4. **Access the application**:
   - Application runs on: http://localhost:8080
   - API base path: http://localhost:8080/api

## API Endpoints

### 1. Execute Single Query
```
GET /api/query?db={mysql|postgres}&sql={SQL_QUERY}
```

Example:
```
http://localhost:8080/api/query?db=mysql&sql=SELECT%20COUNT(*)%20FROM%20users
```

### 2. Compare Performance
```
GET /api/compare?sql={SQL_QUERY}
```

Example:
```
http://localhost:8080/api/compare?sql=SELECT%20COUNT(*)%20FROM%20users
```

### 3. Concurrency Test
```
GET /api/concurrency?db={mysql|postgres}&sql={SQL_QUERY}&threads={NUMBER}
```

Example:
```
http://localhost:8080/api/concurrency?db=mysql&sql=SELECT%20COUNT(*)%20FROM%20users&threads=20
```

### 4. Run All Tests
```
GET /api/test/all
```

This runs all standard performance tests and returns formatted results.

## HikariCP Configuration

Both databases are configured with:
- **Maximum pool size**: 20 connections
- **Minimum idle**: 5 connections
- **Connection timeout**: 20 seconds
- **Idle timeout**: 5 minutes
- **Max lifetime**: 20 minutes

## Sample Response

### Query Result
```json
{
  "database": "mysql",
  "sql": "SELECT COUNT(*) FROM users",
  "executionTimeMs": 445,
  "rowsReturned": 1,
  "error": null
}
```

### Performance Comparison
```json
{
  "mysqlResult": {
    "database": "mysql",
    "sql": "SELECT COUNT(*) FROM users",
    "executionTimeMs": 445,
    "rowsReturned": 1,
    "error": null
  },
  "postgresResult": {
    "database": "postgres",
    "sql": "SELECT COUNT(*) FROM users",
    "executionTimeMs": 102,
    "rowsReturned": 1,
    "error": null
  },
  "winner": "PostgreSQL",
  "differencePercent": 336.3
}
```

### Concurrency Result
```json
{
  "database": "mysql",
  "sql": "SELECT COUNT(*) FROM users",
  "threadCount": 10,
  "totalTimeMs": 523,
  "avgThreadTimeMs": 145.2,
  "minThreadTimeMs": 98,
  "maxThreadTimeMs": 201
}
```

## Performance Testing Guide

1. **Start with simple queries**:
   ```
   /api/compare?sql=SELECT%20*%20FROM%20users%20LIMIT%2010
   ```

2. **Test aggregation performance**:
   ```
   /api/compare?sql=SELECT%20COUNT(*)%20FROM%20users
   ```

3. **Test filtered queries**:
   ```
   /api/compare?sql=SELECT%20name,%20email%20FROM%20users%20WHERE%20age%20>%2030%20LIMIT%205
   ```

4. **Run concurrency tests**:
   ```
   /api/concurrency?db=mysql&sql=SELECT%20COUNT(*)%20FROM%20users&threads=20
   /api/concurrency?db=postgres&sql=SELECT%20COUNT(*)%20FROM%20users&threads=20
   ```

5. **Get complete test results**:
   ```
   /api/test/all
   ```

## Expected Results

With 5 million records:

**Limited queries (10 records)**:
- PostgreSQL: ~15ms
- MySQL: ~15-20ms
- Similar performance

**COUNT queries**:
- PostgreSQL: ~100-150ms
- MySQL: ~400-500ms
- PostgreSQL 3-4x faster

**Full table scan**:
- Varies based on system load
- Both databases 1500ms-2500ms

**Concurrency (10 threads)**:
- Watch for connection pool exhaustion
- PostgreSQL typically handles concurrent COUNT queries better
- MySQL may show higher thread time variance

## Troubleshooting

1. **Connection pool exhausted**:
   - Reduce thread count in concurrency tests
   - Increase `maximum-pool-size` in application.properties

2. **Connection timeout**:
   - Check if databases are running
   - Verify connection URLs in application.properties

3. **Slow first query**:
   - First query warms up the connection pool
   - Subsequent queries will be faster

4. **Build errors**:
   - Ensure Java 17+ is installed: `java -version`
   - Ensure Maven is installed: `mvn -version`

## Notes

- The application creates connection pools on startup
- First query may be slower due to pool initialization
- Concurrency tests use a thread pool of 20 threads maximum
- All queries use parameterized statements for security
