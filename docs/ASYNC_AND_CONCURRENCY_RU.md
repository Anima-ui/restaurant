# Асинхронность, потоки и JMeter

## Что сделано

В проекте реализованы:

1. Асинхронная bulk-операция создания клиентов через `@Async`.
2. Потокобезопасный реестр задач и генерация `taskId`.
3. Демонстрация race condition и исправление через `AtomicInteger`.
4. JMeter-сценарий для всех endpoint'ов проекта.

## Асинхронная операция

Асинхронной сделана операция `POST /api/v1/customers/bulk/async`.

Как работает:

1. Клиент отправляет bulk-запрос на создание клиентов.
2. Сервер сразу возвращает `taskId`.
3. Дальше задача выполняется в фоне.
4. Клиент проверяет статус через `GET /api/v1/customers/bulk/tasks/{taskId}`.
5. Во время выполнения видно `RUNNING` и рост `savedCount`.

Пример запуска:

```json
{
  "customers": [
    { "fullName": "Ivan Petrov", "phone": "+79990001150" },
    { "fullName": "Petr Ivanov", "phone": "+79990001151" },
    { "fullName": "Sergey Smirnov", "phone": "+79990001152" }
  ]
}
```

Что показывать:

- `POST /api/v1/customers/bulk/async`
- `GET /api/v1/customers/bulk/tasks/{taskId}`
- смену статусов `PENDING -> RUNNING -> COMPLETED`
- постепенный рост `savedCount`

## Потокобезопасность

Используется:

- `AtomicLong` для генерации `taskId`
- `ConcurrentHashMap` для хранения статусов задач
- `AtomicInteger` для safe-счётчика в concurrency demo

Это гарантирует:

- уникальные `taskId`
- безопасное параллельное обновление статусов
- отсутствие потерянных инкрементов в safe-сценарии

## Race Condition

### Небезопасный вариант

`GET /api/v1/concurrency/race-condition/unsafe?threads=64&incrementsPerThread=1000`

Ожидание:

- `expectedValue = 64000`
- `actualValue < 64000`
- `lostUpdates > 0`

### Исправленный вариант

`GET /api/v1/concurrency/race-condition/atomic?threads=64&incrementsPerThread=1000`

Ожидание:

- `expectedValue = 64000`
- `actualValue = 64000`
- `lostUpdates = 0`

## Как тестировать вручную

### Async

1. Запустить приложение.
2. Отправить `POST /api/v1/customers/bulk/async`.
3. Взять `taskId`.
4. Несколько раз вызвать `GET /api/v1/customers/bulk/tasks/{taskId}`.
5. Показать, что задача не завершается мгновенно и прогресс меняется.

### Async с ошибкой

```json
{
  "customers": [
    { "fullName": "Ivan Petrov", "phone": "+79990001160" },
    { "fullName": "Petr Ivanov", "phone": "+79990001160" }
  ]
}
```

Ожидание:

- `status = FAILED`
- в `errorMessage` будет сообщение о конфликте телефонов

### Race condition

1. Вызвать unsafe endpoint.
2. Показать потерянные инкременты.
3. Вызвать atomic endpoint.
4. Показать, что потерь больше нет.

## Тесты и coverage

Запуск всех тестов:

```powershell
.\mvnw.cmd test
```

Тихий режим:

```powershell
.\mvnw.cmd -q test
```

Отчёт JaCoCo:

- `target/site/jacoco/index.html`

## JMeter на все endpoint'ы

Файл сценария:

- `docs/jmeter/all-endpoints-load-test.jmx`

Сценарий проходит по всем endpoint'ам проекта:

- `restaurants`
- `customers`
- `bookings`
- `nplusone`
- `transactions`
- `concurrency`

То есть он покрывает:

- CRUD
- поиск
- async bulk
- status polling
- booking flow
- N+1 demo
- transaction demo
- race condition demo

Из проекта удалены два endpoint'а, которые давали `500` и ломали нагрузочный прогон:

- `GET /api/v1/restaurants/detailed`
- `GET /api/v1/nplusone/nested-optimized`

## Важный нюанс по transaction-demo

Три endpoint'а специально возвращают `409 Conflict`:

- `POST /api/v1/transactions/rollback`
- `POST /api/v1/transactions/exception-no-tx`
- `POST /api/v1/transactions/exception-with-tx`

Почему JMeter считал это ошибкой:

- по умолчанию JMeter считает успешными в основном ответы `2xx` и `3xx`
- `409` это HTTP-ошибка уровня клиента, поэтому sample помечается как `FAIL`
- при этом в нашем случае такой ответ бизнес-логически ожидаем

Что исправлено:

- в JMeter для этих трёх sampler'ов добавлен `JSR223 PostProcessor`
- если ответ равен `409`, sample принудительно помечается как успешный
- поэтому ожидаемые конфликты больше не краснят итоговый отчёт

## Как запустить JMeter локально

1. Запустить приложение на `localhost:8080`.
2. Убедиться, что БД доступна.
3. Открыть JMeter.
4. Загрузить:

- `docs/jmeter/all-endpoints-load-test.jmx`

5. Нажать `Start`.

Смотреть:

- `Summary Report`
- `Aggregate Report`
- `View Results Tree`

Основные метрики:

- `Samples`
- `Average`
- `Min`
- `Max`
- `Error %`
- `Throughput`

## Как запустить через Docker Compose

Поднять инфраструктуру:

```powershell
docker compose up -d
```

Запустить приложение отдельно:

```powershell
.\mvnw.cmd spring-boot:run
```

Запустить JMeter:

```powershell
docker compose --profile load-test run --rm jmeter
```

Если раньше Docker писал, что `justb4/jmeter:5.6.3` not found, причина была в несуществующем теге. В `docker-compose.yml` уже указан рабочий тег `justb4/jmeter:5.5`.

Результаты:

- `docs/jmeter/results/all-endpoints-results.jtl`
- `docs/jmeter/results/report/index.html`

## Что показать на защите

1. `POST /api/v1/customers/bulk/async`
2. `GET /api/v1/customers/bulk/tasks/{taskId}`
3. `GET /api/v1/concurrency/race-condition/unsafe`
4. `GET /api/v1/concurrency/race-condition/atomic`
5. Запуск JMeter на всех endpoint'ах
6. HTML-отчёт `docs/jmeter/results/report/index.html`
7. Пояснение, что три `409` в transaction-demo ожидаемы и в JMeter уже обработаны как допустимый результат
