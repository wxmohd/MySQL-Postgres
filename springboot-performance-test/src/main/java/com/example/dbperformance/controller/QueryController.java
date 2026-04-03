package com.example.dbperformance.controller;

import com.example.dbperformance.service.IdGenerationTestService;
import com.example.dbperformance.service.QueryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class QueryController {
    
    private final QueryService queryService;
    private final IdGenerationTestService idGenerationTestService;
    
    public QueryController(QueryService queryService, IdGenerationTestService idGenerationTestService) {
        this.queryService = queryService;
        this.idGenerationTestService = idGenerationTestService;
    }
    
    @GetMapping("/query")
    public QueryService.QueryResult executeQuery(
            @RequestParam String db,
            @RequestParam String sql) {
        return queryService.executeQuery(db, sql);
    }
    
    @GetMapping("/compare")
    public QueryService.PerformanceComparison compareQueries(@RequestParam String sql) {
        return queryService.comparePerformance(sql);
    }
    
    @GetMapping("/concurrency")
    public QueryService.ConcurrencyResult testConcurrency(
            @RequestParam String db,
            @RequestParam String sql,
            @RequestParam(defaultValue = "10") int threads) {
        return queryService.testConcurrency(db, sql, threads);
    }
    
    @GetMapping("/test/all")
    public String runAllTests() {
        StringBuilder results = new StringBuilder();
        
        results.append("=== Simple MySQL vs PostgreSQL Performance Test ===\n\n");
        
        QueryService.PerformanceComparison simpleSelect = queryService.comparePerformance("SELECT * FROM users LIMIT 10");
        results.append("Test 1: Simple SELECT (10 records)\n");
        results.append(formatComparison(simpleSelect)).append("\n\n");
        
        QueryService.PerformanceComparison countQuery = queryService.comparePerformance("SELECT COUNT(*) FROM users");
        results.append("Test 2: COUNT query\n");
        results.append(formatComparison(countQuery)).append("\n\n");
        
        QueryService.PerformanceComparison filteredQuery = queryService.comparePerformance("SELECT name, email FROM users WHERE age > 30 LIMIT 5");
        results.append("Test 3: Filtered query (age > 30)\n");
        results.append(formatComparison(filteredQuery)).append("\n\n");
        
        results.append("=== Concurrency Test (10 threads each) ===\n\n");
        
        QueryService.ConcurrencyResult mysqlConcurrency = queryService.testConcurrency("mysql", "SELECT COUNT(*) FROM users", 10);
        results.append("MySQL Concurrency:\n");
        results.append(formatConcurrency(mysqlConcurrency)).append("\n\n");
        
        QueryService.ConcurrencyResult postgresConcurrency = queryService.testConcurrency("postgres", "SELECT COUNT(*) FROM users", 10);
        results.append("PostgreSQL Concurrency:\n");
        results.append(formatConcurrency(postgresConcurrency)).append("\n\n");
        
        results.append("=== Test Complete ===");
        
        return results.toString();
    }
    
    private String formatComparison(QueryService.PerformanceComparison comparison) {
        StringBuilder sb = new StringBuilder();
        sb.append("  MySQL: ").append(comparison.mysqlResult.executionTimeMs).append("ms\n");
        sb.append("  PostgreSQL: ").append(comparison.postgresResult.executionTimeMs).append("ms\n");
        sb.append("  Winner: ").append(comparison.winner);
        if (comparison.differencePercent > 0) {
            sb.append(" (").append(String.format("%.1f", comparison.differencePercent)).append("% faster)");
        }
        return sb.toString();
    }
    
    private String formatConcurrency(QueryService.ConcurrencyResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("  Total time: ").append(result.totalTimeMs).append("ms\n");
        sb.append("  Avg thread time: ").append(String.format("%.1f", result.avgThreadTimeMs)).append("ms\n");
        sb.append("  Min thread time: ").append(result.minThreadTimeMs).append("ms\n");
        sb.append("  Max thread time: ").append(result.maxThreadTimeMs).append("ms");
        return sb.toString();
    }
    
    @GetMapping("/test/ids")
    public String runIdGenerationTests(@RequestParam(defaultValue = "100") int count) {
        return idGenerationTestService.runAllIdTests(count);
    }
}
