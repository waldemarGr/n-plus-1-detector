# N+1 Query Detector
```xml
<dependency>
    <groupId>io.github.waldemargr</groupId>
    <artifactId>n-plus-1-detector</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Project Overview
This project is designed to detect N+1 query issues within @Transactional methods in Spring Boot applications. Below is a list of potential optimizations you can consider for your project. The goal is to expand this project to support these best practices in the future.

## Query Optimization List

To ensure your application is optimized and free from common Hibernate pitfalls, follow these recommendations:

1. **Set `spring.jpa.open-in-view = false` in `application.yaml`:**
   - **Reason:** Disabling `open-in-view` helps prevent the common issue of lazy loading outside of transactions, which can lead to unexpected queries being executed. By ensuring that all database interactions happen within a defined transactional context, you reduce the risk of N+1 query problems and improve overall performance.
   - **Heads up** If you disable open-in-view and start seeing errors related to data fetching during tests, it's a strong indication that your application has N+1 problems.
   
2. **In a `@OneToMany` relationship, prefer using a `Set<String, String>` for key-value properties that are not reused between entity instances instead of an entity:**
   - **Reason:** When you store simple properties like key-value pairs directly in a `Set<String, String>`, rather than as separate entities, it avoids additional SQL queries during inserts and updates. This can significantly improve performance by reducing the overhead of managing multiple entities and the associated database operations.

3. **Prefer `Set` over `List` in relationships:**
   - **Reason:** In Hibernate, a `List` is treated as a "bag," which can lead to inefficiencies. A `Set`, on the other hand, ensures uniqueness and is generally more performant for handling collections in relationships. By using a `Set`, you can avoid the potential pitfalls of using a `List`, such as redundant queries and unexpected behavior during collection updates.
   - **Bug: If** you’re using two List collections in an entity and try optimizing them with EntityGraph, get ready for some serious headaches.
4. **Use getReferenceById** when you only need to reference an entity without fetching its data
   ```java 
   Customer customer = customerRepository.getReferenceById(customerId);
   Order order = new Order();
   order.setCustomer(customer);
   corderRepository.persist(order); 
   
   - **Why:** The getReferenceById  method gives you a proxy reference to the entity without actually querying the database for its data. This is super handy when you want to link entities or set up relationships but don’t need the complete entity details right away. By using this method, you avoid those extra database hits, w

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
   - **Here’s a quick overview.:**  `isNew` method decides what to do. But, guess what? It first runs a SELECT to check if the entity is truly new. It’s like an employer checking whether you've already received a raise before giving you another one. You’d probably agree that in this case, it’s better if they skip the extra check and just give you the raise you’ve earned.

   - Consider Adding '@Version' for proper Entity Versioning

6. **Prefer Using `FetchType.LAZY` in Relationships**
   - **Avoiding Unnecessary Data Loading**: Often, you don't need to load all related entities when querying a parent entity. To retrieve additional information, use the appropriate mechanisms like  Fetch Joins or EntityGraphs

7. **Fetching Exactly What You Need**

   - **Fetch Joins** allow you to load related entities in a single query, preventing the N+1 problem by avoiding multiple queries for associations.

   ```java
   @Query("SELECT o FROM Order o JOIN FETCH o.customer JOIN FETCH o.orderItems WHERE o.id = :id")
   Order findOrderWithCustomerAndItems(@Param("id") Long id); 
   ```
   - **EntityGraphs** let you dynamically specify which associations to load eagerly, helping to prevent unnecessary data loading and N+1 queries.
   ```java
   @Repository
   public interface OrderRepository extends JpaRepository<Order, Long> {
   
       @EntityGraph(attributePaths = { "customer", "orderItems" })
       Order findById(Long id);
   }
   ```
8. **`@DynamicUpdate` for Large Tables or large data**
   - **Why:** The `@DynamicUpdate` annotation ensures that Hibernate generates SQL update statements that include only the columns that have been changed. This can be particularly beneficial for large tables or entities with many columns. For example, if you have a User entity with 30 columns and you only update the gender field, @DynamicUpdate will ensure that only the gender column is included in the SQL update statement, rather than all 30 columns. This can significantly reduce the amount of data sent to the database and improve performance.
9. **`@DynamicInsert` to Avoid Sending Nulls**  annotation makes Hibernate generate SQL INSERT statements that include only non-null columns. If your entity has many null values, @DynamicInsert ensures that only columns with actual values are sent to the database.
10. **Avoid Default hashCode and equals from `@EqualsAndHashCode`**
11. **Why:** The hashCode and equals methods should be based on fields that do not change over the lifetime of an object. By default, @lombok's `@EqualsAndHashCode` may include mutable fields, which can lead to inconsistencies. To avoid this, it’s better to base these methods on a stable identifier, such as a UUID generated by your application.
12. **A key takeaway: Understand Your Logs**
   - Read the logs and understand what they mean. This library simply analyzes the logs and identifies issues if they arise. You can achieve the same result by analyzing and understanding Hibernate logs yourself."

//todo
Dirty checking/ cache 1lvl/  and save / flush

13. **Sources**
- https://docs.spring.io/spring-data/jpa/docs/current/api/org/springframework/data/jpa/repository/JpaRepository.html#getReferenceById(ID)
- https://www.youtube.com/watch?v=exqfB1WaqIw&list=WL&index=18
- https://www.youtube.com/watch?v=UPWkpl5PL_w
- https://vladmihalcea.com/spring-read-only-transaction-hibernate-optimization/
- https://projectlombok.org/features/EqualsAndHashCode