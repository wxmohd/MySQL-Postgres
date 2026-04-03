package com.example.dbperformance.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class CliRunner implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(CliRunner.class);
    
    private final IdGenerationManualTester idGenerationTester;
    private final ManualQueryTester queryTester;
    
    public CliRunner(IdGenerationManualTester idGenerationTester, 
                     ManualQueryTester queryTester) {
        this.idGenerationTester = idGenerationTester;
        this.queryTester = queryTester;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Check if this was started with specific test arguments
        if (args.length == 0) {
            // Show interactive menu
            showInteractiveMenu();
        } else {
            // Execute specific test based on args
            executeCommandLineTest(args);
        }
    }
    
    private void showInteractiveMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    testIdGenerationMenu(scanner);
                    break;
                case "2":
                    testManualQueriesMenu(scanner);
                    break;
                case "3":
                    testAllIdStrategies(scanner);
                    break;
                case "4":
                    logger.info("Exiting...");
                    running = false;
                    break;
                default:
                    logger.info("Invalid option. Try again.");
            }
        }
        
        scanner.close();
    }
    
    private void printMenu() {
        logger.info("\n");
        logger.info("╔════════════════════════════════════════╗");
        logger.info("║      DATABASE PERFORMANCE TEST CLI     ║");
        logger.info("╚════════════════════════════════════════╝");
        logger.info("1. Test Single ID Generation Strategy");
        logger.info("2. Execute Manual Queries");
        logger.info("3. Test All ID Generation Strategies");
        logger.info("4. Exit");
        logger.info("════════════════════════════════════════");
        logger.info("Enter choice (1-4): ");
    }
    
    private void testIdGenerationMenu(Scanner scanner) {
        logger.info("\n════ ID GENERATION TESTS ════");
        logger.info("1. MySQL IDENTITY (AUTO_INCREMENT)");
        logger.info("2. MySQL UUID");
        logger.info("3. PostgreSQL SERIAL");
        logger.info("4. PostgreSQL IDENTITY");
        logger.info("5. PostgreSQL UUID");
        logger.info("6. PostgreSQL SEQUENCE (Allocation Size 1)");
        logger.info("7. PostgreSQL SEQUENCE (Allocation Size 50)");
        logger.info("8. Batch Insert Comparison");
        logger.info("9. Concurrent Inserts (10 threads, 100 records each)");
        logger.info("0. Back to Main Menu");
        logger.info("════════════════════════════════");
        logger.info("Enter choice: ");
        
        String choice = scanner.nextLine().trim();
        
        logger.info("Enter record count (default 100): ");
        String countInput = scanner.nextLine().trim();
        int count = countInput.isEmpty() ? 100 : Integer.parseInt(countInput);
        
        switch (choice) {
            case "1":
                idGenerationTester.testMysqlIdentity(count);
                break;
            case "2":
                idGenerationTester.testMysqlUUID(count);
                break;
            case "3":
                idGenerationTester.testPostgresSerial(count);
                break;
            case "4":
                idGenerationTester.testPostgresIdentity(count);
                break;
            case "5":
                idGenerationTester.testPostgresUUID(count);
                break;
            case "6":
                idGenerationTester.testPostgresSequenceWithAllocationSize(count, 1);
                break;
            case "7":
                idGenerationTester.testPostgresSequenceWithAllocationSize(count, 50);
                break;
            case "8":
                idGenerationTester.testBatchInsertComparison(count);
                break;
            case "9":
                idGenerationTester.testConcurrentInserts(10, 100);
                break;
            case "0":
                logger.info("Returning to main menu...");
                break;
            default:
                logger.info("Invalid choice");
        }
    }
    
    private void testManualQueriesMenu(Scanner scanner) {
        logger.info("\n════ MANUAL QUERY EXECUTION ════");
        logger.info("1. Execute SELECT Query");
        logger.info("2. Execute UPDATE Query");
        logger.info("3. Compare Performance (MySQL vs PostgreSQL)");
        logger.info("0. Back to Main Menu");
        logger.info("════════════════════════════════");
        logger.info("Enter choice: ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice) {
            case "1":
                executeSelectQuery(scanner);
                break;
            case "2":
                executeUpdateQuery(scanner);
                break;
            case "3":
                comparePerformanceQuery(scanner);
                break;
            case "0":
                logger.info("Returning to main menu...");
                break;
            default:
                logger.info("Invalid choice");
        }
    }
    
    private void executeSelectQuery(Scanner scanner) {
        logger.info("\nEnter database (mysql/postgres): ");
        String database = scanner.nextLine().trim();
        
        logger.info("Enter SQL query: ");
        String sql = scanner.nextLine().trim();
        
        queryTester.executeManualQuery(database, sql);
    }
    
    private void executeUpdateQuery(Scanner scanner) {
        logger.info("\nEnter database (mysql/postgres): ");
        String database = scanner.nextLine().trim();
        
        logger.info("Enter SQL UPDATE query: ");
        String sql = scanner.nextLine().trim();
        
        queryTester.executeUpdate(database, sql);
    }
    
    private void comparePerformanceQuery(Scanner scanner) {
        logger.info("\nEnter SQL query: ");
        String sql = scanner.nextLine().trim();
        
        queryTester.comparePerformance(sql);
    }
    
    private void testAllIdStrategies(Scanner scanner) {
        logger.info("\nEnter record count for each test (default 100): ");
        String countInput = scanner.nextLine().trim();
        int count = countInput.isEmpty() ? 100 : Integer.parseInt(countInput);
        
        logger.info("\n════ RUNNING ALL ID GENERATION TESTS ════\n");
        
        logger.info("• MySQL IDENTITY Test");
        idGenerationTester.testMysqlIdentity(count);
        
        logger.info("• MySQL UUID Test");
        idGenerationTester.testMysqlUUID(count);
        
        logger.info("• PostgreSQL SERIAL Test");
        idGenerationTester.testPostgresSerial(count);
        
        logger.info("• PostgreSQL IDENTITY Test");
        idGenerationTester.testPostgresIdentity(count);
        
        logger.info("• PostgreSQL UUID Test");
        idGenerationTester.testPostgresUUID(count);
        
        logger.info("• PostgreSQL SEQUENCE (Allocation 1)");
        idGenerationTester.testPostgresSequenceWithAllocationSize(count, 1);
        
        logger.info("• PostgreSQL SEQUENCE (Allocation 50)");
        idGenerationTester.testPostgresSequenceWithAllocationSize(count, 50);
        
        logger.info("• Batch Insert Comparison");
        idGenerationTester.testBatchInsertComparison(count);
        
        logger.info("• Concurrent Inserts Test");
        idGenerationTester.testConcurrentInserts(10, 100);
        
        logger.info("\n════ ALL TESTS COMPLETED ════\n");
    }
    
    private void executeCommandLineTest(String[] args) {
        // Execute based on command line arguments
        // Example: java -jar app.jar --test=all-id --count=100
        logger.info("Command line mode: {}", String.join(" ", args));
        
        if (args.length > 0) {
            String firstArg = args[0].toLowerCase();
            int count = extractCountFromArgs(args, 100);
            
            switch (firstArg) {
                case "--test=all-id":
                    logger.info("Running all ID generation tests with count={}", count);
                    runAllIdTests(count);
                    break;
                case "--test=mysql-identity":
                    idGenerationTester.testMysqlIdentity(count);
                    break;
                case "--test=mysql-uuid":
                    idGenerationTester.testMysqlUUID(count);
                    break;
                case "--test=postgres-serial":
                    idGenerationTester.testPostgresSerial(count);
                    break;
                case "--test=postgres-identity":
                    idGenerationTester.testPostgresIdentity(count);
                    break;
                case "--test=postgres-uuid":
                    idGenerationTester.testPostgresUUID(count);
                    break;
                default:
                    logger.info("Unknown command: {}", firstArg);
            }
        }
    }
    
    private void runAllIdTests(int count) {
        idGenerationTester.testMysqlIdentity(count);
        idGenerationTester.testMysqlUUID(count);
        idGenerationTester.testPostgresSerial(count);
        idGenerationTester.testPostgresIdentity(count);
        idGenerationTester.testPostgresUUID(count);
        idGenerationTester.testPostgresSequenceWithAllocationSize(count, 1);
        idGenerationTester.testPostgresSequenceWithAllocationSize(count, 50);
        idGenerationTester.testBatchInsertComparison(count);
        idGenerationTester.testConcurrentInserts(10, 100);
    }
    
    private int extractCountFromArgs(String[] args, int defaultValue) {
        for (String arg : args) {
            if (arg.startsWith("--count=")) {
                try {
                    return Integer.parseInt(arg.substring(8));
                } catch (NumberFormatException e) {
                    logger.warn("Invalid count value, using default: {}", defaultValue);
                }
            }
        }
        return defaultValue;
    }
}
