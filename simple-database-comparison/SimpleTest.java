import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleTest {
    
    private static final String MYSQL_URL = "jdbc:mariadb://localhost:3306/test_db";
    private static final String POSTGRES_URL = "jdbc:postgresql://localhost:5432/test_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = ""; 
    
    public static void main(String[] args) {
        System.out.println("=== Simple MySQL vs PostgreSQL Performance Test ===\n");
        
        System.out.println("Testing MySQL:");
        testDatabase("MySQL", MYSQL_URL, USERNAME, PASSWORD);
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        System.out.println("Testing PostgreSQL:");
        testDatabase("PostgreSQL", POSTGRES_URL, "postgres", "postgres");
        
        System.out.println("\n=== Test Complete ===");
    }
    
    private static void testDatabase(String dbName, String url, String user, String pass) {
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            
            long startTime = System.currentTimeMillis();
            simpleSelect(conn);
            long simpleTime = System.currentTimeMillis() - startTime;
            
            startTime = System.currentTimeMillis();
            countUsers(conn);
            long countTime = System.currentTimeMillis() - startTime;
            
            startTime = System.currentTimeMillis();
            findUsersByAge(conn, 30);
            long filterTime = System.currentTimeMillis() - startTime;
            
            System.out.println("  Simple SELECT: " + simpleTime + "ms");
            System.out.println("  COUNT query: " + countTime + "ms");
            System.out.println("  Filtered query: " + filterTime + "ms");
            System.out.println("  Total time: " + (simpleTime + countTime + filterTime) + "ms");
            
            conn.close();
            
        } catch (SQLException e) {
            System.out.println("  Error connecting to " + dbName + ": " + e.getMessage());
        }
    }
    
    private static void simpleSelect(Connection conn) throws SQLException {
        String sql = "SELECT * FROM users";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            int count = 0;
            while (rs.next()) {
                count++;
            }
            System.out.println("  Found " + count + " users");
        }
    }
    
    private static void countUsers(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                System.out.println("  Total users: " + rs.getInt(1));
            }
        }
    }
    
    private static void findUsersByAge(Connection conn, int age) throws SQLException {
        String sql = "SELECT name, email FROM users WHERE age > ? LIMIT 5";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, age);
            
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                }
                System.out.println("  Users over " + age + ": " + count);
            }
        }
    }
}
