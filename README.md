# Restaurant Reservation System

## Description

Spring Boot REST application for managing restaurants.

The project is created as part of laboratory work and will be extended step by step.

## Tech stack

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* H2 Database
* Lombok
* Checkstyle

## Features (Lab 1)

* REST API for Restaurant entity
* GET by id
* GET by city
* Create restaurant
* DTO + Mapper
* Layered architecture (Controller → Service → Repository)
* Code style validation with Checkstyle

## How to run

```bash
mvn spring-boot:run
```

## H2 Console

```
http://localhost:8080/h2-console
```

JDBC URL:

```
jdbc:h2:mem:testdb
```

## Future work

* Relationships
* Booking system
* Caching
* Concurrency
* Client
* Docker deployment
