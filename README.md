# N+1 Query Detector and HashCode Analysis

### Maven Central.

https://mvnrepository.com/artifact/io.github.waldemargr/n-plus-1-detector

```xml

<dependency>
    <groupId>io.github.waldemargr</groupId>
    <artifactId>n-plus-1-detector</artifactId>
    <version>1.3.0</version>
</dependency>
```

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=waldemarGr_n-plus-1-detector&metric=bugs)](https://sonarcloud.io/summary/new_code?id=waldemarGr_n-plus-1-detector)

## Project Overview

### Current Features

#### N+1 Query Detection:

- **Objective:** Identify and resolve N+1 query problems that can lead to performance bottlenecks in applications using
  Hibernate.
- **Implementation:** Use `@EnableAdditionalHibernateStatistic` to analyze queries statistic and provide insights into
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

### Query Execution Plan Logging (Mysql/Oracle)

- **Note:** This feature has not yet been tested on Oracle.
- **Objective:** Provide detailed insights into the execution plans of app queries, helping to identify and address
  performance issues in your database interactions
- **Implementation:** Use the `@EnableQueryPlanAnalysis` annotation to enable logging of query execution plans. By
  applying this annotation, the application will log the execution plan for every query executed by Hibernate, offering
  a deeper look into how queries are being processed.

```java

@EnableRelationshipAnalysis
@EnableAdditionalHibernateStatistic
@EnableHashCodeAnalysis
@SpringBootApplication
public class MySpringApp {

}
```

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
11. **Use `@Transactional(readOnly = true)` Wisely** Be sure you’re using it correctly
    - **Why:** Performance Boost: By marking a transaction as read-only, you let the database optimize queries, skipping
      write-related overhead. This typically results in faster query execution and better overall performance. Memory
      Efficiency: Read-only transactions consume less memory because they avoid the extra resources needed for managing
      data writes and tracking changes.
        - In read-write transaction, the dirty checking mechanism creates a copy of the entity to compare if changes
          have
          occurred. This results in approximately double the memory usage, as both the original and modified states of
          the
          entity are stored in memory.
12. todo spring.datasource.hikari.auto-commit
13. todo TransactionTemplate
13. **Level 1 Cache in Hibernate**
    - operates within the scope of a single transaction.
    - The cache uses the entity’s unique identifier (ID) only.
    ```JAVA
    @Transactional // 
    public void demonstrateL1Cache() {
        // First retrieval - hits the database
        Customer customer1 = customerRepository.findById(1L).orElse(null);
        System.out.println(customer1.getName());

        // Second retrieval - should be served from L1 cache, no DB hit
        Customer customer = customerRepository.findById(1L).orElse(null);
        System.out.println(customer.getName());
    
        customer.setName("name");
    
        // Flush to synchronize changes with the database
        customerRepository.flush(); 
        // At this point, the database is updated with the new name, but L1 cache still holds 'customer' with the updated state.
    
        entityManager.detach(customer1);
        // Detaching the entity means it is no longer in the L1 cache
        Customer customer = customerRepository.findById(1L).orElse(null);
    
        entityManager.clear();
        // Load the entity again - this will hit the database because the cache was cleared
        Customer customer = customerRepository.findById(1L).orElse(null);
        System.out.println("After Clear: " + customer.getName());
    }
    ```
    - **Understanding Hibernate Level 1 Cache Implementation**  it’s useful to look at a simplified view of its internal
      implementation.
       ```JAVA
       Map<EntityUniqueKey, Object> entitiesByUniqueKey = new HashMap<>();
       ```
14. **Dirty checking** Dirty checking works within the context of a transaction to automatically detect changes in an
    entity. It compares the current state of the entity with its original state and saves any detected changes when the
    transaction commits.
     ```Java
    @Transactional
    public void updateUser(Long userId, String newName) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setName(newName);
        //  No need to manually save; dirty checking will work its magic
    }
    ```

15. **A key takeaway: Understand Your Logs**
    - Read the logs and understand what they mean. This library simply analyzes the logs and identifies issues if they
      arise. You can achieve the same result by analyzing and understanding Hibernate logs yourself.
    ```property
    logging.level.org.hibernate.SQL=debug
    logging.level.org.hibernate.orm.jdbc.bind=trace
    ```

//todo
@NaturalId -
wyświetlanie planu zaytania
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