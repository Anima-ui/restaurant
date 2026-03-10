# Restaurant Management System ER Diagram

```plantuml
@startuml
hide methods
hide stereotypes
skinparam classAttributeIconSize 0

entity Customer {
  * id : Long
  --
  fullName : String
  phone : String
}

entity Restaurant {
  * id : Long
  --
  name : String
  city : String
  cuisineType : String
}

entity RestaurantTable {
  * id : Long
  --
  tableNumber : Integer
  seats : Integer
  restaurant_id : Long
}

entity Dish {
  * id : Long
  --
  name : String
  price : BigDecimal
  restaurant_id : Long
}

entity Booking {
  * id : Long
  --
  bookingTime : LocalDateTime
  status : BookingStatus
  customer_id : Long
  table_id : Long
}

Customer ||--o{ Booking : makes
Restaurant ||--o{ RestaurantTable : has
Restaurant ||--o{ Dish : offers
RestaurantTable ||--o{ Booking : receives

@enduml
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
- `Customer.bookings`: no cascade configured

## Fetch Strategy

- `Restaurant.tables`: `LAZY`
- `Restaurant.dishes`: `LAZY`
- `RestaurantTable.restaurant`: `LAZY`
- `RestaurantTable.bookings`: `LAZY`
- `Dish.restaurant`: `LAZY`
- `Booking.customer`: `LAZY`
- `Booking.table`: `LAZY`
- `Customer.bookings`: `LAZY`

## Project Match

- The diagram matches the current JPA entities in `src/main/java/com/restaurant/app/domain/model`.
- Field names use the same names as the Java model where possible: `fullName`, `cuisineType`, `tableNumber`, `bookingTime`.
- Foreign key columns match the current mappings: `restaurant_id`, `customer_id`, `table_id`.
