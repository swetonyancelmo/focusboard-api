# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

FocusBoard is a stateless RESTful task management API built with Spring Boot. Users register, log in, and perform CRUD operations on their own tasks. The API uses JWT for authentication, Redis for caching, and PostgreSQL for persistence.

## Commands

### Start infrastructure (PostgreSQL + Redis)
```bash
docker-compose up -d
```

### Run the application
```bash
mvn spring-boot:run
```

### Build (skip tests)
```bash
mvn clean package -DskipTests
```

### Run all tests
```bash
mvn test
```

### Run a single test class
```bash
mvn test -Dtest=FocusboardApplicationTests
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

## Architecture

### Request Flow
```
Client → JwtAuthFilter → SecurityContext → Controller → Service → Repository → PostgreSQL
                                                              ↕
                                                           Redis Cache
```

### Layer Responsibilities

- **`config/security/`** — `JwtService` (token generation/validation via JJWT), `JwtAuthFilter` (per-request token extraction and `SecurityContext` population), `SecurityConfig` (filter chain, public routes, stateless sessions).
- **`config/`** — `AppConfig` (BCrypt bean), `RedisConfig` (Jackson-based cache manager with 10min TTL), `SwaggerConfig` (OpenAPI setup).
- **`controller/`** — Thin layer that resolves the authenticated user's UUID from `SecurityContext` and delegates to services. Swagger annotations are extracted into `controller/docs/` interfaces to keep controllers clean.
- **`service/`** — All business logic lives here. `AuthService` handles register/login/refresh/logout. `TaskService` handles CRUD with `@Cacheable`/`@CacheEvict`.
- **`common/RestPage`** — Custom `PageImpl` subclass required for Redis to serialize/deserialize paginated results correctly via Jackson.
- **`exceptions/`** — `GlobalExceptionHandler` (`@RestControllerAdvice`) centralizes error responses into a consistent `ApiErrorResponse` shape.

### Authentication & Token Flow

1. `POST /auth/login` → authenticates via `AuthenticationManager` → issues a JWT access token (15min) and a refresh token (7 days, stored in `refresh_tokens` table).
2. Every protected request passes through `JwtAuthFilter`, which extracts the bearer token, validates it via `JwtService`, and loads the user via `UserDetailsServiceImpl`.
3. `POST /auth/refresh` → validates the DB-stored refresh token, rotates it (old deleted, new issued), and returns a new access token.
4. `POST /auth/logout` → deletes the refresh token from the DB.

### Caching Strategy

`@Cacheable` on `TaskService.findAllTasks` uses key `userId:pageNumber:pageSize`. Any write operation (create, update, delete) triggers `@CacheEvict(allEntries = true)` — this evicts **all** task cache entries, not just the affected user's, because the key space isn't bounded per user in the eviction annotation.

### Database Migrations

Flyway manages the schema — `ddl-auto` is set to `validate`, so JPA never modifies the schema. All changes must go through versioned SQL files in `src/main/resources/db/migration/`. Current migrations: V1 (users), V2 (tasks with enums + trigger for `updated_at`), V3 (refresh_tokens).

### Configuration

Key `application.yml` properties:
- `security.jwt.secret` — Base64URL-encoded HMAC secret (hardcoded dev value in repo; override in prod).
- `security.jwt.refresh-token-expiration-days` — also bound via `@Value("${security.jwt.refresh-token-expiration-days:7}")` in `AuthService`.
- DB connects to `localhost:5433` (non-standard port to avoid conflicts with a local Postgres instance).

### Ownership Enforcement

Every task query uses `findByIdAndUserId` or `deleteByIdAndUserId` — the `userId` extracted from the JWT is always passed alongside the task ID, so users can never read or mutate another user's tasks.