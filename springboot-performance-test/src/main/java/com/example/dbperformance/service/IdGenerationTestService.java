package com.example.dbperformance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class IdGenerationTestService {
    
    private static final Logger logger = LoggerFactory.getLogger(IdGenerationTestService.class);
    
    private final JdbcTemplate mysqlJdbcTemplate;
    private final JdbcTemplate postgresJdbcTemplate;
    private final ExecutorService executorService;
    
    public IdGenerationTestService(
            @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate,
            @Qualifier("postgresJdbcTemplate") JdbcTemplate postgresJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
        this.postgresJdbcTemplate = postgresJdbcTemplate;
        this.executorService = Executors.newFixedThreadPool(20);
    }
    
    public String runAllIdTests(int recordCount) {
        StringBuilder log = new StringBuilder();
        log.append("\n========================================\n");
        log.append("ID GENERATION STRATEGY COMPARISON\n");
        log.append("Records per test: ").append(recordCount).append("\n");
        log.append("========================================\n\n");
        
        log.append("=== MYSQL IDENTITY (AUTO_INCREMENT) ===\n");
        log.append(testMysqlIdentity(recordCount));
        
        log.append("\n=== MYSQL UUID ===\n");
        log.append(testMysqlUUID(recordCount));
        
        log.append("\n=== POSTGRESQL SERIAL ===\n");
        log.append(testPostgresSerial(recordCount));
        
        log.append("\n=== POSTGRESQL IDENTITY ===\n");
        log.append(testPostgresIdentity(recordCount));
        
        log.append("\n=== POSTGRESQL UUID ===\n");
        log.append(testPostgresUUID(recordCount));
        
        log.append("\n=== POSTGRESQL SEQUENCE (Allocation 1) ===\n");
        log.append(testPostgresSequence(recordCount, 1));
        
        log.append("\n=== POSTGRESQL SEQUENCE (Allocation 50) ===\n");
        log.append(testPostgresSequenceBatch(recordCount, 50));
        
        log.append("\n=== BATCH INSERT COMPARISON ===\n");
        log.append(testBatchInserts(recordCount));
        
        log.append("\n=== CONCURRENT INSERT TEST (10 threads) ===\n");
        log.append(testConcurrentInserts(10, 100));
        
        log.append("\n========================================\n");
        log.append("TEST COMPLETE\n");
        log.append("========================================\n");
        
        return log.toString();
    }
    
    private String testMysqlIdentity(int count) {
        StringBuilder log = new StringBuilder();
        
        try {
            mysqlJdbcTemplate.update("DELETE FROM test_identity");
            long start = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                mysqlJdbcTemplate.update(
                    "INSERT INTO test_identity (name) VALUES (?)",
                    "User_" + i
                );
            }
            
            long duration = System.currentTimeMillis() - start;
            double avgTime = (double) duration / count;
            
            log.append(String.format("Total time: %dms%n", duration));
            log.append(String.format("Avg per insert: %.2fms%n", avgTime));
            log.append(String.format("Inserts/second: %.0f%n", 1000.0 / avgTime));
            
            // Get last ID
            Integer lastId = mysqlJdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM test_identity", Integer.class);
            log.append(String.format("Last generated ID: %d%n", lastId));
            
        } catch (Exception e) {
            log.append("ERROR: ").append(e.getMessage()).append("\n");
        }
        
        return log.toString();
    }
    
    private String testMysqlUUID(int count) {
        StringBuilder log = new StringBuilder();
        
        try {
            mysqlJdbcTemplate.update("DELETE FROM test_uuid");
            long start = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                String uuid = UUID.randomUUID().toString();
                mysqlJdbcTemplate.update(
                    "INSERT INTO test_uuid (id, name) VALUES (?, ?)",
                    uuid, "User_" + i
                );
            }
            
            long duration = System.currentTimeMillis() - start;
            double avgTime = (double) duration / count;
            
            log.append(String.format("Total time: %dms%n", duration));
            log.append(String.format("Avg per insert: %.2fms%n", avgTime));
            log.append(String.format("Inserts/second: %.0f%n", 1000.0 / avgTime));
            log.append("Note: UUID generated in Java (randomUUID)\n");
            
        } catch (Exception e) {
            log.append("ERROR: ").append(e.getMessage()).append("\n");
        }
        
        return log.toString();
    }
    
    private String testPostgresSerial(int count) {
        StringBuilder log = new StringBuilder();
        
        try {
            postgresJdbcTemplate.update("DELETE FROM test_serial");
            long start = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                postgresJdbcTemplate.update(
                    "INSERT INTO test_serial (name) VALUES (?)",
                    "User_" + i
                );
            }
            
            long duration = System.currentTimeMillis() - start;
            double avgTime = (double) duration / count;
            
            log.append(String.format("Total time: %dms%n", duration));
            log.append(String.format("Avg per insert: %.2fms%n", avgTime));
            log.append(String.format("Inserts/second: %.0f%n", 1000.0 / avgTime));
            
            Integer lastId = postgresJdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM test_serial", Integer.class);
            log.append(String.format("Last generated ID: %d%n", lastId));
            
        } catch (Exception e) {
            log.append("ERROR: ").append(e.getMessage()).append("\n");
        }
        
        return log.toString();
    }
    
    private String testPostgresIdentity(int count) {
        StringBuilder log = new StringBuilder();
        
        try {
            postgresJdbcTemplate.update("DELETE FROM test_identity");
            long start = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                postgresJdbcTemplate.update(
                    "INSERT INTO test_identity (name) VALUES (?)",
                    "User_" + i
                );
            }
            
            long duration = System.currentTimeMillis() - start;
            double avgTime = (double) duration / count;
            
            log.append(String.format("Total time: %dms%n", duration));
            log.append(String.format("Avg per insert: %.2fms%n", avgTime));
            log.append(String.format("Inserts/second: %.0f%n", 1000.0 / avgTime));
            
            Integer lastId = postgresJdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM test_identity", Integer.class);
            log.append(String.format("Last generated ID: %d%n", lastId));
            log.append("Note: GENERATED ALWAYS AS IDENTITY\n");
            
        } catch (Exception e) {
            log.append("ERROR: ").append(e.getMessage()).append("\n");
        }
        
        return log.toString();
    }
    
    private String testPostgresUUID(int count) {
        StringBuilder log = new StringBuilder();
        
        try {
            postgresJdbcTemplate.update("DELETE FROM test_uuid");
            long start = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                postgresJdbcTemplate.update(
                    "INSERT INTO test_uuid (name) VALUES (?)",
                    "User_" + i
                );
            }
            
            long duration = System.currentTimeMillis() - start;
            double avgTime = (double) duration / count;
            
            log.append(String.format("Total time: %dms%n", duration));
            log.append(String.format("Avg per insert: %.2fms%n", avgTime));
            log.append(String.format("Inserts/second: %.0f%n", 1000.0 / avgTime));
            log.append("Note: UUID generated by PostgreSQL (gen_random_uuid)\n");
            
        } catch (Exception e) {
            log.append("ERROR: ").append(e.getMessage()).append("\n");
        }
        
        return log.toString();
    }
    
    private String testPostgresSequence(int count, int allocationSize) {
        StringBuilder log = new StringBuilder();
        
        try {
            postgresJdbcTemplate.update("DELETE FROM test_sequence");
            postgresJdbcTemplate.execute("ALTER SEQUENCE test_seq_1 RESTART WITH 1");
            long start = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                // Fetch next value from sequence
                Integer id = postgresJdbcTemplate.queryForObject(
                    "SELECT nextval('test_seq_1')", Integer.class);
                postgresJdbcTemplate.update(
                    "INSERT INTO test_sequence (id, name) VALUES (?, ?)",
                    id, "User_" + i
                );
            }
            
            long duration = System.currentTimeMillis() - start;
            double avgTime = (double) duration / count;
            
            log.append(String.format("Allocation size: %d%n", allocationSize));
            log.append(String.format("Total time: %dms%n", duration));
            log.append(String.format("Avg per insert: %.2fms%n", avgTime));
            log.append(String.format("Inserts/second: %.0f%n", 1000.0 / avgTime));
            log.append("Note: Explicit sequence fetch per insert\n");
            
        } catch (Exception e) {
            log.append("ERROR: ").append(e.getMessage()).append("\n");
        }
        
        return log.toString();
    }
    
    private String testPostgresSequenceBatch(int count, int batchSize) {
        StringBuilder log = new StringBuilder();
        
        try {
            postgresJdbcTemplate.update("DELETE FROM test_batch_sequence");
            postgresJdbcTemplate.execute("ALTER SEQUENCE test_seq_50 RESTART WITH 1");
            long start = System.currentTimeMillis();
            
            int currentId = 0;
            int idsInBatch = 0;
            
            for (int i = 0; i < count; i++) {
                // Fetch batch of IDs at once
                if (idsInBatch == 0) {
                    currentId = postgresJdbcTemplate.queryForObject(
                        "SELECT nextval('test_seq_50')", Integer.class);
                    idsInBatch = batchSize;
                }
                
                int id = currentId - idsInBatch + 1;
                postgresJdbcTemplate.update(
                    "INSERT INTO test_batch_sequence (id, name) VALUES (?, ?)",
                    id, "User_" + i
                );
                idsInBatch--;
            }
            
            long duration = System.currentTimeMillis() - start;
            double avgTime = (double) duration / count;
            
            log.append(String.format("Batch allocation size: %d%n", batchSize));
            log.append(String.format("Total time: %dms%n", duration));
            log.append(String.format("Avg per insert: %.2fms%n", avgTime));
            log.append(String.format("Inserts/second: %.0f%n", 1000.0 / avgTime));
            log.append(String.format("Sequence calls: %d (vs %d individual)%n", 
                (count + batchSize - 1) / batchSize, count));
            log.append("Note: Batch allocation - fetch 50 IDs at once\n");
            
        } catch (Exception e) {
            log.append("ERROR: ").append(e.getMessage()).append("\n");
        }
        
        return log.toString();
    }
    
    private String testBatchInserts(int count) {
        StringBuilder log = new StringBuilder();
        
        int batchSize = 1000;
        int batches = count / batchSize;
        
        try {
            // MySQL batch
            mysqlJdbcTemplate.update("DELETE FROM test_identity");
            long mysqlStart = System.currentTimeMillis();
            
            for (int b = 0; b < batches; b++) {
                StringBuilder sql = new StringBuilder("INSERT INTO test_identity (name) VALUES ");
                List<Object> params = new ArrayList<>();
                
                for (int i = 0; i < batchSize; i++) {
                    if (i > 0) sql.append(", ");
                    sql.append("(?)");
                    params.add("Batch_" + b + "_" + i);
                }
                
                mysqlJdbcTemplate.update(sql.toString(), params.toArray());
            }
            
            long mysqlDuration = System.currentTimeMillis() - mysqlStart;
            
            // PostgreSQL batch
            postgresJdbcTemplate.update("DELETE FROM test_identity");
            long pgStart = System.currentTimeMillis();
            
            for (int b = 0; b < batches; b++) {
                StringBuilder sql = new StringBuilder("INSERT INTO test_identity (name) VALUES ");
                List<Object> params = new ArrayList<>();
                
                for (int i = 0; i < batchSize; i++) {
                    if (i > 0) sql.append(", ");
                    sql.append("(?)");
                    params.add("Batch_" + b + "_" + i);
                }
                
                postgresJdbcTemplate.update(sql.toString(), params.toArray());
            }
            
            long pgDuration = System.currentTimeMillis() - pgStart;
            
            log.append(String.format("Records: %d (in %d batches of %d)%n", count, batches, batchSize));
            log.append(String.format("MySQL batch total: %dms%n", mysqlDuration));
            log.append(String.format("PostgreSQL batch total: %dms%n", pgDuration));
            log.append(String.format("Winner: %s (%.0f%% faster)%n", 
                mysqlDuration < pgDuration ? "MySQL" : "PostgreSQL",
                Math.abs((double)(mysqlDuration - pgDuration) / Math.max(mysqlDuration, pgDuration)) * 100));
            
        } catch (Exception e) {
            log.append("ERROR: ").append(e.getMessage()).append("\n");
        }
        
        return log.toString();
    }
    
    private String testConcurrentInserts(int threads, int insertsPerThread) {
        StringBuilder log = new StringBuilder();
        
        try {
            // Clear tables
            mysqlJdbcTemplate.update("DELETE FROM test_identity");
            postgresJdbcTemplate.update("DELETE FROM test_identity");
            
            // Test MySQL concurrent
            CountDownLatch mysqlLatch = new CountDownLatch(threads);
            long mysqlStart = System.currentTimeMillis();
            
            for (int t = 0; t < threads; t++) {
                final int threadId = t;
                executorService.submit(() -> {
                    for (int i = 0; i < insertsPerThread; i++) {
                        mysqlJdbcTemplate.update(
                            "INSERT INTO test_identity (name) VALUES (?)",
                            "Thread_" + threadId + "_" + i
                        );
                    }
                    mysqlLatch.countDown();
                });
            }
            
            mysqlLatch.await(60, TimeUnit.SECONDS);
            long mysqlDuration = System.currentTimeMillis() - mysqlStart;
            
            // Test PostgreSQL concurrent
            CountDownLatch pgLatch = new CountDownLatch(threads);
            long pgStart = System.currentTimeMillis();
            
            for (int t = 0; t < threads; t++) {
                final int threadId = t;
                executorService.submit(() -> {
                    for (int i = 0; i < insertsPerThread; i++) {
                        postgresJdbcTemplate.update(
                            "INSERT INTO test_identity (name) VALUES (?)",
                            "Thread_" + threadId + "_" + i
                        );
                    }
                    pgLatch.countDown();
                });
            }
            
            pgLatch.await(60, TimeUnit.SECONDS);
            long pgDuration = System.currentTimeMillis() - pgStart;
            
            int totalRecords = threads * insertsPerThread;
            
            log.append(String.format("Threads: %d, Inserts per thread: %d%n", threads, insertsPerThread));
            log.append(String.format("Total records: %d%n", totalRecords));
            log.append(String.format("MySQL total: %dms (%.0f inserts/sec)%n", 
                mysqlDuration, (double)totalRecords / mysqlDuration * 1000));
            log.append(String.format("PostgreSQL total: %dms (%.0f inserts/sec)%n", 
                pgDuration, (double)totalRecords / pgDuration * 1000));
            log.append(String.format("Winner: %s%n", 
                mysqlDuration < pgDuration ? "MySQL" : "PostgreSQL"));
            
        } catch (Exception e) {
            log.append("ERROR: ").append(e.getMessage()).append("\n");
        }
        
        return log.toString();
    }
}
