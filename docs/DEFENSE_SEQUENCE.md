# Laboratory Defense Sequence

## Purpose

This file is intended for the live university demo. It gives a strict sequence of actions so each requirement can be shown to the преподаватель clearly and without jumping between endpoints.

## Before the demo

1. Start PostgreSQL:

```bash
docker compose up -d
```

2. Start the application:

```bash
./mvnw spring-boot:run
```

3. Prepare one REST client:

- Postman
- IntelliJ HTTP Client
- `curl`

Base URL:

```text
http://localhost:8080
```

## Data preparation

Create at least 3 restaurants so pagination and nested filtering are visible.

### Restaurant 1

```json
{
  "name": "Basilico",
  "city": "Moscow",
  "cuisineType": "Italian",
  "tables": [
    { "tableNumber": 1, "seats": 2 },
    { "tableNumber": 2, "seats": 4 }
  ],
  "dishes": [
    { "name": "Pasta Carbonara", "price": 14.00 },
    { "name": "Truffle Pasta", "price": 29.00 }
  ]
}
```

### Restaurant 2

```json
{
  "name": "Tokyo Hall",
  "city": "Moscow",
  "cuisineType": "Japanese",
  "tables": [
    { "tableNumber": 3, "seats": 2 }
  ],
  "dishes": [
    { "name": "Sushi Set", "price": 18.00 },
    { "name": "Ramen", "price": 12.00 }
  ]
}
```

### Restaurant 3

```json
{
  "name": "Neva Grill",
  "city": "Saint Petersburg",
  "cuisineType": "European",
  "tables": [
    { "tableNumber": 5, "seats": 4 }
  ],
  "dishes": [
    { "name": "Steak", "price": 35.00 },
    { "name": "Salad", "price": 11.00 }
  ]
}
```

## Recommended live order

### 1. Show the domain model briefly

Say:

- the root entity is `Restaurant`;
- nested entity for the laboratory search is `Dish`;
- filtering restaurants by dish parameters is logical for a restaurant project.

If needed, open:

- [INFO.md](d:/javalabs/app/docs/INFO.md)
- [ER_DIAGRAM.md](d:/javalabs/app/docs/ER_DIAGRAM.md)

### 2. Show that base CRUD works

Run:

1. `POST /api/v1/restaurants` several times with the prepared JSON.
2. `GET /api/v1/restaurants/all`
3. `GET /api/v1/restaurants/{id}`
4. `GET /api/v1/restaurants?city=Moscow`

Say:

- restaurants are stored normally;
- dishes and tables are created together with restaurant;
- these records will be used for the advanced query.

### 3. Show requirement 1: complex GET query with JPQL and nested filtering

Run:

```text
GET /api/v1/restaurants/search/jpql?city=Moscow&cuisineType=Italian&dishName=pasta&minDishPrice=10&maxDishPrice=30&page=0&size=2&sort=name,asc
```

Explain:

- this is a complex GET request;
- filtering goes through nested entity `Dish`;
- implementation uses `@Query` and JPQL;
- pagination is already visible in the response.

### 4. Show requirement 2: analogous native query from a separate PostgreSQL SQL file

Run:

```text
GET /api/v1/restaurants/search/native?city=Moscow&dishName=pasta&minDishPrice=10&maxDishPrice=30&page=0&size=2&sort=name,asc
```

Then open the SQL files:

- [restaurant-search-native.sql](d:/javalabs/app/src/main/resources/sql/restaurant-search-native.sql)
- [restaurant-search-native-count.sql](d:/javalabs/app/src/main/resources/sql/restaurant-search-native-count.sql)

Explain:

- native SQL is not embedded in annotation anymore;
- it is stored in separate PostgreSQL-oriented `.sql` files;
- Java code only loads the SQL and passes parameters;
- this satisfies the requirement for native query in external SQL form.

### 5. Show requirement 3: pagination

Run the same JPQL endpoint twice:

```text
GET /api/v1/restaurants/search/jpql?page=0&size=1&sort=name,asc
GET /api/v1/restaurants/search/jpql?page=1&size=1&sort=name,asc
```

Explain:

- page content changes;
- `totalElements`, `totalPages`, `size`, `number` prove `Pageable` works.

### 6. Show requirement 4: in-memory HashMap index with composite key

Open:

- [RestaurantSearchCache.java](d:/javalabs/app/src/main/java/com/restaurant/app/sevice/cache/RestaurantSearchCache.java)
- [RestaurantSearchCacheKey.java](d:/javalabs/app/src/main/java/com/restaurant/app/sevice/cache/RestaurantSearchCacheKey.java)

Explain:

- cache is based on `HashMap<K, V>`;
- key contains filter parameters, page, size, sort, and search mode;
- the key is composite;
- the key is implemented as `record`, so `equals()` and `hashCode()` are correct.

### 7. Show requirement 5: cache invalidation on data change

Open:

- [RestaurantServiceImpl.java](d:/javalabs/app/src/main/java/com/restaurant/app/sevice/impl/RestaurantServiceImpl.java)

Show methods:

- `create(...)`
- `update(...)`
- `delete(...)`

Explain:

- each mutation clears the search cache;
- this is logically correct because these operations can change query results.

For a live proof:

1. Call a search endpoint once.
2. Update a restaurant with `PUT /api/v1/restaurants/{id}`.
3. Call the same search again.
4. Explain that mutation invalidated the previously cached result.

### 8. If the teacher asks about testing

Open:

- [TEST.md](d:/javalabs/app/docs/TEST.md)
- [SEARCH_AND_CACHE.md](d:/javalabs/app/docs/SEARCH_AND_CACHE.md)

Say:

- `TEST.md` contains general manual testing;
- `SEARCH_AND_CACHE.md` explains exactly why the added search and cache are logically justified;
- this file gives the exact defense sequence.

## Short version for a fast defense

If time is short, use this minimal order:

1. Create restaurants.
2. Show `GET /api/v1/restaurants/search/jpql...`
3. Show `GET /api/v1/restaurants/search/native...`
4. Open the two SQL files in `src/main/resources/sql`.
5. Show page `0` and page `1`.
6. Open `RestaurantSearchCache` and `RestaurantSearchCacheKey`.
7. Open invalidation in `RestaurantServiceImpl`.

## What to say in one sentence per requirement

1. Complex GET with nested filtering is implemented through JPQL `@Query` on `Restaurant -> Dish`.
2. The analogous native query is moved to separate PostgreSQL SQL files and executed from repository code.
3. Both query variants support `Pageable`.
4. Repeated search results are stored in an in-memory `HashMap` with a composite key.
5. Cache is invalidated on restaurant create, update, and delete.
