# Bulk Operations README

## What Was Implemented

1. Bulk customer creation:
   `POST /api/v1/customers/bulk`

2. Transaction demo for the same business operation:
   `POST /api/v1/transactions/customers/bulk-no-tx`
   `POST /api/v1/transactions/customers/bulk-with-tx`

3. Service-layer use of:
   - `Stream API`
   - `Optional`

4. Unit tests for service implementations and cache classes.

5. Coverage verification with JaCoCo.

## Endpoints For Demo

### 1. Successful bulk create

`POST /api/v1/customers/bulk`

```json
{
  "customers": [
    { "fullName": "Ivan Petrov", "phone": "+79990001122" },
    { "fullName": "Petr Ivanov", "phone": "+79990001123" }
  ]
}
```

Expected result:
- both customers are created
- `savedCount = 2`
- response contains created customers

### 2. Bulk without transaction

`POST /api/v1/transactions/customers/bulk-no-tx`

```json
{
  "customers": [
    { "fullName": "Ivan Petrov", "phone": "+79990001130" },
    { "fullName": "Petr Ivanov", "phone": "+79990001130" }
  ]
}
```

Expected result:
- first customer is saved
- second customer causes a conflict
- partial data remains in DB
- `savedCount = 1`

### 3. Bulk with transaction

`POST /api/v1/transactions/customers/bulk-with-tx`

```json
{
  "customers": [
    { "fullName": "Ivan Petrov", "phone": "+79990001140" },
    { "fullName": "Petr Ivanov", "phone": "+79990001140" }
  ]
}
```

Expected result:
- response is `409 Conflict`
- transaction rolls back
- no new customers from this request remain in DB

## How To Show The Difference

1. Call `POST /api/v1/transactions/customers/bulk-no-tx`.
2. Call `GET /api/v1/customers`.
3. Show that one record from the failed bulk request stayed in DB.
4. Call `POST /api/v1/transactions/customers/bulk-with-tx`.
5. Call `GET /api/v1/customers` again.
6. Show that the transactional request left no new records.

## Tests

Run:

```powershell
.\mvnw.cmd -q test
```

Main added tests:
- `CustomerServiceImplTest`
- `BookingServiceImplTest`
- `RestaurantServiceImplTest`
- `TransactionDemoServiceImplTest`
- `RestaurantSearchCacheTest`
- `RestaurantSearchCacheKeyTest`

## Coverage

JaCoCo is configured in `pom.xml`.

Checked packages:
- `com.restaurant.app.sevice.impl.*`
- `com.restaurant.app.sevice.cache.*`

Coverage report:
- `target/site/jacoco/index.html`

Current rule:
- `100%` line coverage for service implementation and cache classes
