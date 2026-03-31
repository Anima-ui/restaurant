# Bulk Operations And Testing

## Что реализовано

1. Bulk-операция с бизнес-смыслом:
   `POST /api/v1/customers/bulk`
   Массовое добавление клиентов ресторана.

2. Демонстрация транзакций:
   `POST /api/v1/transactions/customers/bulk-no-tx`
   `POST /api/v1/transactions/customers/bulk-with-tx`

3. В сервисном слое используются:
   - `Stream API` для преобразования и проверки коллекций
   - `Optional` для безопасной обработки входных данных и поиска по телефону

## Как тестировать bulk-операцию

### 1. Успешный bulk

`POST /api/v1/customers/bulk`

```json
{
  "customers": [
    { "fullName": "Ivan Petrov", "phone": "+79990001122" },
    { "fullName": "Petr Ivanov", "phone": "+79990001123" }
  ]
}
```

Ожидаемый результат:
- оба клиента сохраняются
- `savedCount = 2`
- в ответе возвращается список созданных клиентов

### 2. Демонстрация без @Transactional

`POST /api/v1/transactions/customers/bulk-no-tx`

```json
{
  "customers": [
    { "fullName": "Ivan Petrov", "phone": "+79990001130" },
    { "fullName": "Petr Ivanov", "phone": "+79990001130" }
  ]
}
```

Ожидаемый результат:
- первый клиент успевает сохраниться
- на втором возникает конфликт
- данные первого клиента остаются в БД
- `savedCount = 1`

### 3. Демонстрация с @Transactional

`POST /api/v1/transactions/customers/bulk-with-tx`

```json
{
  "customers": [
    { "fullName": "Ivan Petrov", "phone": "+79990001140" },
    { "fullName": "Petr Ivanov", "phone": "+79990001140" }
  ]
}
```

Ожидаемый результат:
- возникает `409 Conflict`
- транзакция откатывается полностью
- ни один клиент из этого запроса не должен остаться в БД

## Как показать разницу преподавателю

1. Выполнить `bulk-no-tx` с повторяющимся телефоном.
2. Выполнить `GET /api/v1/customers`.
3. Показать, что первый клиент сохранился.
4. Выполнить `bulk-with-tx` с повторяющимся телефоном.
5. Снова выполнить `GET /api/v1/customers`.
6. Показать, что после транзакционного сценария новые клиенты не появились.

## Unit-тесты

Запуск:

```powershell
.\mvnw.cmd -q test
```

Основные тесты:
- `CustomerServiceImplTest`
- `TransactionDemoServiceImplTest`
- существующие тесты репозиториев и сервисов проекта
