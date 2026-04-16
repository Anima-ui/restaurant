# CI/CD и Деплой

Эта папка описывает CI/CD-настройки проекта.

Рабочий GitHub Actions workflow находится здесь:
- [.github/workflows/ci-cd.yml](d:/javalabs/app/.github/workflows/ci-cd.yml)

Он лежит именно в `.github/workflows`, потому что GitHub запускает workflow только из этой директории. В этой папке оставлена документация и список секретов, которые нужно добавить в GitHub.

## Что делает workflow

Workflow `CI/CD` выполняет 4 этапа:

1. `build-and-test`
   - скачивает код
   - ставит JDK 21
   - запускает Maven validate
   - запускает все тесты
   - сохраняет JaCoCo report как artifact

2. `docker-build`
   - собирает Docker image из [Dockerfile](d:/javalabs/app/Dockerfile)
   - проверяет, что приложение реально контейнеризуется

3. `deploy`
   - вызывается только при `push`
   - дергает Render Deploy Hook
   - если secret не задан, деплой пропускается

4. `healthcheck`
   - после деплоя проверяет `/actuator/health`
   - если приложение не становится healthy, job падает

## GitHub Secrets

В GitHub нужно открыть:

`Repository -> Settings -> Secrets and variables -> Actions -> New repository secret`

Добавить:

- `RENDER_DEPLOY_HOOK_URL`
  - URL deploy hook из Render
  - нужен для автоматического деплоя

- `APP_HEALTH_URL`
  - полный URL health endpoint
  - пример:
    `https://restaurant-app.onrender.com/actuator/health`

Если эти secrets не заданы:
- сборка и тесты всё равно будут работать
- Docker build тоже будет работать
- deploy и healthcheck будут пропущены

## PaaS

Для Render добавлен blueprint:
- [render.yaml](d:/javalabs/app/render.yaml)

Он описывает:
- web-сервис приложения
- Docker runtime
- PostgreSQL database
- переменные окружения
- healthcheck path `/actuator/health`

Для PaaS база передаётся не одной строкой, а отдельными переменными:
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Spring собирает JDBC URL в [application.yaml](d:/javalabs/app/src/main/resources/application.yaml). Это сделано потому, что некоторые PaaS отдают Postgres connection string в формате `postgres://...`, а Spring datasource ожидает `jdbc:postgresql://...`.

Фактическое размещение на PaaS нужно сделать через аккаунт Render:

1. Загрузить проект на GitHub.
2. В Render выбрать `New -> Blueprint`.
3. Подключить GitHub repository.
4. Render прочитает [render.yaml](d:/javalabs/app/render.yaml).
5. После создания сервиса скопировать Deploy Hook.
6. Добавить Deploy Hook в GitHub secret `RENDER_DEPLOY_HOOK_URL`.
7. Добавить URL healthcheck в `APP_HEALTH_URL`.

## Healthcheck

Healthcheck endpoint:

```text
GET /actuator/health
```

Ожидаемый ответ:

```json
{
  "status": "UP"
}
```

Он работает благодаря зависимости:

```xml
spring-boot-starter-actuator
```

и настройкам в:
- [application.yaml](d:/javalabs/app/src/main/resources/application.yaml)

## Переменные окружения

Безопасный пример для GitHub лежит в:
- [env.example](d:/javalabs/app/env.example)

Для локального запуска можно скопировать его в `.env` и заменить значения:

```powershell
Copy-Item env.example .env
```

После этого Docker Compose будет брать значения из `.env`.

## Локальный запуск через Docker Compose

Команда:

```powershell
docker compose up -d --build
```

Что поднимется:
- приложение
- PostgreSQL
- pgAdmin

Адрес приложения:

```text
http://localhost:8080
```

Healthcheck:

```text
http://localhost:8080/actuator/health
```

pgAdmin:

```text
http://localhost:5050
```

## Что сказать на защите

Короткая формулировка:

В проект добавлен Dockerfile для сборки Spring Boot приложения в контейнер. Docker Compose поднимает приложение вместе с PostgreSQL и использует переменные окружения из `.env`. Для проверки работоспособности добавлен actuator health endpoint `/actuator/health`. CI/CD настроен через GitHub Actions: workflow выполняет validate, тесты, Docker build, деплой через Render Deploy Hook и последующий healthcheck развернутого приложения.
