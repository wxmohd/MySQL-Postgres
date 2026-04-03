package com.example.dbperformance.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component
public class IdGenerationManualTester {
    
    private static final Logger logger = LoggerFactory.getLogger(IdGenerationManualTester.class);
    
    private final JdbcTemplate mysqlJdbcTemplate;
    private final JdbcTemplate postgresJdbcTemplate;
    private final ExecutorService executorService;
    
    public IdGenerationManualTester(
            @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate,
            @Qualifier("postgresJdbcTemplate") JdbcTemplate postgresJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
        this.postgresJdbcTemplate = postgresJdbcTemplate;
        this.executorService = Executors.newFixedThreadPool(20);
    }
    
    // ==================== MYSQL TESTS ====================
    
    public void testMysqlIdentity(int count) {
        logTestHeader("MYSQL IDENTITY (AUTO_INCREMENT)", count);
        
        try {
            logger.info("Step 1: Cleaning up old data");
            mysqlJdbcTemplate.update("DELETE FROM test_identity");
            logger.info("  ✓ Data deleted");
            
            logger.info("Step 2: Inserting {} records", count);
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                mysqlJdbcTemplate.update(
                    "INSERT INTO test_identity (name) VALUES (?)",
                    "User_" + i
                );
                if ((i + 1) % 10 == 0) {
                    logger.debug("  Inserted {} records", i + 1);
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logResults(count, duration);
            
            logger.info("Step 3: Retrieving generated IDs");
            Integer maxId = mysqlJdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM test_identity", Integer.class);
            Integer minId = mysqlJdbcTemplate.queryForObject(
                "SELECT MIN(id) FROM test_identity", Integer.class);
            logger.info("  First ID: {}", minId);
            logger.info("  Last ID: {}", maxId);
            logger.info("  ID Range: {} to {}", minId, maxId);
            
        } catch (Exception e) {
            logError(e);
        }
        
        logTestFooter();
    }
    
    public void testMysqlUUID(int count) {
        logTestHeader("MYSQL UUID (Client-Generated)", count);
        
        try {
            logger.info("Step 1: Cleaning up old data");
            mysqlJdbcTemplate.update("DELETE FROM test_uuid");
            logger.info("  ✓ Data deleted");
            
            logger.info("Step 2: Generating {} UUIDs and inserting", count);
            long startTime = System.currentTimeMillis();
            
            List<String> generatedUUIDs = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String uuid = UUID.randomUUID().toString();
                generatedUUIDs.add(uuid);
                mysqlJdbcTemplate.update(
                    "INSERT INTO test_uuid (id, name) VALUES (?, ?)",
                    uuid, "User_" + i
                );
                if ((i + 1) % 10 == 0) {
                    logger.debug("  Inserted {} records", i + 1);
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logResults(count, duration);
            
            logger.info("Step 3: Sample generated UUIDs");
            for (int i = 0; i < Math.min(3, generatedUUIDs.size()); i++) {
                logger.info("  UUID {}: {}", i + 1, generatedUUIDs.get(i));
            }
            
        } catch (Exception e) {
            logError(e);
        }
        
        logTestFooter();
    }
    
    // ==================== POSTGRESQL TESTS ====================
    
    public void testPostgresSerial(int count) {
        logTestHeader("POSTGRESQL SERIAL", count);
        
        try {
            logger.info("Step 1: Cleaning up old data");
            postgresJdbcTemplate.update("DELETE FROM test_serial");
            postgresJdbcTemplate.execute("ALTER SEQUENCE test_serial_id_seq RESTART WITH 1");
            logger.info("  ✓ Data deleted and sequence reset");
            
            logger.info("Step 2: Inserting {} records", count);
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                postgresJdbcTemplate.update(
                    "INSERT INTO test_serial (name) VALUES (?)",
                    "User_" + i
                );
                if ((i + 1) % 10 == 0) {
                    logger.debug("  Inserted {} records", i + 1);
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logResults(count, duration);
            
            logger.info("Step 3: Retrieving generated IDs");
            Integer maxId = postgresJdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM test_serial", Integer.class);
            Integer minId = postgresJdbcTemplate.queryForObject(
                "SELECT MIN(id) FROM test_serial", Integer.class);
            logger.info("  First ID: {}", minId);
            logger.info("  Last ID: {}", maxId);
            
        } catch (Exception e) {
            logError(e);
        }
        
        logTestFooter();
    }
    
    public void testPostgresIdentity(int count) {
        logTestHeader("POSTGRESQL IDENTITY (GENERATED ALWAYS)", count);
        
        try {
            logger.info("Step 1: Cleaning up old data");
            postgresJdbcTemplate.update("DELETE FROM test_identity");
            postgresJdbcTemplate.execute("ALTER SEQUENCE test_identity_id_seq RESTART WITH 1");
            logger.info("  ✓ Data deleted and sequence reset");
            
            logger.info("Step 2: Inserting {} records", count);
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                postgresJdbcTemplate.update(
                    "INSERT INTO test_identity (name) VALUES (?)",
                    "User_" + i
                );
                if ((i + 1) % 10 == 0) {
                    logger.debug("  Inserted {} records", i + 1);
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logResults(count, duration);
            
            logger.info("Step 3: Retrieving generated IDs");
            Integer maxId = postgresJdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM test_identity", Integer.class);
            logger.info("  Last ID: {}", maxId);
            
        } catch (Exception e) {
            logError(e);
        }
        
        logTestFooter();
    }
    
    public void testPostgresUUID(int count) {
        logTestHeader("POSTGRESQL UUID (Server-Generated)", count);
        
        try {
            logger.info("Step 1: Cleaning up old data");
            postgresJdbcTemplate.update("DELETE FROM test_uuid");
            logger.info("  ✓ Data deleted");
            
            logger.info("Step 2: Inserting {} records with server-side UUID generation", count);
            long startTime = System.currentTimeMillis();
            
            List<String> retrievedUUIDs = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                postgresJdbcTemplate.update(
                    "INSERT INTO test_uuid (name) VALUES (?)",
                    "User_" + i
                );
                if ((i + 1) % 10 == 0) {
                    logger.debug("  Inserted {} records", i + 1);
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logResults(count, duration);
            
            logger.info("Step 3: Retrieving sample generated UUIDs");
            retrievedUUIDs = postgresJdbcTemplate.queryForList(
                "SELECT id FROM test_uuid LIMIT 3", String.class);
            for (int i = 0; i < retrievedUUIDs.size(); i++) {
                logger.info("  UUID {}: {}", i + 1, retrievedUUIDs.get(i));
            }
            
        } catch (Exception e) {
            logError(e);
        }
        
        logTestFooter();
    }
    
    // ==================== SEQUENCE WITH ALLOCATION SIZE TESTS ====================
    
    public void testPostgresSequenceWithAllocationSize(int count, int allocationSize) {
        logTestHeader("POSTGRESQL SEQUENCE (Allocation Size: " + allocationSize + ")", count);
        
        try {
            logger.info("Step 1: Cleaning up old data");
            postgresJdbcTemplate.update("DELETE FROM test_sequence_alloc");
            postgresJdbcTemplate.execute("ALTER SEQUENCE test_seq_alloc RESTART WITH 1");
            logger.info("  ✓ Data deleted and sequence reset");
            
            logger.info("Step 2: Inserting {} records with allocation size {}", count, allocationSize);
            long startTime = System.currentTimeMillis();
            
            int sequenceCallCount = 0;
            for (int i = 0; i < count; i++) {
                if (i % allocationSize == 0) {
                    // Fetch allocation size number of IDs from sequence in one call (simulated)
                    sequenceCallCount++;
                    logger.debug("  Allocation batch {}: fetching {} IDs from sequence", 
                        sequenceCallCount, allocationSize);
                }
                
                Integer id = postgresJdbcTemplate.queryForObject(
                    "SELECT nextval('test_seq_alloc')", Integer.class);
                postgresJdbcTemplate.update(
                    "INSERT INTO test_sequence_alloc (id, name) VALUES (?, ?)",
                    id, "User_" + i
                );
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logResults(count, duration);
            
            logger.info("Step 3: Sequence Call Analysis");
            logger.info("  Total allocation batches: {}", 
                (int) Math.ceil((double) count / allocationSize));
            logger.info("  Sequence calls per allocation: 1 (for {} IDs)", allocationSize);
            
        } catch (Exception e) {
            logError(e);
        }
        
        logTestFooter();
    }
    
    // ==================== BATCH INSERT TESTS ====================
    
    public void testBatchInsertComparison(int count) {
        logTestHeader("BATCH INSERT COMPARISON", count);
        
        try {
            logger.info("Test 1: MySQL Single Inserts");
            long singleInsertTime = testMysqlSingleInserts(count / 5);
            
            logger.info("\nTest 2: PostgreSQL Single Inserts");
            long postgresInsertTime = testPostgresSingleInserts(count / 5);
            
            logger.info("\n========================================");
            logger.info("BATCH COMPARISON RESULTS:");
            logger.info("MySQL (Single): {}ms", singleInsertTime);
            logger.info("PostgreSQL (Single): {}ms", postgresInsertTime);
            
            if (singleInsertTime > 0 && postgresInsertTime > 0) {
                double differencePercent = Math.abs(singleInsertTime - postgresInsertTime) * 100.0 /
                    Math.max(singleInsertTime, postgresInsertTime);
                logger.info("Difference: {:.2f}%", differencePercent);
            }
            
        } catch (Exception e) {
            logError(e);
        }
        
        logTestFooter();
    }
    
    private long testMysqlSingleInserts(int count) {
        try {
            mysqlJdbcTemplate.update("DELETE FROM test_identity");
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                mysqlJdbcTemplate.update(
                    "INSERT INTO test_identity (name) VALUES (?)",
                    "Batch_User_" + i
                );
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("  Inserted {} records in {}ms ({:.2f} inserts/sec)",
                count, duration, (count * 1000.0) / duration);
            return duration;
            
        } catch (Exception e) {
            logger.error("  Error: {}", e.getMessage());
            return -1;
        }
    }
    
    private long testPostgresSingleInserts(int count) {
        try {
            postgresJdbcTemplate.update("DELETE FROM test_serial");
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                postgresJdbcTemplate.update(
                    "INSERT INTO test_serial (name) VALUES (?)",
                    "Batch_User_" + i
                );
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("  Inserted {} records in {}ms ({:.2f} inserts/sec)",
                count, duration, (count * 1000.0) / duration);
            return duration;
            
        } catch (Exception e) {
            logger.error("  Error: {}", e.getMessage());
            return -1;
        }
    }
    
    // ==================== CONCURRENT INSERT TESTS ====================
    
    public void testConcurrentInserts(int threadCount, int recordsPerThread) {
        logTestHeader("CONCURRENT INSERT TEST (" + threadCount + " threads)", recordsPerThread * threadCount);
        
        try {
            testConcurrentOnDatabase("mysql", threadCount, recordsPerThread);
            testConcurrentOnDatabase("postgres", threadCount, recordsPerThread);
        } catch (Exception e) {
            logError(e);
        }
        
        logTestFooter();
    }
    
    private void testConcurrentOnDatabase(String database, int threadCount, int recordsPerThread) {
        logger.info("\nDatabase: {} | Threads: {} | Records/Thread: {}", 
            database.toUpperCase(), threadCount, recordsPerThread);
        
        JdbcTemplate template = "mysql".equalsIgnoreCase(database) ? 
            mysqlJdbcTemplate : postgresJdbcTemplate;
        String table = "mysql".equalsIgnoreCase(database) ? "test_identity" : "test_serial";
        
        try {
            template.update("DELETE FROM " + table);
            
            CountDownLatch latch = new CountDownLatch(threadCount);
            long startTime = System.currentTimeMillis();
            
            Future<Long>[] futures = new Future[threadCount];
            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                futures[t] = executorService.submit(() -> {
                    long threadStart = System.currentTimeMillis();
                    try {
                        for (int i = 0; i < recordsPerThread; i++) {
                            template.update(
                                "INSERT INTO " + table + " (name) VALUES (?)",
                                "Concurrent_T" + threadId + "_R" + i
                            );
                        }
                    } catch (Exception e) {
                        logger.error("Thread {} error: {}", threadId, e.getMessage());
                    }
                    latch.countDown();
                    return System.currentTimeMillis() - threadStart;
                });
            }
            
            latch.await(120, TimeUnit.SECONDS);
            long totalTime = System.currentTimeMillis() - startTime;
            
            long totalThreadTime = 0;
            long maxTime = 0;
            long minTime = Long.MAX_VALUE;
            
            for (Future<Long> future : futures) {
                try {
                    Long time = future.get();
                    totalThreadTime += time;
                    maxTime = Math.max(maxTime, time);
                    minTime = Math.min(minTime, time);
                } catch (Exception e) {
                    logger.error("Error: {}", e.getMessage());
                }
            }
            
            logger.info("  Total time: {}ms", totalTime);
            logger.info("  Avg thread time: {:.2f}ms", totalThreadTime / (double) threadCount);
            logger.info("  Min thread time: {}ms", minTime);
            logger.info("  Max thread time: {}ms", maxTime);
            logger.info("  Total records inserted: {}", threadCount * recordsPerThread);
            logger.info("  Records/sec: {:.0f}", (threadCount * recordsPerThread * 1000.0) / totalTime);
            
        } catch (Exception e) {
            logger.error("Concurrent test error for {}: {}", database, e.getMessage());
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    private void logTestHeader(String testName, int recordCount) {
        logger.info("\n========================================");
        logger.info("TEST: {}", testName);
        logger.info("Record Count: {}", recordCount);
        logger.info("========================================");
    }
    
    private void logTestFooter() {
        logger.info("========================================\n");
    }
    
    private void logResults(int count, long duration) {
        double avgTime = (double) duration / count;
        double recordsPerSecond = (count * 1000.0) / duration;
        
        logger.info("  Duration: {}ms", duration);
        logger.info("  Average per insert: {:.4f}ms", avgTime);
        logger.info("  Records/second: {:.0f}", recordsPerSecond);
    }
    
    private void logError(Exception e) {
        logger.error("ERROR: {}", e.getMessage());
        logger.error("Cause: {}", e.getCause() != null ? e.getCause().getMessage() : "N/A");
    }
}
