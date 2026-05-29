# TODO App

REST API для управления задачами с JWT-авторизацией и асинхронным аудитом действий пользователей.

Учебный проект, на котором я отрабатывал работу со Spring Boot, Spring Security, Spring Data JPA, миграциями БД через Liquibase, асинхронной обработкой и тестированием.

## Технологический стек

- **Java 25**, Spring Boot 3
- **Spring Security** + JWT (access & refresh tokens)
- **Spring Data JPA** + Hibernate
- **PostgreSQL** + Liquibase (миграции в YAML и SQL)
- **Spring Async** (`@Async` + `ThreadPoolTaskExecutor`) для фоновых задач
- **Lombok** для упрощения кода
- **Springdoc OpenAPI** для документации API
- **Maven** (через Maven Wrapper)

## Реализованные фичи

- Регистрация и авторизация пользователей с JWT (access + refresh tokens)
- CRUD-операции с задачами (каждый пользователь видит только свои задачи)
- Валидация входных данных через Bean Validation (`@Valid`, `@NotBlank` и др.)
- Глобальная обработка исключений через `@ControllerAdvice`
- **Асинхронный аудит действий пользователей** через `@Async`
- Пагинация и сортировка списков задач
- Поиск задач по названию
- Профили `dev` и `prod` с разными конфигурациями
- Миграции БД через Liquibase

## Тестирование

Сервисный слой покрыт unit-тестами с использованием JUnit 5, Mockito и AssertJ.

**Покрытие:**
- `TaskServiceImpl` — 10 тестов на CRUD-операции (create, getById, update, delete)
- `StringUtils` — 17 параметризованных тестов на граничные случаи

**Применяемые техники:**
- Структура given/when/then
- Изоляция зависимостей через `@Mock` и `@InjectMocks`
- Проверка возвращаемых значений через AssertJ (`assertThat`)
- Проверка побочных эффектов через `verify` (вызовы аудита)
- Подмена `SecurityContext` для тестирования логики авторизации
- Проверка позитивных и негативных сценариев (TaskNotFoundException, AccessDeniedException)

Запуск всех тестов:

    ./mvnw test

## Архитектура асинхронного аудита

При создании, изменении или удалении задачи в фоновом потоке (через `@Async`) сохраняется запись в таблицу `audit_log`. Основной HTTP-запрос **не блокируется** — пользователь получает ответ мгновенно, а аудит записывается параллельно.

Конфигурация пула потоков — в `AsyncConfig`:
- `corePoolSize: 5`
- `maxPoolSize: 20`
- `queueCapacity: 50`
- Политика отказа: `CallerRunsPolicy`
- Глобальный обработчик ошибок: `AsyncUncaughtExceptionHandler`

## Запуск локально

### Требования

- Java 25+
- PostgreSQL 14+
- Maven (через Maven Wrapper, отдельно ставить не нужно)

### Шаги запуска

1. Создать БД в PostgreSQL:
```sql
   CREATE DATABASE todo_db;
```

2. Запустить приложение:
```bash
   ./mvnw spring-boot:run
```

3. Приложение откроется на `http://localhost:8080`.

4. Liquibase автоматически создаст все таблицы при первом запуске.

5. Swagger UI доступен (в dev-профиле): `http://localhost:8080/swagger-ui.html`

### Переменные окружения (опционально)

Все переменные имеют дефолтные значения для локальной разработки. Для prod-окружения **обязательны**:

| Переменная | Default (dev) | Описание |
|------------|---------------|----------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/todo_db` | URL базы данных |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Имя пользователя БД |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Пароль БД |
| `JWT_SECRET` | dev fallback | Секрет для подписи JWT (обязательно для prod!) |
| `JWT_EXPIRATION` | 86400000 (24 ч) | Время жизни access token (мс) |
| `JWT_REFRESH_EXPIRATION` | 604800000 (7 дн) | Время жизни refresh token (мс) |
