# SPA клиент на React

## Что реализовано

В проект добавлен SPA-клиент на React, который работает поверх уже существующего Spring Boot API.

Фронтенд лежит в:

- [index.html](/d:/javalabs/app/src/main/resources/static/index.html)
- [app.js](/d:/javalabs/app/src/main/resources/static/app.js)
- [app.css](/d:/javalabs/app/src/main/resources/static/app.css)

После запуска backend клиент открывается по адресу:

- `http://localhost:8080/`

## Что показывает SPA

### Restaurants

- CRUD для ресторанов
- фильтрацию через:
  - `/api/v1/restaurants/search/jpql`
  - `/api/v1/restaurants/search/native`
- связь `OneToMany`:
  - `Restaurant -> tables`
  - `Restaurant -> dishes`
- связь `ManyToMany`:
  - `Restaurant <-> amenities`

### Customers

- список клиентов
- создание клиента
- запуск async bulk-операции
- проверку статуса async-задачи по `taskId`

### Bookings

- создание брони
- список броней
- обновление статуса брони

## Что было добавлено в backend для SPA

Чтобы показать `ManyToMany`, в API ресторанов добавлены `amenities`.

Изменённые части:

- [RestaurantDto.java](/d:/javalabs/app/src/main/java/com/restaurant/app/domain/dto/RestaurantDto.java)
- [RestaurantCreateRequest.java](/d:/javalabs/app/src/main/java/com/restaurant/app/domain/dto/RestaurantCreateRequest.java)
- [RestaurantUpdateRequest.java](/d:/javalabs/app/src/main/java/com/restaurant/app/domain/dto/RestaurantUpdateRequest.java)
- [RestaurantMapper.java](/d:/javalabs/app/src/main/java/com/restaurant/app/mapper/RestaurantMapper.java)
- [RestaurantServiceImpl.java](/d:/javalabs/app/src/main/java/com/restaurant/app/sevice/impl/RestaurantServiceImpl.java)
- [AmenityRepository.java](/d:/javalabs/app/src/main/java/com/restaurant/app/repository/AmenityRepository.java)

## Как тестировать

1. Запустить PostgreSQL, если он нужен локально.
2. Запустить backend:

```powershell
.\mvnw.cmd spring-boot:run
```

3. Открыть:

`http://localhost:8080/`

## Что показать на защите

### 1. CRUD

На вкладке `Restaurants`:

- создать ресторан
- отредактировать ресторан
- удалить ресторан

### 2. Фильтрация

На вкладке `Restaurants`:

- ввести `city`, `cuisine`, `dishName`
- переключить `JPQL / Native`
- показать результаты поиска

### 3. OneToMany

На карточке ресторана показать:

- список `tables`
- список `dishes`

### 4. ManyToMany

На карточке ресторана показать:

- список `amenities`

### 5. Async API

На вкладке `Customers`:

- запустить `async bulk`
- показать `taskId`
- несколько раз нажать `Обновить task status`
- показать изменение `savedCount`

### 6. Booking flow

На вкладке `Bookings`:

- выбрать клиента
- выбрать стол
- создать бронь
- перевести статус в `CONFIRMED` или `CANCELED`

## Важное замечание

SPA использует React через CDN, поэтому для открытия клиента нужен доступ браузера к внешним CDN-скриптам React и Babel.
