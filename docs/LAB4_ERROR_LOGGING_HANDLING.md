# Lab 4: Error Handling, Logging, Validation, AOP, OpenAPI

## Short Overview

The project was extended with six main features required by the laboratory:

1. Global error handling through `@RestControllerAdvice`.
2. Validation of incoming data with `@Valid` and bean validation constraints.
3. Unified error response format for all endpoints.
4. Logging configured through `logback` with log levels and log rotation.
5. AOP aspect for logging execution time of service methods.
6. Swagger/OpenAPI with endpoint and DTO descriptions.

## What Was Added

### Global error handling

Added:

- `GlobalExceptionHandler`
- `ResourceNotFoundException`
- `ConflictOperationException`
- DTOs `ApiError` and `ApiFieldError`

Behavior:

- validation errors return `400`
- missing entities return `404`
- conflict/business errors return `409`
- unexpected errors return `500`

All errors now use one JSON format.

### Validation

Validation was strengthened for request DTOs:

- restaurant create/update
- booking create/status update
- customer create
- search request
- nested table and dish request objects

Also validation was added for path variables like `id > 0`.

### Unified error format

Error response now contains:

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `fieldErrors`

Example:

```json
{
  "timestamp": "2026-03-22T15:00:00+03:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/restaurants",
  "fieldErrors": [
    {
      "field": "name",
      "message": "must not be blank"
    }
  ]
}
```

### Logging via logback

Added file:

- `src/main/resources/logback-spring.xml`

Configured:

- console logging
- file logging
- rotation by date and file size
- levels for application package and Hibernate SQL

Log files will appear in:

- `logs/restaurant-app.log`
- `logs/archive/...`

### AOP execution time logging

Added aspect:

- `ServiceExecutionLoggingAspect`

It logs execution time of service methods from `com.restaurant.app.sevice.impl`.

Expected log example:

```text
Executed RestaurantServiceImpl.searchByDishFiltersJpql(..) in 12 ms
```

### Swagger/OpenAPI

Added:

- `springdoc-openapi-starter-webmvc-ui`
- `OpenApiConfig`
- endpoint and DTO annotations

Swagger UI is available at:

- `http://localhost:8080/swagger-ui.html`

OpenAPI JSON:

- `http://localhost:8080/v3/api-docs`

## How To Test

### 1. Start the application

If PostgreSQL is used through Docker:

```bash
docker compose up -d
./mvnw spring-boot:run
```

### 2. Test validation

Request:

`POST /api/v1/restaurants`

Body:

```json
{
  "name": "",
  "city": "",
  "cuisineType": "Italian",
  "tables": [],
  "dishes": []
}
```

Expected result:

- HTTP `400`
- response in unified `ApiError` format
- `fieldErrors` contains validation problems

### 3. Test 404 handling

Request:

`GET /api/v1/restaurants/999999`

Expected result:

- HTTP `404`
- unified error response
- message says restaurant was not found

### 4. Test 409 handling

Request:

`POST /api/v1/transactions/rollback`

Body:

```json
{
  "name": "Rollback Demo",
  "city": "Moscow",
  "cuisineType": "Italian",
  "tables": [
    { "tableNumber": 1, "seats": 2 }
  ],
  "dishes": [
    { "name": "Pasta", "price": 10.00 }
  ]
}
```

Expected result:

- HTTP `409`
- unified error response
- no partial transaction data committed

### 5. Test logback logging

Run any endpoint, for example:

`GET /api/v1/restaurants/all`

Expected result:

- logs are printed to console
- logs are written to `logs/restaurant-app.log`
- after growth/rotation they are moved to `logs/archive`

### 6. Test AOP execution timing

Run:

`GET /api/v1/restaurants/all`

Expected result in logs:

- line with execution time of service method

### 7. Test Swagger/OpenAPI

Open:

- `http://localhost:8080/swagger-ui.html`

Expected result:

- Swagger UI opens
- endpoints are grouped and described
- DTO schemas are visible

## Expected Final Result

After all changes, the project should provide:

- centralized error processing
- consistent JSON error responses
- validated input for request bodies and ids
- file and console logging with rotation
- AOP timing logs for service methods
- interactive Swagger/OpenAPI documentation
