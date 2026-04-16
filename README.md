# Restaurant Reservation System

Spring Boot проект для управления ресторанами, клиентами и бронированиями. В проекте есть REST API, простой React SPA-клиент, PostgreSQL, транзакционные сценарии, async-задачи, демонстрация race condition, тесты, Docker и CI/CD.

## Возможности

- CRUD для ресторанов, клиентов и бронирований
- Связи `OneToMany` и `ManyToMany`: столы, блюда, удобства, брони
- Фильтрация ресторанов в SPA-клиенте
- JPQL и native SQL поиск
- Кэширование результатов поиска
- Демонстрация N+1 проблемы и оптимизации
- Bulk-операции для клиентов
- Async bulk-операция с `taskId` и проверкой статуса
- Демонстрация race condition и решения через `AtomicInteger`
- Глобальная обработка ошибок и логирование
- JMeter-сценарий нагрузочного тестирования
- Dockerfile и Docker Compose
- GitHub Actions CI/CD

## Стек

- Java 21
- Spring Boot 4
- Spring Data JPA
- PostgreSQL
- H2 для тестов
- Maven
- JaCoCo
- Docker / Docker Compose
- React через CDN
- JMeter

## Быстрый запуск через Docker

Создать локальный `.env`:

```powershell
Copy-Item env.example .env
```

Запустить приложение и БД:

```powershell
docker compose up -d --build
```

Открыть приложение:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

Healthcheck:

```text
http://localhost:8080/actuator/health
```

Остановить:

```powershell
docker compose down
```

## Запуск без Docker

Нужен локальный PostgreSQL с базой `restaurant_lab`.

```powershell
.\mvnw.cmd spring-boot:run
```

## Тесты

Запуск всех тестов:

```powershell
.\mvnw.cmd test
```

JaCoCo report:

```text
target/site/jacoco/index.html
```

## Нагрузочное тестирование

JMeter запускается через отдельный Docker Compose profile:

```powershell
docker compose --profile load-test run --rm jmeter
```

## CI/CD

Workflow находится здесь:

```text
.github/workflows/ci-cd.yml
```

Он выполняет:

- Maven validate
- тесты
- Docker build
- deploy через Render Deploy Hook
- healthcheck после деплоя

Для деплоя нужны GitHub Secrets:

```text
RENDER_DEPLOY_HOOK_URL
APP_HEALTH_URL
```

## PaaS

Для Render добавлен blueprint:

```text
render.yaml
```

Он описывает приложение, PostgreSQL и переменные окружения.

## Документация

Дополнительные локальные материалы лежат в `docs/` и `ci-cd/`.

> Сейчас `docs/` добавлена в `.gitignore`, поэтому эти файлы не попадут в GitHub, если не убрать это правило.
