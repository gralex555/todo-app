# TODO App

REST API для управления задачами с JWT-авторизацией и асинхронным аудитом действий пользователей.


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

- Сервисный слой покрыт unit-тестами с использованием JUnit 5, Mockito и AssertJ.
- Тестирование REST-эндпоинтов через @WebMvcTest и MockMvc
  (12 тестов на TaskController, проверка статусов и JSON через jsonPath)
- Подмена аутентификации через @WithMockUser для тестов защищённых эндпоинтов
- Тестирование репозиториев через @DataJpaTest с in-memory H2
    (5 тестов на TaskRepository, проверка производных запросов Spring Data)
- TestEntityManager для подготовки данных в обход тестируемого репозитория
- Отдельная конфигурация application-test.yaml для изоляции тестов
- Интеграционные тесты с Testcontainers и реальной PostgreSQL
  (5 тестов на TaskRepository, проверка на той же СУБД, что в продакшене)
- Liquibase в тестах: прогон реальных миграций обеспечивает идентичность тестовой и продакшен-схемы
- Hibernate ddl-auto: validate для проверки соответствия сущностей и миграций
- @DynamicPropertySource для динамической настройки Spring под Docker-контейнер

## Покрытие

**Всего 49 тестов** по всем слоям приложения:
- 17 тестов на утилиты (StringUtils) — параметризованные тесты на JUnit 5 + AssertJ
- 10 тестов на сервисы (TaskServiceImpl) — Mockito с проверкой позитивных и негативных сценариев
- 12 тестов на контроллеры (TaskController) — @WebMvcTest с MockMvc
- 5 тестов на репозитории с in-memory H2 — @DataJpaTest для быстрой проверки запросов
- 5 интеграционных тестов с Testcontainers и реальной PostgreSQL — на той же СУБД, что в продакшене


- **Применяемые техники:**


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

## Кэширование (Redis)

Для оптимизации чтения задач используется Redis в качестве кэша.

### Конфигурация
- Кэшируется метод `TaskService.getById(id)` через `@Cacheable("tasks")`
- При `update` или `delete` ключ сбрасывается через `@CacheEvict`
- TTL: 10 минут (автоматическая инвалидация)
- Сериализация: JSON через Jackson 3


### Запуск
Redis запускается автоматически вместе с приложением:

    docker compose up

Контейнер `todo-redis` доступен на `localhost:6379` для отладки.

### Просмотр кэша

    docker exec -it todo-redis redis-cli
    KEYS *
    GET "tasks::1"

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

### Переменные окружения

Все переменные имеют дефолтные значения для локальной разработки. Для prod-окружения **обязательны**:

| Переменная | Default (dev) | Описание |
|------------|---------------|----------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/todo_db` | URL базы данных |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Имя пользователя БД |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Пароль БД |
| `JWT_SECRET` | dev fallback | Секрет для подписи JWT (обязательно для prod!) |
| `JWT_EXPIRATION` | 86400000 (24 ч) | Время жизни access token (мс) |
| `JWT_REFRESH_EXPIRATION` | 604800000 (7 дн) | Время жизни refresh token (мс) |

## Запуск через Docker

Приложение упаковано в Docker-контейнер. Для запуска нужны:
- Docker Desktop (или Docker Engine на Linux)
- Свободные порты 8080 и 5433

Запуск всей инфраструктуры (приложение + PostgreSQL):

    docker compose up

Приложение будет доступно по адресу http://localhost:8080
Swagger UI — http://localhost:8080/swagger-ui.html
PostgreSQL — localhost:5433 (для отладки через любой SQL-клиент)

Остановка:

    docker compose down

## Сборка Docker-образа

Используется многоэтапная сборка (multi-stage build):
- Этап сборки: eclipse-temurin:25-jdk + Maven для сборки jar
- Этап запуска: eclipse-temurin:25-jre + готовый jar

Это уменьшает размер образа и не требует Maven на машине развёртывания.