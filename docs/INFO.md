# Project Entities

This file describes each entity, its relations, and how it is used in the program.

## Restaurant

Purpose:

- main entity of the application
- represents one restaurant
- root object for tables and dishes

Fields:

- `id`
- `name`
- `city`
- `cuisineType`

Relations:

- `Restaurant 1 -> N RestaurantTable`
- `Restaurant 1 -> N Dish`

Used in program:

- create, read, update, delete through `/api/v1/restaurants`
- transaction examples
- N+1 examples

Code:

- [Restaurant.java](d:/javalabs/app/src/main/java/com/restaurant/app/domain/model/Restaurant.java)

## RestaurantTable

Purpose:

- represents a real table inside a restaurant
- stores its own business data, so it is not a join table

Fields:

- `id`
- `tableNumber`
- `seats`

Relations:

- `RestaurantTable N -> 1 Restaurant`
- `RestaurantTable 1 -> N Booking`

Used in program:

- created together with restaurant
- used for booking creation
- used in nested N+1 examples
- used in transaction demos

Code:

- [RestaurantTable.java](d:/javalabs/app/src/main/java/com/restaurant/app/domain/model/RestaurantTable.java)

## Dish

Purpose:

- menu item of a restaurant

Fields:

- `id`
- `name`
- `price`

Relations:

- `Dish N -> 1 Restaurant`

Used in program:

- created together with restaurant
- used in simple N+1 scenario `Restaurant -> Dish`

Code:

- [Dish.java](d:/javalabs/app/src/main/java/com/restaurant/app/domain/model/Dish.java)

## Customer

Purpose:

- client who can create bookings

Fields:

- `id`
- `fullName`
- `phone`

Relations:

- `Customer 1 -> N Booking`

Used in program:

- created through `/api/v1/customers`
- linked to booking creation

Code:

- [Customer.java](d:/javalabs/app/src/main/java/com/restaurant/app/domain/model/Customer.java)

## Booking

Purpose:

- reservation of a specific table by a specific customer at a specific time

Fields:

- `id`
- `bookingTime`
- `status`

Relations:

- `Booking N -> 1 Customer`
- `Booking N -> 1 RestaurantTable`

Used in program:

- created through `/api/v1/bookings`
- status updated through `/api/v1/bookings/{id}/status`
- used in nested N+1 examples

Code:

- [Booking.java](d:/javalabs/app/src/main/java/com/restaurant/app/domain/model/Booking.java)

## BookingStatus

Purpose:

- enum that defines booking state

Values:

- `CREATED`
- `CONFIRMED`
- `CANCELED`

Used in program:

- passed in booking create request
- changed in booking status update request
- returned by `/api/v1/bookings/statuses`

Code:

- [BookingStatus.java](d:/javalabs/app/src/main/java/com/restaurant/app/domain/model/BookingStatus.java)

## Relation Summary

- `Restaurant` owns `RestaurantTable` and `Dish`
- `Customer` owns bookings logically
- `Booking` connects customer and restaurant table
- `RestaurantTable` participates in reservation flow and N+1 nested loading

## Related Docs

- [ER_DIAGRAM.md](d:/javalabs/app/docs/ER_DIAGRAM.md)
- [TEST.md](d:/javalabs/app/docs/TEST.md)
- [N_PLUS_ONE.md](d:/javalabs/app/docs/N_PLUS_ONE.md)
