# HiPerAnalyzer (Hibernate Performance Analyzer)

#### Requirements
- Spring Boot: 3.x.x 
- Java: >= 17 
- Hibernate: 6.x
- Spring Boot 3.x

https://mvnrepository.com/artifact/io.github.waldemargr/n-plus-1-detector
```xml
<dependency>
    <groupId>io.github.waldemargr</groupId>
    <artifactId>n-plus-1-detector</artifactId>
    <version>1.5.0</version>
</dependency>
```
How to run:
```java
@EnableAdditionalSelectBeforeInsertDetector
@EnableQueryPlanAnalysis
@EnableRelationshipAnalysis
@NPlus1QueryDetection
@EnableHashCodeAnalysis

@SpringBootApplication
public class SpringApp {

}
```

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=waldemarGr_n-plus-1-detector&metric=bugs)](https://sonarcloud.io/summary/new_code?id=waldemarGr_n-plus-1-detector)

## Project Overview

### Current Features
#### N+1 Query Detection:
- **Objective:** Identify and resolve N+1 query problems that can lead to performance bottlenecks in applications using
  Hibernate.
- **Implementation:** Use `@NPlus1QueryDetection` to analyze queries statistic and provide insights into
  potential
  N+1 query issues.
#### HashCode Analysis:
- **Objective:** Ensure that hashCode methods in your `@Entity` are implemented correctly to follow best practices and
  avoid common pitfalls.
- **Implementation:** Use the `@EnableHashCodeAnalysis` annotation to activate the hashCode analysis functionality. This
  annotation triggers a scan for `@Entity` classes starting from the package path specified where you put the
  `@EnableHashCodeAnalysis`. It performs analysis of hashCode methods at the `bytecode` level, ensuring that these
  methods are based on stable fields and avoiding common mistakes.
#### Relationship Entity Analysis:
- **Objective:** Identify opportunities for optimizing entity fields in relation to specific relationships, improving
  performance and memory efficiency.
- **Implementation:** Use the `@EnableRelationshipAnalysis` annotation to activate relationship entity analysis. This
  annotation initiates a scan of your `@Entity` classes, starting from the package path
  where `@EnableRelationshipAnalysis`
  is applied. It performs a bytecode-level analysis to evaluate how entity fields, particularly collections like List,
  Set, or Map, are used in relationships, providing recommendations for potential optimizations.
#### Plan Execution Logging (Mysql/Oracle)
- **Objective:** Provide detailed insights into the execution plans of app queries, helping to identify and address
  performance issues in your database interactions
- **Implementation:** Use the `@EnableQueryPlanAnalysis` annotation to enable logging of query execution plans. By
  applying this annotation, the application will log the execution plan for every query executed by Hibernate, offering
  a deeper look into how queries are being processed.
#### Additional Select Before Insert Detector
- **Objective:** Detecting redundant SELECT queries during entity saving.
- **Implementation:** Use the `@EnableAdditionalSelectBeforeInsertDetector



## Optimization List

To ensure your application is optimized and free from common Hibernate pitfalls, follow these recommendations:

1. **Set `spring.jpa.open-in-view = false` in `application.yaml`:**
    - **Reason:** Disabling `open-in-view` helps prevent the common issue of lazy loading outside of transactions, which
      can lead to unexpected queries being executed. By ensuring that all database interactions happen within a defined
      transactional context, you reduce the risk of N+1 query problems and improve overall performance.
   - Faster Transaction Completion: Speeds up transactions, saving you those precious milliseconds.
    - **Heads up** If you disable open-in-view and start seeing errors related to data fetching during tests, it's a
      strong indication that your application has N+1 problems.

2. **In a `@OneToMany` relationship, prefer using a `Set<String, String>` for key-value properties that are not reused
   between entity instances instead of an entity:**
    - **Reason:** When you store simple properties like key-value pairs directly in a `Set<String, String>`, rather than
      as separate entities, it avoids additional SQL queries during inserts and updates. This can significantly improve
      performance by reducing the overhead of managing multiple entities and the associated database operations.

3. **Prefer `Set` over `List` in relationships:**
    - ##### MultiplebagFetchException
    - **Reason:** In Hibernate, a `List` is treated as a "bag," which can lead to inefficiencies. A `Set`, on the other
      hand, ensures uniqueness and is generally more performant for handling collections in relationships. By using
      a `Set`, you can avoid the potential pitfalls of using a `List`, such as redundant queries and unexpected behavior
      during collection updates.
    - **Bug: If** you’re using two List collections in an entity and try optimizing them with EntityGraph, get ready for
      some serious headaches.
4. **Use getReferenceById** when you only need to reference an entity without fetching its data
   ```java 
     Customer customer = customerRepository.getReferenceById(customerId);
     Order order = new Order();
     order.setCustomer(customer);
     corderRepository.persist(order); 
   ```
    - **Why:** The getReferenceById method gives you a proxy reference to the entity without actually querying the
      database for its data. This is super handy when you want to link entities or set up relationships but don’t need
      the complete entity details right away. By using this method, you avoid those extra database hits
    - **Save Resources:** getReferenceById skips the full entity load, saving memory (no duplicate entities due to dirty
      checking), CPU, and an extra DB query.

5. **Select before Insert** If you notice Hibernate running a SELECT before an INSERT, it’s worth figuring out why.
    - This **only** happens when the entity’s `ID` is not `null` - check is the record exist
    - **Reason:** Check out the implementation of the save method
       ```java 
       
       @Override
       @Transactional
       public <S extends T> S save(S entity) {
          Assert.notNull(entity, "Entity must not be null");
          if (entityInformation.isNew(entity)) {
             entityManager.persist(entity);
             return entity;
          } else {
             return entityManager.merge(entity);
          }
       }  
       ```
    - **Here’s a quick overview.:**  `isNew` method decides what to do. But, guess what? It first runs a SELECT to check
      if the entity is truly new. It’s like an employer checking whether you've already received a raise before giving
      you another one. You’d probably agree that in this case, it’s better if they skip the extra check and just give
      you the raise you’ve earned.
        - Consider Adding '@Version' for proper Entity Versioning, When the version is null, Hibernate knows it doesn’t
          need to perform a check query That's what I need! One less select. Nice to know!
        - Or implements Persistable interface [Spring Data Persistable API][11]. Is an elegant.

6. **Prefer Using `FetchType.LAZY` in Relationships**
    - **Avoiding Unnecessary Data Loading**: Often, you don't need to load all related entities when querying a parent
      entity. To retrieve additional information, use the appropriate mechanisms like Fetch Joins or EntityGraphs
   - **If You still Need `FetchType.EAGER`, You Might Have an N+1 Problem** Need FetchType.EAGER because you’d get an
     exception outside a transaction? Well, you’ve just invited the N+1 problem!

7. **Fetching Exactly What You Need**
    - **Fetch Joins** allow you to load related entities in a single query, preventing the N+1 problem by avoiding
      multiple queries for associations.

   ```java
   @Query("SELECT o FROM Order o JOIN FETCH o.customer JOIN FETCH o.orderItems WHERE o.id = :id")
   Order findOrderWithCustomerAndItems(@Param("id") Long id); 
   ```
    - **EntityGraphs** let you dynamically specify which associations to load eagerly, helping to prevent unnecessary
      data loading and N+1 queries.
   ```java
   @Repository
   public interface OrderRepository extends JpaRepository<Order, Long> {
       @EntityGraph(attributePaths = { "customer", "orderItems" })
       Order findById(Long id);
   }
   ```
8. **`@DynamicUpdate` for Large Tables or large data**
    - **Why:** The `@DynamicUpdate` annotation ensures that Hibernate generates SQL update statements that include only
      the columns that have been changed. This can be particularly beneficial for large tables or entities with many
      columns. For example, if you have a User entity with 30 columns and you only update the lastname field,
      @DynamicUpdate will ensure that only the gender column is included in the SQL update statement, rather than all 30
      columns. This can significantly reduce the amount of data sent to the database and improve performance.
9. **`@DynamicInsert` to Avoid Sending Nulls for Large Tables**
    - annotation makes Hibernate generate SQL INSERT statements that include only non-null columns. If your entity has
      many null values, @DynamicInsert ensures that only columns with actual values are sent to the database.
10. **Avoid Default hashCode and equals from `@EqualsAndHashCode` for  `@Entity`**
    - **Why:** The hashCode and equals methods should be based on fields that do not change over the lifetime of an
      object. By default, @lombok's `@EqualsAndHashCode` may include mutable fields, which can lead to inconsistencies.
      To avoid this, it’s better to base these methods on a stable identifier, such as a UUID or NaturalId generated by
      your application.
        - If possible, use immutable fields to simplify the implementation and avoid potential issues.
    - **Why Not Use @Id and @GeneratedValue for hashCode:** The ID is assigned only after persisting to the database, so
      before saving, the ID doesn’t exist and can lead to incorrect behavior.
    - **Best Practice:** For @Entity classes, implement equals and hashCode methods yourself.
11. Avoiding N+1 Issues with @Data and @ToString
    - @Data and @ToString from Lombok generates toString methods that include all fields, even lazy-loaded collections.
      This can cause N+1 queries when toString triggers lazy-loading
12. Efficient Logging with log.trace() and Avoiding Unnecessary db Calls
    - Using trace(String format, Object... arguments); with the two-argument form helps prevent unnecessary computations
      e.g toSting, including potential
      N+1 issues caused by toString calls.
     ```properties
    logging.level.root=INFO
    ```
    ```JAVA
     public void logTrace(){
         log.trace("Processing order: " + order);  // `toString` is resolved even if TRACE is disabled
         log.trace("Processing order: {}", order.toString());  // `toString` is not resolved 
    }
    ``` 
13. **Use `@Transactional(readOnly = true)` Wisely** Be sure you’re using it correctly
    - **Why:** Performance Boost: By marking a transaction as read-only, you let the database optimize queries, skipping
      write-related overhead. This typically results in faster query execution and better overall performance. Memory
      Efficiency: Read-only transactions consume less memory because they avoid the extra resources needed for managing
      data writes and tracking changes.
        - In read-write transaction, the dirty checking mechanism creates a copy of the entity to compare if changes
          have occurred. This results in approximately double the memory usage, as both the original and modified states
          of the entity are stored in memory.
14. **Do not use `@Transactional` for read db operations and external calls.**
    - Victor Rentea, in his talk, suggests that it is often unnecessary to use transactions for REST, SOAP connections,
      or simple read operations from the database. He referred to this as "naive use of transactions," which can lead to
      unnecessary overhead and inefficiency.
      He’s probably right, and I should quickly check how many times I’ve done this in my code.
15. **Lazy Connection Acquisition by `spring.datasource.hikari.auto-commit=false`**
    -I know it sounds crazy, and the documentation doesn't explicitly mention it, but it actually works this way.
    Vladimír explained it in article on [Spring Transaction Connection Management][9]
    ```JAVA
    @Transactional
    public void typicalTransaction() {
        // Access to the database is obtained here
        doSomething(); // 2s
        doCallByRest(); // 2s
        doCallBySoap(); // 2s
        userRepository.setUser(); // Only here do we actually need database access
    }
    // After the transaction ends, the connection is returned to the pool
    ```
    - Notice that the database connection was obtained for 6 seconds but remained unused. Other resources had to wait
      until the connection was released back to the connection pool.
     ```PROPERTIES
    spring.datasource.hikari.auto-commit=false
    spring.jpa.properties.hibernate.connection.provider_disables_autocommit=true
    ```
    ```JAVA
    @Transactional
    public void typicalTransaction() {
        doSomething(); 
        doCallByRest();
        doCallBySoap(); 
        // Access to the database is obtained here
        userRepository.setUser(); // Only here do we actually need database access
    }
    // After the transaction ends, the connection is returned to the pool
    ```
    - By setting `spring.datasource.hikari.auto-commit=false`, the connection to the database is only established when
      it
      is actually needed, not immediately at the start of the transaction.

16. todo TransactionTemplate
17. **Level 1 Cache in Hibernate**
    - operates within the scope of a single transaction.
    - The cache uses the entity’s unique identifier (ID) only.
    ```JAVA
    @Transactional 
    public void demonstrateL1Cache() {
        // First retrieval - hits the database
        Customer customer = customerRepository.findById(1L).orElse(null);
        System.out.println(customer.getName());

        // Second retrieval - should be served from L1 cache, no DB hit
        Customer customer = customerRepository.findById(1L).orElse(null);
        System.out.println(customer.getName());
    
        // Flush to synchronize changes with the database
        customerRepository.flush(); 
        // At this point, the database is updated with the new name, but L1 cache still holds 'customer' with the updated state.
    
        entityManager.detach(customer1);
        // Detaching the entity means it is no longer in the L1 cache - hits the database
        Customer customer = customerRepository.findById(1L).orElse(null);
    
        entityManager.clear();
        // Load the entity again - this will hit the database because the cache was cleared or detach
        Customer customer = customerRepository.findById(1L).orElse(null);
        System.out.println("After Clear: " + customer.getName());
    }
    ```
    - **Understanding Hibernate Level 1 Cache Implementation**  it’s useful to look at a simplified view of its internal
      implementation.
       ```JAVA
       Map<EntityUniqueKey, Object> entitiesByUniqueKey = new HashMap<>();
       ```
18. **Dirty checking** works within the context of a transaction to automatically detect changes in an entity. It
    compares the current state of the entity with its original state and saves any detected changes when the
    transaction commits.
     ```Java
    @Transactional
    public void updateUser(Long userId, String newName) {
        User userer = userRepository.findById(userId).orElseThrow();
        userer.setName(newName);
        //  No need to manually save; dirty checking will work its magic
    }
    ```
19. **SaveAll() batch insert** //todo

20. **A key takeaway: Understand Your Logs**
    - Read the logs and understand what they mean. This library simply analyzes the logs and identifies issues if they
      arise. You can achieve the same result by analyzing and understanding Hibernate logs yourself.
    ```property
    logging.level.org.hibernate.SQL=debug
    logging.level.org.hibernate.orm.jdbc.bind=trace
    ```
21. **Execution Plan for Oracle**
    OK, so when you're running queries on an Oracle DB, the execution plan shows you how Oracle decides to pull data and
    execute the query. Understanding this plan helps us find out where things can be optimized. Here’s a list of common
    operations you'll see in an execution plan:

    - TABLE ACCESS (FULL): Full table scans. If you see this a lot, you probably need better indexing.
    - INDEX RANGE SCAN: This is better than a full table scan, but there's always room for tweaking indexes.
    - NESTED LOOPS: These can get really expensive with large data sets. Might want to switch to hash joins or filter
      the data
      more efficiently.
    - HASH JOIN: Good for big tables but might need some memory tuning to keep things fast.
    - SORT (ORDER BY): Sorting can be a killer. Check if there’s a way to use an index that keeps things sorted for
      you.Execution plan for Oracle

| Operation                | Option                | Description                                                                                                   | Optimization Priority |
|--------------------------|-----------------------|---------------------------------------------------------------------------------------------------------------|-----------------------|
| **AND-EQUAL**            | -                     | Accepts multiple rowid sets, returns the intersection, eliminates duplicates. Used for single-column indexes. | Low                   |
| **BITMAP CONVERSION**    | TO ROWIDS             | Converts bitmap representations to actual rowids.                                                             | Medium                |
|                          | FROM ROWIDS           | Converts rowids to a bitmap representation.                                                                   | Medium                |
|                          | COUNT                 | Returns the number of rowids if actual values are not needed.                                                 | Low                   |
| **BITMAP INDEX**         | SINGLE VALUE          | Looks up the bitmap for a single key value.                                                                   | Medium                |
|                          | RANGE SCAN            | Retrieves bitmaps for a key value range.                                                                      | Medium                |
|                          | FULL SCAN             | Performs a full scan of a bitmap index if there is no start or stop key.                                      | High                  |
| **BITMAP MERGE**         | -                     | Merges several bitmaps from a range scan into one bitmap.                                                     | Medium                |
| **BITMAP MINUS**         | -                     | Subtracts bits of one bitmap from another. Can be used with non-negated predicates.                           | High                  |
| **BITMAP OR**            | -                     | Computes the bitwise OR of two bitmaps.                                                                       | Medium                |
| **BITMAP AND**           | -                     | Computes the bitwise AND of two bitmaps.                                                                      | Medium                |
| **BITMAP KEY ITERATION** | -                     | Takes each row from a table row source, finds the corresponding bitmap from a bitmap index, merges bitmaps.   | Medium                |
| **CONNECT BY**           | -                     | Retrieves rows in hierarchical order for a query with a CONNECT BY clause.                                    | Medium                |
| **CONCATENATION**        | -                     | Accepts multiple sets of rows, returns the union-all of the sets.                                             | Medium                |
| **COUNT**                | -                     | Counts the number of rows selected from a table.                                                              | Low                   |
| **STOPKEY**              | -                     | Limits rows returned by ROWNUM expression in the WHERE clause.                                                | Low                   |
| **DOMAIN INDEX**         | -                     | Retrieves rowids from a domain index.                                                                         | Medium                |
| **FILTER**               | -                     | Accepts a set of rows, eliminates some, and returns the rest.                                                 | Medium                |
| **FIRST ROW**            | -                     | Retrieves only the first row selected by a query.                                                             | Low                   |
| **FOR UPDATE**           | -                     | Retrieves and locks rows selected by a query with a FOR UPDATE clause.                                        | Medium                |
| **HASH JOIN**            | -                     | Joins two sets of rows, useful for large data sets. CBO builds hash table on the join key.                    | High                  |
|                          | ANTI                  | Hash anti-join.                                                                                               | High                  |
|                          | SEMI                  | Hash semi-join.                                                                                               | High                  |
| **INDEX**                | UNIQUE SCAN           | Retrieves a single rowid from an index.                                                                       | Medium                |
|                          | RANGE SCAN            | Retrieves one or more rowids from an index in ascending order.                                                | Medium                |
|                          | RANGE SCAN DESCENDING | Retrieves one or more rowids from an index in descending order.                                               | Medium                |
|                          | FULL SCAN             | Retrieves all rowids from an index when there is no start or stop key, in ascending order.                    | High                  |
|                          | FULL SCAN DESCENDING  | Retrieves all rowids from an index when there is no start or stop key, in descending order.                   | High                  |
|                          | FAST FULL SCAN        | Retrieves all rowids and column values using multiblock reads.                                                | High                  |
|                          | SKIP SCAN             | Retrieves rowids from a concatenated index without using leading columns.                                     | Medium                |
| **INLIST ITERATOR**      | -                     | Iterates over the next operation for each value in the IN-list predicate.                                     | Medium                |
| **INTERSECTION**         | -                     | Accepts two sets of rows, returns the intersection, eliminating duplicates.                                   | Medium                |
| **MERGE JOIN**           | -                     | Joins two sets of rows, combining each row from one set with matching rows from the other.                    | High                  |
|                          | OUTER                 | Merge join operation for outer join.                                                                          | High                  |
|                          | ANTI                  | Merge anti-join.                                                                                              | High                  |
|                          | SEMI                  | Merge semi-join.                                                                                              | High                  |
|                          | CARTESIAN             | Results from tables without join conditions. Can occur even with a join.                                      | High                  |
| **MINUS**                | -                     | Accepts two sets of rows, returns rows in the first set not in the second, eliminating duplicates.            | Medium                |
| **NESTED LOOPS**         | -                     | Joins two sets of rows by comparing each row of the outer set with each row of the inner set.                 | High                  |
|                          | OUTER                 | Nested loops operation for outer join.                                                                        | High                  |
| **PARTITION**            | SINGLE                | Accesses one partition.                                                                                       | Medium                |
|                          | ITERATOR              | Accesses many partitions (a subset).                                                                          | Medium                |
|                          | ALL                   | Accesses all partitions.                                                                                      | High                  |
|                          | INLIST                | Accesses partitions based on an IN-list predicate.                                                            | Medium                |
|                          | INVALID               | Indicates the partition set is empty.                                                                         | Low                   |
| **REMOTE**               | -                     | Retrieves data from a remote database.                                                                        | Medium                |
| **SEQUENCE**             | -                     | Involves accessing sequence values.                                                                           | Low                   |
| **SORT AGGREGATE**       | -                     | Retrieves a single row as the result of a group function.                                                     | High                  |
| **SORT UNIQUE**          | -                     | Sorts rows to eliminate duplicates.                                                                           | Medium                |
| **SORT GROUP BY**        | -                     | Sorts rows into groups for a GROUP BY clause.                                                                 | High                  |
| **SORT JOIN**            | -                     | Sorts rows before a merge-join.                                                                               | High                  |
| **SORT ORDER BY**        | -                     | Sorts rows for an ORDER BY clause.                                                                            | High                  |
| **TABLE ACCESS**         | FULL                  | Retrieves all rows from a table.                                                                              | High                  |
|                          | SAMPLE                | Retrieves sampled rows from a table.                                                                          | Medium                |
|                          | CLUSTER               | Retrieves rows based on an indexed cluster key value.                                                         | Medium                |
|                          | HASH                  | Retrieves rows based on a hash cluster key value.                                                             | Medium                |
|                          | BY ROWID RANGE        | Retrieves rows based on a rowid range.                                                                        | Medium                |
|                          | SAMPLE BY ROWID RANGE | Retrieves sampled rows based on a rowid range.                                                                | Medium                |
|                          | BY USER ROWID         | Retrieves rows using userer-supplied rowids.                                                                    | Medium                |
|                          | BY INDEX ROWID        | Retrieves rows using indexes in a non-partitioned table.                                                      | High                  |
|                          | BY GLOBAL INDEX ROWID | Retrieves rows using only global indexes in a partitioned table.                                              | High                  |
|                          | BY LOCAL INDEX ROWID  | Retrieves rows using local and possibly global indexes in a partitioned table.                                | High                  |

//todo
@NaturalId,
analiza planu zapytania

**Sources**

- https://github.com/spring-projects/spring-boot/issues/7107
- https://docs.spring.io/spring-data/jpa/docs/current/api/org/springframework/data/jpa/repository/JpaRepository.html#getReferenceById(ID)
- https://www.youtube.com/watch?v=exqfB1WaqIw&list=WL&index=18
- https://www.youtube.com/watch?v=UPWkpl5PL_w
- https://vladmihalcea.com/spring-read-only-transaction-hibernate-optimization/
- https://projectlombok.org/features/EqualsAndHashCode
- https://github.com/hibernate/hibernate-orm/blob/main/hibernate-core/src/main/java/org/hibernate/engine/internal/StatefulPersistenceContext.java
- https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#naturalid
- https://vladmihalcea.com/spring-transaction-connection-management/
- https://www.baeldung.com/spring-data-persistable-only-entities
- https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/Persistable.html
- https://docs.oracle.com/cd/A97385_01/server.920/a96533/ex_plan.htm#23465
- https://docs.oracle.com/cd/A97385_01/server.920/a96533/optimops.htm#44852
  https://docs.oracle.com/en/database/oracle/oracle-database/19/tgsql/
- https://docs.jboss.org/hibernate/orm/6.1/javadocs/org/hibernate/loader/MultipleBagFetchException.html
- https://vladmihalcea.com/spring-transaction-connection-management/

**Sources**

1. [Spring Boot Issue - Connection Handling][1]
2. [JpaRepository Documentation - `getReferenceById`][2]
3. [YouTube Video - Hibernate ORM Insights][3]
4. [YouTube Video - Hibernate Optimization][4]
5. [Spring Read-Only Transactions - Optimization][5]
6. [Lombok EqualsAndHashCode Feature][6]
7. [Hibernate StatefulPersistenceContext Source][7]
8. [Hibernate User Guide - Natural ID][8]
9. [Spring Transaction Connection Management][9]
10. [Spring Data Persistable Only Entities][10]
11. [Persistable Documentation][11]
12. [Oracle Execution Plan Documentation][12]
13. [Oracle Optimization Operations][13]
14. [Oracle Database SQL Reference][14]
15. [Hibernate MultipleBagFetchException Documentation][15]

[1]: https://github.com/spring-projects/spring-boot/issues/7107

[2]: https://docs.spring.io/spring-data/jpa/docs/current/api/org/springframework/data/jpa/repository/JpaRepository.html#getReferenceById(ID)

[3]: https://www.youtube.com/watch?v=exqfB1WaqIw&list=WL&index=18

[4]: https://www.youtube.com/watch?v=UPWkpl5PL_w

[5]: https://vladmihalcea.com/spring-read-only-transaction-hibernate-optimization/

[6]: https://projectlombok.org/features/EqualsAndHashCode

[7]: https://github.com/hibernate/hibernate-orm/blob/main/hibernate-core/src/main/java/org/hibernate/engine/internal/StatefulPersistenceContext.java

[8]: https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#naturalid

[9]: https://vladmihalcea.com/spring-transaction-connection-management/

[10]: https://www.baeldung.com/spring-data-persistable-only-entities

[11]: https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/Persistable.html

[12]: https://docs.oracle.com/cd/A97385_01/server.920/a96533/ex_plan.htm#23465

[13]: https://docs.oracle.com/cd/A97385_01/server.920/a96533/optimops.htm#44852

[14]: https://docs.oracle.com/en/database/oracle/oracle-database/19/tgsql/

[15]: https://docs.jboss.org/hibernate/orm/6.1/javadocs/org/hibernate/loader/MultipleBagFetchException.html