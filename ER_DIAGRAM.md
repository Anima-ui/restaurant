# Restaurant Management System ER Diagram

```mermaid
erDiagram
    CUSTOMER ||--o{ BOOKING : makes
    RESTAURANT ||--o{ RESTAURANT_TABLE : has
    RESTAURANT ||--o{ DISH : offers
    RESTAURANT_TABLE ||--o{ BOOKING : receives

    CUSTOMER {
        long id PK
        string full_name
        string phone
    }

    RESTAURANT {
        long id PK
        string name
        string city
        string cuisine_type
    }

    RESTAURANT_TABLE {
        long id PK
        int table_number
        int seats
        long restaurant_id FK
    }

    DISH {
        long id PK
        string name
        decimal price
        long restaurant_id FK
    }

    BOOKING {
        long id PK
        datetime booking_time
        string status
        long customer_id FK
        long table_id FK
    }
```

## Relationship Notes

- `Restaurant (1) -> (N) RestaurantTable`
- `Restaurant (1) -> (N) Dish`
- `Customer (1) -> (N) Booking`
- `RestaurantTable (1) -> (N) Booking`
- `Booking (N) -> (1) Customer`
- `Booking (N) -> (1) RestaurantTable`

## Cascade And Lifecycle

- `Restaurant.tables`: `cascade = ALL`, `orphanRemoval = true`
- `Restaurant.dishes`: `cascade = ALL`, `orphanRemoval = true`
- `RestaurantTable.bookings`: `cascade = ALL`, `orphanRemoval = true`

## Fetch Strategy

- All mapped relations are `LAZY`.
- N+1 demo endpoints intentionally trigger lazy loading.
- Optimized endpoints use `JOIN FETCH` or `EntityGraph`.
