# FocusBoard

A RESTful task management API built with Spring Boot, featuring JWT authentication, Redis caching, and PostgreSQL persistence.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Running with Docker](#running-with-docker)
  - [Running the Application](#running-the-application)
- [API Reference](#api-reference)
  - [Authentication](#authentication)
  - [Tasks](#tasks)
- [Data Models](#data-models)
- [Security](#security)
- [Caching](#caching)
- [Database Migrations](#database-migrations)
- [Environment & Configuration](#environment--configuration)

---

## Overview

FocusBoard is a backend API for managing personal tasks. Users can register, log in, and perform full CRUD operations on their tasks. Each user can only access their own data. The API supports pagination, sorting, and Redis-based caching for efficient data retrieval.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| ORM | Spring Data JPA / Hibernate |
| Migrations | Flyway |
| Authentication | Spring Security + JWT (JJWT 0.12.6) |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Build | Maven 3 |
| Containerization | Docker + Docker Compose |
| Utilities | Lombok |

---

## Architecture

```
focusboard/
├── src/main/java/com/swetonyancelmo/focusboard/
│   ├── common/                  # Shared utilities
│   ├── config/
│   │   ├── security/            # JWT filter, SecurityConfig
│   │   ├── AppConfig.java       # BCrypt encoder bean
│   │   ├── RedisConfig.java     # Cache manager configuration
│   │   └── SwaggerConfig.java   # OpenAPI / Swagger setup
│   ├── controller/
│   │   ├── docs/                # Swagger annotation interfaces
│   │   ├── AuthController.java
│   │   └── TaskController.java
│   ├── dtos/
│   │   ├── request/             # Incoming payload DTOs
│   │   └── response/            # Outgoing response DTOs
│   ├── exceptions/              # Custom exceptions + GlobalExceptionHandler
│   ├── model/
│   │   ├── enums/               # TaskStatus, TaskPriority
│   │   ├── Task.java
│   │   ├── User.java
│   │   └── RefreshToken.java
│   ├── repository/              # Spring Data JPA repositories
│   ├── service/                 # Business logic
│   │   ├── AuthService.java
│   │   ├── TaskService.java
│   │   └── UserDetailsServiceImpl.java
│   └── Startup.java             # Application entry point
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/            # Flyway versioned SQL scripts
├── docker-compose.yml
└── pom.xml
```

### Request Flow

```
Client → JwtAuthFilter → SecurityContext → Controller → Service → Repository → Database
                                                              ↕
                                                           Redis Cache
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose

### Running with Docker

Start PostgreSQL and Redis containers:

```bash
docker-compose up -d
```

This spins up:

| Service | Container | Host Port |
|---|---|---|
| PostgreSQL 16 | `focusboard-db` | `5433` |
| Redis 7 | `focusboard-redis` | `6379` |

### Running the Application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

Swagger UI: `http://localhost:8080/swagger-ui.html`

OpenAPI JSON spec: `http://localhost:8080/v3/api-docs`

---

## API Reference

All protected endpoints require a `Bearer` token in the `Authorization` header:

```
Authorization: Bearer <access_token>
```

---

### Authentication

#### Register

```http
POST /auth/register
Content-Type: application/json
```

**Request body:**

```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "secret123"
}
```

| Field | Type | Constraints |
|---|---|---|
| `name` | string | Required, max 100 chars |
| `email` | string | Required, valid email |
| `password` | string | Required, 6–20 chars |

**Response `201 Created`:**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Jane Doe",
  "email": "jane@example.com"
}
```

---

#### Login

```http
POST /auth/login
Content-Type: application/json
```

**Request body:**

```json
{
  "email": "jane@example.com",
  "password": "secret123"
}
```

**Response `200 OK`:**

```json
{
  "accessToken": "<jwt_access_token>",
  "refreshToken": "<refresh_token>"
}
```

---

#### Refresh Token

```http
POST /auth/refresh
Content-Type: application/json
```

**Request body:**

```json
{
  "refreshToken": "<refresh_token>"
}
```

**Response `200 OK`:**

```json
{
  "accessToken": "<new_jwt_access_token>",
  "refreshToken": "<new_refresh_token>"
}
```

---

#### Logout

```http
POST /auth/logout
Authorization: Bearer <access_token>
```

**Response `204 No Content`**

---

### Tasks

All task endpoints require authentication.

---

#### List Tasks

```http
GET /tasks
Authorization: Bearer <access_token>
```

**Query parameters:**

| Parameter | Type | Default | Description |
|---|---|---|---|
| `page` | integer | `0` | Page number (zero-indexed) |
| `size` | integer | `12` | Items per page |
| `direction` | string | `asc` | Sort direction: `asc` or `desc` (by title) |

**Response `200 OK`:**

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Write unit tests",
      "description": "Cover all service methods",
      "status": "TODO",
      "priority": "HIGH",
      "userId": "660e8400-e29b-41d4-a716-446655440001"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 12,
  "number": 0
}
```

Results are cached in Redis for 10 minutes per `userId + page + size` combination.

---

#### Create Task

```http
POST /tasks
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request body:**

```json
{
  "title": "Write unit tests",
  "description": "Cover all service methods",
  "status": "TODO",
  "priority": "HIGH"
}
```

| Field | Type | Constraints |
|---|---|---|
| `title` | string | Required, max 255 chars |
| `description` | string | Required |
| `status` | enum | Optional — `TODO`, `IN_PROGRESS`, `DONE` (default: `TODO`) |
| `priority` | enum | Optional — `LOW`, `MEDIUM`, `HIGH` (default: `MEDIUM`) |

**Response `201 Created`:** returns the created `TaskResponseDTO`.

---

#### Update Task

```http
PATCH /tasks/{id}
Authorization: Bearer <access_token>
Content-Type: application/json
```

**Request body** (all fields optional):

```json
{
  "title": "Write unit and integration tests",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM"
}
```

| Field | Type | Constraints |
|---|---|---|
| `title` | string | 3–255 chars |
| `description` | string | — |
| `status` | enum | `TODO`, `IN_PROGRESS`, `DONE` |
| `priority` | enum | `LOW`, `MEDIUM`, `HIGH` |

**Response `200 OK`:** returns the updated `TaskResponseDTO`.

---

#### Delete Task

```http
DELETE /tasks/{id}
Authorization: Bearer <access_token>
```

**Response `204 No Content`**

---

### Error Responses

All errors follow a consistent structure:

```json
{
  "timestamp": "2026-06-08T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/tasks",
  "fields": {
    "title": "must not be blank"
  }
}
```

| Status | Scenario |
|---|---|
| `400 Bad Request` | Validation error or business rule violation |
| `401 Unauthorized` | Missing or invalid JWT token |
| `404 Not Found` | Resource not found |
| `409 Conflict` | Email already registered |
| `500 Internal Server Error` | Unexpected error |

---

## Data Models

### User

| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `name` | VARCHAR(100) | NOT NULL |
| `email` | VARCHAR(150) | NOT NULL, UNIQUE |
| `password` | VARCHAR(255) | NOT NULL, BCrypt hashed |
| `created_at` | TIMESTAMP | NOT NULL, default NOW() |
| `updated_at` | TIMESTAMP | NOT NULL, default NOW() |

### Task

| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `title` | VARCHAR(255) | NOT NULL |
| `description` | TEXT | — |
| `status` | task_status | NOT NULL, default `TODO` |
| `priority` | task_priority | NOT NULL, default `MEDIUM` |
| `user_id` | UUID | FK → users(id) ON DELETE CASCADE |
| `created_at` | TIMESTAMP | NOT NULL |
| `updated_at` | TIMESTAMP | NOT NULL, auto-updated via trigger |

**Indexes:** `idx_tasks_user_id`, `idx_tasks_user_id_status`

### RefreshToken

| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, auto-generated |
| `user_id` | UUID | FK → users(id) ON DELETE CASCADE, UNIQUE |
| `token` | TEXT | NOT NULL, UNIQUE |
| `expires_at` | TIMESTAMPTZ | NOT NULL |

---

## Security

- **Stateless** sessions — no server-side session storage.
- **JWT access tokens** expire after **15 minutes**.
- **Refresh tokens** expire after **7 days** and are rotated on each refresh.
- Passwords are hashed with **BCrypt**.
- Each user can only access **their own** tasks — ownership is validated on every request.
- Public routes: `/auth/**`, `/v3/api-docs/**`, `/swagger-ui/**`.

---

## Caching

Redis is used to cache paginated task listings:

- **Cache name:** `tasks`
- **Cache key:** `userId:pageNumber:pageSize`
- **TTL:** 10 minutes
- **Invalidation:** any write operation (create, update, delete) evicts all cache entries for the authenticated user.
- Null values are not cached.
- Serialization: JSON via Jackson with Java time module support.

---

## Database Migrations

Schema is managed by **Flyway**. Migration scripts live in `src/main/resources/db/migration/`:

| Version | Description |
|---|---|
| `V1` | Create `users` table and email index |
| `V2` | Create `task_status` and `task_priority` enums, `tasks` table, indexes, and `updated_at` trigger |
| `V3` | Create `refresh_tokens` table |

Flyway runs automatically on application startup. JPA DDL is set to `validate` — schema changes must go through Flyway.

---

## Environment & Configuration

Key settings in `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/focusboard
    username: focusboard_user
    password: focusboard_pass
  data:
    redis:
      host: localhost
      port: 6379
  flyway:
    enabled: true
    locations: classpath:db/migration
  jpa:
    hibernate:
      ddl-auto: validate

app:
  jwt:
    secret: <base64-encoded-secret>
  refresh-token:
    expiration-days: 7
```

Docker Compose credentials:

| Variable | Value |
|---|---|
| `POSTGRES_USER` | `focusboard_user` |
| `POSTGRES_PASSWORD` | `focusboard_pass` |
| `POSTGRES_DB` | `focusboard` |
| Host port (Postgres) | `5433` |
| Host port (Redis) | `6379` |