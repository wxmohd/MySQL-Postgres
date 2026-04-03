package com.example.dbperformance.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ManualQueryTester {
    
    private static final Logger logger = LoggerFactory.getLogger(ManualQueryTester.class);
    
    private final JdbcTemplate mysqlJdbcTemplate;
    private final JdbcTemplate postgresJdbcTemplate;
    
    public ManualQueryTester(
            @Qualifier("mysqlJdbcTemplate") JdbcTemplate mysqlJdbcTemplate,
            @Qualifier("postgresJdbcTemplate") JdbcTemplate postgresJdbcTemplate) {
        this.mysqlJdbcTemplate = mysqlJdbcTemplate;
        this.postgresJdbcTemplate = postgresJdbcTemplate;
    }
    
    /**
     * Execute a custom query on specified database with detailed logging
     */
    public void executeManualQuery(String database, String sql) {
        logger.info("========================================");
        logger.info("MANUAL QUERY EXECUTION");
        logger.info("========================================");
        logger.info("Database: {}", database.toUpperCase());
        logger.info("Query: {}", sql);
        logger.info("----------------------------------------");
        
        JdbcTemplate template = "mysql".equalsIgnoreCase(database) ? 
            mysqlJdbcTemplate : postgresJdbcTemplate;
        
        long startTime = System.currentTimeMillis();
        try {
            List<Map<String, Object>> results = template.queryForList(sql);
            long executionTime = System.currentTimeMillis() - startTime;
            
            logger.info("Execution Time: {}ms", executionTime);
            logger.info("Rows Returned: {}", results.size());
            
            if (!results.isEmpty()) {
                logger.info("----------------------------------------");
                logger.info("RESULTS:");
                for (int i = 0; i < Math.min(results.size(), 10); i++) {
                    logger.info("Row {}: {}", i + 1, results.get(i));
                }
                if (results.size() > 10) {
                    logger.info("... and {} more rows", results.size() - 10);
                }
            }
            logger.info("========================================\n");
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("QUERY ERROR after {}ms:", executionTime, e.getMessage());
            logger.error("Error Details: {}", e.getCause() != null ? 
                e.getCause().getMessage() : e.getMessage());
            logger.info("========================================\n");
        }
    }
    
    /**
     * Execute update query with detailed logging
     */
    public void executeUpdate(String database, String sql, Object... params) {
        logger.info("========================================");
        logger.info("MANUAL UPDATE EXECUTION");
        logger.info("========================================");
        logger.info("Database: {}", database.toUpperCase());
        logger.info("Query: {}", sql);
        if (params.length > 0) {
            logger.info("Parameters: {}", Arrays.toString(params));
        }
        logger.info("----------------------------------------");
        
        JdbcTemplate template = "mysql".equalsIgnoreCase(database) ? 
            mysqlJdbcTemplate : postgresJdbcTemplate;
        
        long startTime = System.currentTimeMillis();
        try {
            int rowsAffected = template.update(sql, params);
            long executionTime = System.currentTimeMillis() - startTime;
            
            logger.info("Execution Time: {}ms", executionTime);
            logger.info("Rows Affected: {}", rowsAffected);
            logger.info("========================================\n");
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("UPDATE ERROR after {}ms: {}", executionTime, e.getMessage());
            logger.info("========================================\n");
        }
    }
    
    /**
     * Compare query performance between MySQL and PostgreSQL
     */
    public void comparePerformance(String sql) {
        logger.info("========================================");
        logger.info("PERFORMANCE COMPARISON");
        logger.info("========================================");
        logger.info("Query: {}", sql);
        logger.info("========================================");
        
        long mysqlTime = executeAndMeasure("mysql", sql);
        long postgresTime = executeAndMeasure("postgres", sql);
        
        logger.info("----------------------------------------");
        logger.info("RESULTS:");
        logger.info("MySQL Time: {}ms", mysqlTime);
        logger.info("PostgreSQL Time: {}ms", postgresTime);
        
        if (mysqlTime > 0 && postgresTime > 0) {
            double faster = Math.max(mysqlTime, postgresTime) / 
                           (double) Math.min(mysqlTime, postgresTime);
            String winner = mysqlTime < postgresTime ? "MySQL" : "PostgreSQL";
            logger.info("Winner: {} ({}x faster)", winner, String.format("%.2f", faster));
        }
        logger.info("========================================\n");
    }
    
    private long executeAndMeasure(String database, String sql) {
        JdbcTemplate template = "mysql".equalsIgnoreCase(database) ? 
            mysqlJdbcTemplate : postgresJdbcTemplate;
        
        long startTime = System.currentTimeMillis();
        try {
            template.queryForList(sql);
            return System.currentTimeMillis() - startTime;
        } catch (Exception e) {
            logger.error("Error in {}: {}", database, e.getMessage());
            return -1;
        }
    }
}
