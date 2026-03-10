# Testing Guide

This file explains how to test the project manually: how to start it, what each endpoint does, what JSON to send, and how to verify transaction and N+1 behavior.

## 1. What You Need Before Testing

The application expects PostgreSQL on:

```text
localhost:5432
database: restaurant_lab
username: postgres
password: postgres
```

You can start PostgreSQL with Docker:

```bash
docker compose up -d
```

Then start the application:

```bash
./mvnw spring-boot:run
```

Base URL:

```text
http://localhost:8080
```

## 2. Recommended Tool For Requests

You can test with:

- Postman
- IntelliJ HTTP Client
- `curl`

All examples below use JSON with header:

```text
Content-Type: application/json
```

## 3. Main JSON For Creating Test Data

Use this JSON for most `POST` requests:

```json
{
  "name": "Basilico",
  "city": "Moscow",
  "cuisineType": "Italian",
  "tables": [
    {
      "tableNumber": 1,
      "seats": 2
    },
    {
      "tableNumber": 2,
      "seats": 4
    }
  ],
  "dishes": [
    {
      "name": "Pizza Margherita",
      "price": 12.50
    },
    {
      "name": "Pasta Carbonara",
      "price": 14.00
    }
  ]
}
```

JSON for update:

```json
{
  "name": "Basilico Updated",
  "city": "Saint Petersburg",
  "cuisineType": "Mediterranean"
}
```

## 4. Restaurant Endpoints

### `POST /api/v1/restaurants`

Purpose: create a restaurant with related tables and dishes.

Example request:

```bash
curl -X POST http://localhost:8080/api/v1/restaurants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Basilico",
    "city": "Moscow",
    "cuisineType": "Italian",
    "tables": [
      { "tableNumber": 1, "seats": 2 },
      { "tableNumber": 2, "seats": 4 }
    ],
    "dishes": [
      { "name": "Pizza Margherita", "price": 12.50 },
      { "name": "Pasta Carbonara", "price": 14.00 }
    ]
  }'
```

Expected result:

- HTTP `201 Created`
- restaurant is saved
- tables are saved
- dishes are saved

### `GET /api/v1/restaurants/all`

Purpose: get all restaurants.

Example:

```bash
curl http://localhost:8080/api/v1/restaurants/all
```

### `GET /api/v1/restaurants/{id}`

Purpose: get one restaurant by id.

Example:

```bash
curl http://localhost:8080/api/v1/restaurants/1
```

### `GET /api/v1/restaurants?city=Moscow`

Purpose: get restaurants filtered by city.

Example:

```bash
curl "http://localhost:8080/api/v1/restaurants?city=Moscow"
```

### `GET /api/v1/restaurants/detailed?city=Moscow`

Purpose: get restaurants by city with `tables` and `dishes` fetched more efficiently.

Example:

```bash
curl "http://localhost:8080/api/v1/restaurants/detailed?city=Moscow"
```

### `PUT /api/v1/restaurants/{id}`

Purpose: update basic restaurant fields.

Example:

```bash
curl -X PUT http://localhost:8080/api/v1/restaurants/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Basilico Updated",
    "city": "Saint Petersburg",
    "cuisineType": "Mediterranean"
  }'
```

### `DELETE /api/v1/restaurants/{id}`

Purpose: delete restaurant.

Example:

```bash
curl -X DELETE http://localhost:8080/api/v1/restaurants/1
```

Expected result:

- HTTP `204 No Content`

## 5. Transaction Demo Endpoints

These endpoints show the difference between saving data with and without `@Transactional`.

### `POST /api/v1/transactions/partial`

Purpose:

- saves restaurant
- saves one table
- throws exception after saves
- because there is no transaction, saved data remains in DB

Request JSON:

```json
{
  "name": "Tx Partial Demo",
  "city": "Moscow",
  "cuisineType": "Italian",
  "tables": [],
  "dishes": []
}
```

How to test:

1. Call `GET /api/v1/transactions/state`
2. Remember `restaurantsInDb` and `tablesInDb`
3. Call `POST /api/v1/transactions/partial`
4. Call `GET /api/v1/transactions/state` again

What you should see:

- counts increased
- exception scenario happened
- data still exists in DB

### `POST /api/v1/transactions/rollback`

Purpose:

- saves restaurant inside transaction
- saves table inside transaction
- throws exception
- because `@Transactional` is enabled, everything rolls back

Request JSON:

```json
{
  "name": "Tx Rollback Demo",
  "city": "Moscow",
  "cuisineType": "Italian",
  "tables": [],
  "dishes": []
}
```

How to test:

1. Call `GET /api/v1/transactions/state`
2. Remember counts
3. Call `POST /api/v1/transactions/rollback`
4. Call `GET /api/v1/transactions/state` again

What you should see:

- counts do not increase after rollback
- response note says rollback happened

### `POST /api/v1/transactions/exception-no-tx`

Purpose:

- saves only restaurant
- throws exception after save
- without transaction the restaurant stays in DB

Request JSON:

```json
{
  "name": "Exception No Tx",
  "city": "Moscow",
  "cuisineType": "Georgian",
  "tables": [],
  "dishes": []
}
```

What you should see:

- restaurant count increases
- response note says data remained in DB

### `POST /api/v1/transactions/exception-with-tx`

Purpose:

- saves only restaurant
- throws exception
- with `@Transactional` the restaurant is rolled back

Request JSON:

```json
{
  "name": "Exception With Tx",
  "city": "Moscow",
  "cuisineType": "French",
  "tables": [],
  "dishes": []
}
```

What you should see:

- restaurant count does not increase
- response note says rollback removed changes

### `POST /api/v1/transactions/cascade`

Purpose:

- saves restaurant
- related tables are saved through cascade

Request JSON:

```json
{
  "name": "Cascade Demo",
  "city": "Kazan",
  "cuisineType": "Asian",
  "tables": [],
  "dishes": []
}
```

What you should see:

- restaurant saved
- tables count increases automatically

### `GET /api/v1/transactions/state`

Purpose: returns current count of restaurants and tables in DB.

Example:

```bash
curl http://localhost:8080/api/v1/transactions/state
```

Use this endpoint before and after transaction scenarios.

## 6. N+1 Endpoints

These endpoints are needed to show the N+1 problem and its solution.

### `GET /api/v1/nplusone/problem`

Purpose:

- loads restaurants first
- then loads dishes lazily for each restaurant
- causes `1 + N` SQL queries

Example:

```bash
curl http://localhost:8080/api/v1/nplusone/problem
```

### `GET /api/v1/nplusone/optimized`

Purpose:

- uses `JOIN FETCH`
- loads restaurants and dishes more efficiently
- produces fewer SQL queries

Example:

```bash
curl http://localhost:8080/api/v1/nplusone/optimized
```

### `GET /api/v1/nplusone/nested-problem`

Purpose:

- loads restaurants
- then tables for each restaurant
- then bookings for each table
- demonstrates a deeper N+1 case

Example:

```bash
curl http://localhost:8080/api/v1/nplusone/nested-problem
```

### `GET /api/v1/nplusone/nested-optimized`

Purpose:

- uses `EntityGraph`
- fetches nested relations more efficiently

Example:

```bash
curl http://localhost:8080/api/v1/nplusone/nested-optimized
```

## 7. How To Properly Test N+1

The project already has Hibernate statistics enabled in `application.yaml`.

Relevant settings:

- `spring.jpa.show-sql: true`
- `hibernate.generate_statistics: true`
- logging for `org.hibernate.SQL`
- logging for `org.hibernate.stat`

### Step-by-step scenario

1. Create several restaurants.
2. Make sure each restaurant has dishes.
3. Call `GET /api/v1/nplusone/problem`.
4. Look at application logs.
5. Call `GET /api/v1/nplusone/optimized`.
6. Compare the logs.

### Recommended data volume

For a visible difference, create at least:

- 3 to 5 restaurants
- 2 to 3 dishes for each restaurant

### What you should see in logs for `/problem`

Typical pattern:

- one query for restaurants
- additional queries for dishes for each restaurant

This is the N+1 problem:

```text
1 query for restaurant list + N queries for child collections
```

### What you should see in logs for `/optimized`

Typical pattern:

- one query with `JOIN FETCH`
- much fewer total SQL statements

### How To Explain It During Demo

You can say:

1. `problem` loads parent entities first and then triggers lazy loading per restaurant.
2. `optimized` uses explicit fetch strategy and avoids extra queries.
3. Hibernate statistics and SQL logs show the difference in query count.

## 8. Suggested Full Manual Test Order

If you want a clean demo from start to finish, use this order:

1. Start PostgreSQL with Docker.
2. Start the Spring Boot app.
3. Create 3 to 5 restaurants through `POST /api/v1/restaurants`.
4. Check `GET /api/v1/restaurants/all`.
5. Check `GET /api/v1/restaurants/{id}`.
6. Check `GET /api/v1/restaurants?city=Moscow`.
7. Check `GET /api/v1/restaurants/detailed?city=Moscow`.
8. Run `GET /api/v1/nplusone/problem`.
9. Run `GET /api/v1/nplusone/optimized`.
10. Run `POST /api/v1/transactions/partial`.
11. Run `GET /api/v1/transactions/state`.
12. Run `POST /api/v1/transactions/rollback`.
13. Run `GET /api/v1/transactions/state`.
14. Run `POST /api/v1/transactions/exception-no-tx`.
15. Run `POST /api/v1/transactions/exception-with-tx`.

## 9. Quick Checks For Each Requirement

Requirement: logical entity relations

- check [ER_DIAGRAM.md](d:/javalabs/app/ER_DIAGRAM.md)

Requirement: save related entities with transaction and without it

- test `/api/v1/transactions/partial`
- test `/api/v1/transactions/rollback`

Requirement: show that data remains saved after exception without transaction

- test `/api/v1/transactions/exception-no-tx`

Requirement: show rollback with `@Transactional`

- test `/api/v1/transactions/exception-with-tx`

Requirement: show N+1 problem and solution

- test `/api/v1/nplusone/problem`
- test `/api/v1/nplusone/optimized`

Requirement: show SQL statistics

- check logs while calling N+1 endpoints

## 10. If Something Does Not Work

Check these points:

- PostgreSQL container is running
- database name is `restaurant_lab`
- port `5432` is free
- application started without DB connection errors
- you are sending `Content-Type: application/json`
- request body contains all required fields
