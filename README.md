# Product Service

A production-grade **Product** microservice built with **Spring Boot 3.3 / Java 17**, backed by **PostgreSQL** through JPA/Hibernate, with a clean layered architecture, request validation, and a centralized exception-handling strategy.

## Architecture

```
Controller  -->  Service (interface + impl)  -->  Repository  -->  PostgreSQL
    |                    |
   DTOs  <---------  Mapper  <--------------------------  Entity
    |
GlobalExceptionHandler (cross-cutting, @RestControllerAdvice)
```

| Layer | Package | Responsibility |
|---|---|---|
| Controller | `controller` | HTTP concerns only: routing, status codes, `@Valid` triggering |
| Service | `service`, `service.impl` | Business rules, transaction boundaries |
| Repository | `repository` | Spring Data JPA persistence access |
| Entity | `entity` | JPA-mapped persistence model |
| DTO | `dto` | Public API contract, decoupled from the entity |
| Mapper | `mapper` | Explicit entity <-> DTO conversion |
| Exception | `exception` | Custom exceptions + global `@RestControllerAdvice` |

Why DTOs instead of exposing the entity directly: the API contract can evolve independently of the persistence model, we avoid leaking JPA proxies/lazy-loading issues over the wire, and we control exactly which fields are exposed.

## Tech Stack

- Java 17, Spring Boot 3.3.2
- Spring Web (REST), Spring Data JPA, Bean Validation (Jakarta Validation)
- PostgreSQL + Liquibase (versioned schema migrations, `ddl-auto: validate`)
- Lombok
- springdoc-openapi (Swagger UI)
- Spring Boot Actuator (health/readiness/liveness probes for k8s)
- JUnit 5, Mockito, AssertJ, MockMvc, H2 (test profile)

## Key production-readiness decisions

- **Liquibase over `ddl-auto=update`**: schema changes are explicit, versioned XML changesets (`src/main/resources/db/changelog`), reviewable in PRs, and safe to run in CI/CD. Each changeset has a unique `id`/`author` so Liquibase can track exactly what's applied via its `DATABASECHANGELOG` table. Hibernate is set to `validate` only.
- **Optimistic locking (`@Version`)**: concurrent updates to the same product don't silently overwrite each other; a `409 Conflict` is returned instead.
- **Uniform error contract**: every error (validation, not-found, conflict, malformed JSON, unsupported method, unexpected exception) returns the same `ErrorResponse` JSON shape, so clients can handle errors generically.
- **Externalized configuration**: DB URL/credentials/port come from environment variables with sane local defaults — no secrets in source.
- **Connection pool tuning**: HikariCP sized explicitly rather than left on defaults.
- **`open-in-view: false`**: avoids the "OSIV" anti-pattern that hides lazy-loading N+1 issues.
- **Pagination capped at 100/page** server-side, regardless of what a client requests, to protect the DB.
- **Actuator health/readiness/liveness** endpoints for container orchestration.

## Getting Started

### Option A — Docker Compose (fastest)

```bash
docker compose up --build
```

This starts PostgreSQL and the service together. The API is then available at `http://localhost:8080`.

### Option B — Run locally against your own Postgres

1. Create a database and user:
   ```sql
   CREATE DATABASE product_service_db;
   CREATE USER product_service WITH PASSWORD 'product_service';
   GRANT ALL PRIVILEGES ON DATABASE product_service_db TO product_service;
   ```
2. Export config (or rely on the defaults, which match the SQL above):
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/product_service_db
   export DB_USERNAME=product_service
   export DB_PASSWORD=product_service
   ```
3. Run:
   ```bash
   mvn spring-boot:run
   ```

Liquibase will create the `products` table automatically on startup.

### Running tests

```bash
mvn test
```

Tests run against an in-memory H2 database (`test` profile) — no external DB needed.

### API Docs

Once running: `http://localhost:8080/swagger-ui.html`

## API Reference

Base path: `/api/v1/products`

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/products` | Create a product |
| GET | `/api/v1/products/{id}` | Get a product by id |
| GET | `/api/v1/products?name=&category=&page=&size=&sortBy=&direction=` | Paginated search/list |
| PUT | `/api/v1/products/{id}` | Full update |
| DELETE | `/api/v1/products/{id}` | Delete |

### Create a product

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
        "sku": "SKU-1001",
        "name": "Wireless Mouse",
        "description": "Ergonomic wireless mouse",
        "price": 29.99,
        "quantity": 250,
        "category": "Electronics",
        "active": true
      }'
```

Response `201 Created` (with `Location` header pointing at the new resource):

```json
{
  "id": 1,
  "sku": "SKU-1001",
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse",
  "price": 29.99,
  "quantity": 250,
  "category": "Electronics",
  "active": true,
  "version": 0,
  "createdAt": "2026-07-16T10:15:30Z",
  "updatedAt": "2026-07-16T10:15:30Z"
}
```

### Validation error example

Request with a blank `name` returns `400 Bad Request`:

```json
{
  "timestamp": "2026-07-16T10:16:02Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/products",
  "fieldErrors": [
    { "field": "name", "message": "Name must not be blank", "rejectedValue": "" }
  ]
}
```

### Not found example

`GET /api/v1/products/999` returns `404 Not Found`:

```json
{
  "timestamp": "2026-07-16T10:17:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: 999",
  "path": "/api/v1/products/999"
}
```

### Duplicate SKU example

`409 Conflict`:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "A product with SKU 'SKU-1001' already exists",
  "path": "/api/v1/products"
}
```

## Project layout

```
product-service/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── src
    ├── main
    │   ├── java/com/example/productservice
    │   │   ├── ProductServiceApplication.java
    │   │   ├── controller/ProductController.java
    │   │   ├── service/ProductService.java
    │   │   ├── service/impl/ProductServiceImpl.java
    │   │   ├── repository/ProductRepository.java
    │   │   ├── entity/Product.java
    │   │   ├── dto/ProductRequestDTO.java
    │   │   ├── dto/ProductResponseDTO.java
    │   │   ├── dto/PagedResponse.java
    │   │   ├── mapper/ProductMapper.java
    │   │   └── exception/
    │   │       ├── ResourceNotFoundException.java
    │   │       ├── DuplicateResourceException.java
    │   │       ├── ErrorResponse.java
    │   │       └── GlobalExceptionHandler.java
    │   └── resources
    │       ├── application.yml
    │       └── db/changelog
    │           ├── db.changelog-master.xml
    │           └── changes/001-create-products-table.xml
    └── test/java/com/example/productservice
        ├── ProductServiceApplicationTests.java
        ├── service/ProductServiceImplTest.java
        └── controller/ProductControllerTest.java
```

## Next steps you may want to add

- AuthN/AuthZ (e.g. Spring Security + OAuth2/JWT) — not included since it wasn't in scope.
- Rate limiting / API gateway in front of this service.
- Distributed tracing (Micrometer Tracing + OTel) if this runs alongside other services.
- CI pipeline (build, test, image scan, push) — a GitHub Actions workflow is a natural fit given the Dockerfile provided.
