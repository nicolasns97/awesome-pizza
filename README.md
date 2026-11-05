# Awesome Pizza

REST API for a pizzeria: customers place and track orders, the kitchen takes them from a queue one at a time.

**Stack:** Java 27, Spring Boot 4, PostgreSQL 18, Flyway, Docker.

## Run

```bash
cp .env.example .env
docker compose up -d --build
```

API docs: http://localhost:8080/swagger-ui.html. Kitchen endpoints require the `X-Kitchen-Token` header.

## API

| Method | Path | Who |
|---|---|---|
| `GET` | `/api/v1/menu` | customer |
| `POST` | `/api/v1/orders` | customer |
| `GET` | `/api/v1/orders/{trackingCode}` | customer |
| `POST` | `/api/v1/orders/{trackingCode}/cancel` | customer |
| `GET` | `/api/v1/orders?status=&page=&size=` | kitchen |
| `POST` | `/api/v1/orders/next` | kitchen |
| `POST` | `/api/v1/orders/{trackingCode}/complete` | kitchen |

Order lifecycle: `CREATED → IN_PROGRESS → COMPLETED`, or `CREATED → CANCELED`.

