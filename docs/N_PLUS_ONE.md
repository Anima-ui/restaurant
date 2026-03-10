# N+1 Guide

This file explains the `N+1` endpoints, what each one demonstrates, how they differ, and what exactly to look for in Hibernate logs and statistics.

## 1. What N+1 Means

`N+1` is a query problem.

Typical flow:

1. the application loads a list of parent entities with 1 query
2. then for each parent entity it lazily loads child data with another query

Result:

```text
1 query for parent list + N extra queries for child collections
```

If there are 5 restaurants, this often becomes:

```text
1 query for restaurants + 5 queries for dishes = 6 queries
```

If nested collections are involved, the number grows even more.

## 2. Where It Is Configured

The project already has SQL logging and Hibernate statistics enabled in:

- [application.yaml](d:/javalabs/app/src/main/resources/application.yaml)

Important settings already present:

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        generate_statistics: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.orm.jdbc.bind: TRACE
    org.hibernate.stat: DEBUG
```

That means:

- SQL queries will be printed
- bind parameters will be visible
- Hibernate statistics messages will be visible

## 3. N+1 Endpoints Overview

Controller:

- [NPlusOneController.java](d:/javalabs/app/src/main/java/com/restaurant/app/controller/nplusone/NPlusOneController.java)

Service logic:

- [RestaurantServiceImpl.java](d:/javalabs/app/src/main/java/com/restaurant/app/sevice/impl/RestaurantServiceImpl.java)

Repository methods:

- [RestaurantRepository.java](d:/javalabs/app/src/main/java/com/restaurant/app/repository/RestaurantRepository.java)

## 4. Endpoint: `GET /api/v1/nplusone/problem`

What it does:

- loads restaurants with `findAllWithoutFetch()`
- restaurants are loaded first
- `restaurant.getDishes().size()` then forces lazy loading for each restaurant

Why it is a problem:

- one query loads all restaurants
- each restaurant then triggers another query for dishes
- total query count becomes `1 + N`

Relevant code path:

- [NPlusOneController.java](d:/javalabs/app/src/main/java/com/restaurant/app/controller/nplusone/NPlusOneController.java)
- [RestaurantServiceImpl.java](d:/javalabs/app/src/main/java/com/restaurant/app/sevice/impl/RestaurantServiceImpl.java)

What it demonstrates:

- the classic `N+1` problem on `Restaurant -> Dish`

What to expect in logs:

- one `select` from `restaurants`
- then repeated `select` statements from `dishes` filtered by `restaurant_id`

Typical pattern:

```text
select ... from restaurants r1_0
select ... from dishes d1_0 where d1_0.restaurant_id=?
select ... from dishes d1_0 where d1_0.restaurant_id=?
select ... from dishes d1_0 where d1_0.restaurant_id=?
```

How to explain it:

- the first query gets the restaurant list
- every access to `getDishes()` causes another SQL query for that restaurant

## 5. Endpoint: `GET /api/v1/nplusone/optimized`

What it does:

- loads restaurants with `findAllWithDishesJoinFetch()`
- repository uses `LEFT JOIN FETCH r.dishes`
- dishes are fetched together with restaurants

Why it is better:

- restaurants and dishes are loaded in one SQL query
- lazy extra queries for dishes are avoided

What it demonstrates:

- the fix for the simple `Restaurant -> Dish` N+1 case

What to expect in logs:

- one SQL query with `join`
- no repeated `select ... from dishes where restaurant_id=?` for each restaurant

Typical pattern:

```text
select distinct ...
from restaurants r1_0
left join dishes d1_0 on r1_0.id=d1_0.restaurant_id
```

How it differs from `/problem`:

- `/problem` loads children one parent at a time
- `/optimized` loads parent and child data together

## 6. Endpoint: `GET /api/v1/nplusone/nested-problem`

What it does:

- loads restaurants with `findAllWithoutFetch()`
- then forces loading of `tables`
- then for each table forces loading of `bookings`

Why it is worse than the simple problem:

- first query loads restaurants
- then extra queries load tables per restaurant
- then even more queries load bookings per table

This is no longer just `1 + N`.
It is closer to:

```text
1 + N + M
```

Where:

- `N` = number of restaurants
- `M` = total number of tables across those restaurants

What it demonstrates:

- nested N+1
- query explosion across multiple lazy relations

What to expect in logs:

- one `select` from `restaurants`
- repeated `select` from `restaurant_tables where restaurant_id=?`
- repeated `select` from `bookings where table_id=?`

Typical pattern:

```text
select ... from restaurants r1_0
select ... from restaurant_tables t1_0 where t1_0.restaurant_id=?
select ... from restaurant_tables t1_0 where t1_0.restaurant_id=?
select ... from bookings b1_0 where b1_0.table_id=?
select ... from bookings b1_0 where b1_0.table_id=?
```

How it differs from `/problem`:

- `/problem` has one lazy child collection level
- `/nested-problem` has two lazy levels: `tables` and `bookings`

## 7. Endpoint: `GET /api/v1/nplusone/nested-optimized`

What it does:

- loads restaurants using repository method with `@EntityGraph(attributePaths = {"tables", "tables.bookings"})`
- nested relations are fetched more efficiently

Why it is better:

- Hibernate is instructed in advance to fetch needed associations
- much fewer extra queries happen

What it demonstrates:

- a fix for nested N+1
- `EntityGraph` as an alternative to `JOIN FETCH`

What to expect in logs:

- significantly fewer selects than `/nested-problem`
- no long repeated sequence of table and booking lookups per parent object

How it differs from `/optimized`:

- `/optimized` solves a simple one-level case with `JOIN FETCH`
- `/nested-optimized` solves a nested case with `EntityGraph`

## 8. What To Search For In Logs

When testing, focus on repeated SQL patterns.

### Signs that N+1 really happened

Look for:

- one query for the root entity list
- many similar queries for child entities
- the same SQL repeated with different bind values

Examples of bad signs:

```text
select ... from restaurants ...
select ... from dishes ... where restaurant_id=?
select ... from dishes ... where restaurant_id=?
select ... from dishes ... where restaurant_id=?
```

or:

```text
select ... from restaurant_tables ... where restaurant_id=?
select ... from restaurant_tables ... where restaurant_id=?
select ... from bookings ... where table_id=?
select ... from bookings ... where table_id=?
select ... from bookings ... where table_id=?
```

That repeated pattern is the evidence.

### Signs that optimization worked

Look for:

- one query with `join`
- or very few total queries
- no repeated child-loading query for every parent

Examples of good signs:

```text
select distinct ...
from restaurants
left join dishes ...
```

or a small number of queries instead of a long repeated sequence.

## 9. What To Search For In Hibernate Statistics

Because `hibernate.generate_statistics` is enabled, Hibernate writes statistical information.

The exact message format may vary by Hibernate version, but usually you should watch for:

- total JDBC statements executed
- session metrics
- query execution summaries

Useful phrases to search in logs:

- `statistics`
- `session metrics`
- `jdbc statements`
- `queries`

What matters during demo:

1. call `/problem`
2. note that many statements were executed
3. call `/optimized`
4. show that the number is lower

Even if the exact wording differs, the key proof is:

- repeated SQL for the problem endpoint
- visibly fewer SQL statements for the optimized endpoint

## 10. Best Demo Scenario

Use enough data so the difference is obvious.

Recommended setup:

- 3 to 5 restaurants
- each restaurant has 2 to 3 dishes
- each restaurant has 2 to 3 tables
- some tables have bookings

Then test in this order:

1. create several restaurants through `POST /api/v1/restaurants`
2. call `GET /api/v1/nplusone/problem`
3. inspect logs and count repeated child queries
4. call `GET /api/v1/nplusone/optimized`
5. compare with the previous logs
6. call `GET /api/v1/nplusone/nested-problem`
7. inspect the much larger repeated query sequence
8. call `GET /api/v1/nplusone/nested-optimized`
9. compare again

## 11. Short Explanation For Presentation

You can explain it like this:

`/problem`:

- loads restaurants first
- dishes are loaded lazily one restaurant at a time
- this creates `1 + N` queries

`/optimized`:

- uses `JOIN FETCH`
- restaurants and dishes are fetched together
- extra child queries disappear

`/nested-problem`:

- same issue, but now on multiple levels
- restaurant -> tables -> bookings
- query count grows much faster

`/nested-optimized`:

- uses `EntityGraph`
- nested relations are fetched in advance
- query count becomes much smaller

## 12. Final Check

If you need to prove the project is configured correctly for N+1 analysis, check:

- [application.yaml](d:/javalabs/app/src/main/resources/application.yaml)
- [NPlusOneController.java](d:/javalabs/app/src/main/java/com/restaurant/app/controller/nplusone/NPlusOneController.java)
- [RestaurantServiceImpl.java](d:/javalabs/app/src/main/java/com/restaurant/app/sevice/impl/RestaurantServiceImpl.java)

The required logging and statistics are already enabled, so no additional property change was needed.
