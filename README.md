# N+1 Query Detector

This project is designed to detect and optimize N+1 query issues in a Hibernate-based application.

## Query Optimization List

To ensure your application is optimized and free from common Hibernate pitfalls, follow these recommendations:

1. **Set `spring.jpa.open-in-view = false` in `application.yaml`:**
   - **Reason:** Disabling `open-in-view` helps prevent the common issue of lazy loading outside of transactions, which can lead to unexpected queries being executed. By ensuring that all database interactions happen within a defined transactional context, you reduce the risk of N+1 query problems and improve overall performance.

2. **In a `@OneToMany` relationship, prefer using a `Set<String, String>` for key-value properties that are not reused between entity instances instead of an entity:**
   - **Reason:** When you store simple properties like key-value pairs directly in a `Set<String, String>`, rather than as separate entities, it avoids additional SQL queries during inserts and updates. This can significantly improve performance by reducing the overhead of managing multiple entities and the associated database operations.

3. **Prefer `Set` over `List` in relationships:**
   - **Reason:** In Hibernate, a `List` is treated as a "bag," which can lead to inefficiencies. A `Set`, on the other hand, ensures uniqueness and is generally more performant for handling collections in relationships. By using a `Set`, you can avoid the potential pitfalls of using a `List`, such as redundant queries and unexpected behavior during collection updates.
