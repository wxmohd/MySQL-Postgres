package com.example.dbperformance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class QueryService {
    
    private static final Logger logger = LoggerFactory.getLogger(QueryService.class);
    
    private final JdbcTemplate mysqlJdbcTemplate;
    private final JdbcTemplate postgresJdbcTemplate;
    private final ExecutorService executorService;
    
    public QueryService(
            @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate,
            @Qualifier("postgresJdbcTemplate") JdbcTemplate postgresJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
        this.postgresJdbcTemplate = postgresJdbcTemplate;
        this.executorService = Executors.newFixedThreadPool(20);
    }
    
    public QueryResult executeQuery(String db, String sql) {
        long startTime = System.currentTimeMillis();
        JdbcTemplate template = "mysql".equalsIgnoreCase(db) ? mysqlJdbcTemplate : postgresJdbcTemplate;
        
        try {
            List<Map<String, Object>> results = template.queryForList(sql);
            long executionTime = System.currentTimeMillis() - startTime;
            return new QueryResult(db, sql, executionTime, results.size(), null);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new QueryResult(db, sql, executionTime, 0, e.getMessage());
        }
    }
    
    public ConcurrencyResult testConcurrency(String db, String sql, int threadCount) {
        JdbcTemplate template = "mysql".equalsIgnoreCase(db) ? mysqlJdbcTemplate : postgresJdbcTemplate;
        CountDownLatch latch = new CountDownLatch(threadCount);
        long startTime = System.currentTimeMillis();
        
        Future<Long>[] futures = new Future[threadCount];
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            futures[i] = executorService.submit(() -> {
                long threadStart = System.currentTimeMillis();
                try {
                    template.queryForList(sql);
                } catch (Exception e) {
                    logger.error("Thread {} error: {}", threadId, e.getMessage());
                }
                latch.countDown();
                return System.currentTimeMillis() - threadStart;
            });
        }
        
        try {
            latch.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        long totalThreadTime = 0;
        long maxThreadTime = 0;
        long minThreadTime = Long.MAX_VALUE;
        
        for (Future<Long> future : futures) {
            try {
                Long threadTime = future.get();
                totalThreadTime += threadTime;
                maxThreadTime = Math.max(maxThreadTime, threadTime);
                minThreadTime = Math.min(minThreadTime, threadTime);
            } catch (Exception e) {
                logger.error("Error getting future result: {}", e.getMessage());
            }
        }
        
        double avgThreadTime = threadCount > 0 ? (double) totalThreadTime / threadCount : 0;
        
        return new ConcurrencyResult(db, sql, threadCount, totalTime, avgThreadTime, minThreadTime, maxThreadTime);
    }
    
    public PerformanceComparison comparePerformance(String sql) {
        QueryResult mysqlResult = executeQuery("mysql", sql);
        QueryResult postgresResult = executeQuery("postgres", sql);
        return new PerformanceComparison(mysqlResult, postgresResult);
    }
    
    public static class QueryResult {
        public String database;
        public String sql;
        public long executionTimeMs;
        public int rowsReturned;
        public String error;
        
        public QueryResult(String database, String sql, long executionTimeMs, int rowsReturned, String error) {
            this.database = database;
            this.sql = sql;
            this.executionTimeMs = executionTimeMs;
            this.rowsReturned = rowsReturned;
            this.error = error;
        }
    }
    
    public static class ConcurrencyResult {
        public String database;
        public String sql;
        public int threadCount;
        public long totalTimeMs;
        public double avgThreadTimeMs;
        public long minThreadTimeMs;
        public long maxThreadTimeMs;
        
        public ConcurrencyResult(String database, String sql, int threadCount, long totalTimeMs, 
                                 double avgThreadTimeMs, long minThreadTimeMs, long maxThreadTimeMs) {
            this.database = database;
            this.sql = sql;
            this.threadCount = threadCount;
            this.totalTimeMs = totalTimeMs;
            this.avgThreadTimeMs = avgThreadTimeMs;
            this.minThreadTimeMs = minThreadTimeMs;
            this.maxThreadTimeMs = maxThreadTimeMs;
        }
    }
    
    public static class PerformanceComparison {
        public QueryResult mysqlResult;
        public QueryResult postgresResult;
        public String winner;
        public double differencePercent;
        
        public PerformanceComparison(QueryResult mysqlResult, QueryResult postgresResult) {
            this.mysqlResult = mysqlResult;
            this.postgresResult = postgresResult;
            
            if (mysqlResult.error != null && postgresResult.error != null) {
                this.winner = "Both failed";
            } else if (mysqlResult.error != null) {
                this.winner = "PostgreSQL (MySQL failed)";
            } else if (postgresResult.error != null) {
                this.winner = "MySQL (PostgreSQL failed)";
            } else if (mysqlResult.executionTimeMs < postgresResult.executionTimeMs) {
                this.winner = "MySQL";
                this.differencePercent = ((double)(postgresResult.executionTimeMs - mysqlResult.executionTimeMs) / mysqlResult.executionTimeMs) * 100;
            } else {
                this.winner = "PostgreSQL";
                this.differencePercent = ((double)(mysqlResult.executionTimeMs - postgresResult.executionTimeMs) / postgresResult.executionTimeMs) * 100;
            }
        }
    }
}
