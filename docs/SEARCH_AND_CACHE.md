# Search And Cache Changes

## What was added

The project now contains a dedicated restaurant search scenario based on the existing `Restaurant -> Dish` relation. This keeps the task aligned with the restaurant domain instead of introducing artificial entities.

Implemented items:

1. A complex GET query with filtering by nested entity through `@Query` and JPQL.
2. The same filtering scenario implemented through a native SQL query.
3. Pagination for both query variants via `Pageable`.
4. An in-memory index built on `HashMap<K, V>` for previously requested search results.
5. Cache invalidation when restaurant data changes.

## Why this is logical for the project

Filtering restaurants by dish parameters is a natural restaurant use case:

- users search restaurants by city and cuisine;
- users refine the result by menu position name;
- users can filter by dish price range;
- the nested filter is real because the restaurant is selected through data from `Dish`.

This is more justified than adding abstract audit data, technical tags, or unrelated catalog fields that the current application does not use.

## API endpoints

Two new endpoints were added:

- `GET /api/v1/restaurants/search/jpql`
- `GET /api/v1/restaurants/search/native`

Supported query parameters:

- `city`
- `cuisineType`
- `dishName`
- `minDishPrice`
- `maxDishPrice`
- pagination params from Spring Data: `page`, `size`, `sort`

Example:

```http
GET /api/v1/restaurants/search/jpql?city=Moscow&cuisineType=Italian&dishName=pasta&minDishPrice=20&maxDishPrice=50&page=0&size=5&sort=name,asc
```

## Query implementation details

The JPQL query joins `Restaurant` with `Dish` and applies optional filters. This satisfies the requirement for a complex GET request with filtering by a nested entity using `@Query`.

The native query repeats the same business logic against `restaurants` and `dishes` tables, but unlike the JPQL variant it is stored in separate PostgreSQL SQL files:

- `src/main/resources/sql/restaurant-search-native.sql`
- `src/main/resources/sql/restaurant-search-native-count.sql`

This matches the requirement that native SQL should be visible as an external SQL script instead of being embedded into annotation text.

Both queries use `DISTINCT` because one restaurant may have several matching dishes.

Each query also has its own `countQuery`, which is required for correct page metadata.

## In-memory index

The in-memory index is implemented in `RestaurantSearchCache` and is explicitly based on:

```java
Map<RestaurantSearchCacheKey, Page<RestaurantDto>> cache = new HashMap<>();
```

The cache key is composite and includes:

- search mode (`JPQL` or `NATIVE`);
- all filter parameters;
- page number;
- page size;
- sort description.

`RestaurantSearchCacheKey` is implemented as a Java `record`, so `equals()` and `hashCode()` are generated correctly from all key parts. This directly satisfies the requirement for correct key behavior.

## Cache invalidation

The search result depends on restaurant city, cuisine type, and dishes created together with the restaurant. Because of that, cache clearing is bound to restaurant mutations:

- `create(...)`
- `update(...)`
- `delete(...)`

This is sufficient and logically justified for the current project, because these operations are the existing write points that can change search results.

## Dependency check

No extra runtime dependency was required for the feature itself because:

- JPQL and native queries are already supported by `spring-boot-starter-data-jpa`;
- `Pageable` is already part of Spring Data JPA;
- `HashMap` is part of the JDK.

One test dependency was added:

- `com.h2database:h2` in `test` scope.

It was added only to make repository tests deterministic without depending on a local PostgreSQL instance.
