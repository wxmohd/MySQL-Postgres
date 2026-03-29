# Simple MySQL vs PostgreSQL Comparison

A super simple project to compare MySQL and PostgreSQL performance with just 3 files!

## What You Need
- MySQL/MariaDB installed
- PostgreSQL installed  
- Java installed

## Quick Setup (5 minutes)

### Step 1: Create Databases
```sql
-- In MySQL:
CREATE DATABASE test_db;

-- In PostgreSQL:
CREATE DATABASE test_db;
```

### Step 2: Run Setup Script
Copy the contents of `setup.sql` and run it in both databases.

### Step 3: Update Connection Details
Edit `SimpleTest.java` and change:
- Username/password for your databases
- Database URLs if different

### Step 4: Run the Test
```bash
javac SimpleTest.java
java SimpleTest
```

## What It Tests

1. **Simple SELECT** - Gets 10 users
2. **COUNT query** - Counts all users  
3. **Filtered query** - Finds users over 30

## Sample Output
```
=== Simple MySQL vs PostgreSQL Performance Test ===

Testing MySQL:
  Found 10 users
  Total users: 1005
  Users over 30: 5
  Simple SELECT: 2ms
  COUNT query: 1ms
  Filtered query: 3ms
  Total time: 6ms

==================================================

Testing PostgreSQL:
  Found 10 users
  Total users: 1005
  Users over 30: 5
  Simple SELECT: 3ms
  COUNT query: 2ms
  Filtered query: 2ms
  Total time: 7ms

=== Test Complete ===
```

## What This Shows

- **MySQL**: Usually faster for simple operations
- **PostgreSQL**: Better for complex queries (you'll see this with larger datasets)

## That's It!

This simple test gives you a basic understanding of how the databases perform without all the complexity of frameworks and advanced tools.
