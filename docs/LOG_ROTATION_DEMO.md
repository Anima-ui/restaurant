# Log Rotation Demo

## Purpose

This file explains how to quickly demonstrate log rotation during the laboratory defense.

## Current demo-friendly setup

In `logback-spring.xml` the rolling file appender uses:

- `maxFileSize = 256KB`
- archive folder `logs/archive`

This reduced limit is intentional for the laboratory demonstration, so rotation happens after a small number of requests.

## How to trigger rotation

1. Start the application.
2. Open the current log file:

```text
logs/restaurant-app.log
```

3. Repeatedly call endpoints that produce many logs, for example:

```text
GET /api/v1/restaurants/search/jpql?page=0&size=1&sort=name,asc
GET /api/v1/restaurants/search/native?page=0&size=1&sort=name,asc
GET /api/v1/restaurants/all
GET /api/v1/nplusone/problem
GET /api/v1/nplusone/optimized
```

Because Hibernate SQL logging and bind logging are enabled, these requests generate enough output quickly.

## What should happen

After the log file reaches about `256KB`:

- the current `logs/restaurant-app.log` is rolled over;
- archived logs appear in:

```text
logs/archive/
```

Expected file names:

```text
restaurant-app-YYYY-MM-DD.0.log
restaurant-app-YYYY-MM-DD.1.log
...
```

## What to show to the teacher

Show:

1. `logs/restaurant-app.log`
2. `logs/archive/`
3. that after several requests archive files appear automatically

Short explanation:

- logback writes logs to the current file;
- when the size limit is reached, it archives the old file;
- logging continues in a new active file.
