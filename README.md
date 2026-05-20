# Nanolink - URL Shortener API

Nanolink is a URL shortener REST API built with **Spring Boot 4.0.6**, **PostgreSQL 18.3**, and **Java 25**. It includes JWT auth, click analytics, QR codes, rate limiting, and Dockerized deployment.

## Highlights

- JWT authentication (7-day tokens)
- Link CRUD with custom aliases
- Expiration handling (410 Gone for expired links)
- Click tracking + analytics aggregation
- QR code generation (PNG)
- Rate limiting + CORS
- Swagger/OpenAPI docs

## Quick Start (Docker)

### Requirements

- Docker Desktop (Windows/Mac) or Docker + WSL 2 (Windows) or Docker on Linux

### Run

```bash
cp .env.example .env
# Edit .env with real secrets
docker compose up --build
```

Open:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

### Secrets

Edit `.env` with your own values:

```env
NANOLINK_DB_PASSWORD=your_secure_db_password_here
NANOLINK_JWT_SECRET=your_256bit_random_secret_here
```

Generate a secure JWT secret:
- Linux/Mac: `openssl rand -base64 32`
- PowerShell: `[Convert]::ToBase64String((1..32|ForEach-Object{[byte](Get-Random -Max 256)})) | Out-String`

## API Overview

Public:
- POST `/api/auth/register`
- POST `/api/auth/login`
- GET `/{shortCode}`

Protected:
- POST `/api/links`
- GET `/api/links`
- GET `/api/links/{id}`
- PATCH `/api/links/{id}`
- DELETE `/api/links/{id}`
- GET `/api/links/{id}/qr`
- GET `/api/links/{id}/analytics`

## Rate Limiting + CORS

- Auth endpoints: 10 requests/min per IP
- Other endpoints: 100 requests/min per IP
- CORS allows local dev frontends: 3000, 5173, 4200

## Local Development (Without Docker)

Requirements: Java 25, Maven 3.9+, PostgreSQL 18

```bash
mvn clean package
java -jar target/nanolink-0.0.1-SNAPSHOT.jar
```

Run tests:

```bash
mvn test
```

## Tech Stack

| Component | Version |
|---|---|
| Java | 25 |
| Spring Boot | 4.0.6 |
| Spring Security | 7.0.7 |
| PostgreSQL | 18.3 |
| Flyway | 11.14.1 |
| JWT (jjwt) | 0.12.6 |
| ZXing | 3.5.3 |
| Springdoc | 2.7.0 |

## Troubleshooting

- Missing `.env`: run `cp .env.example .env`
- Postgres not ready: wait for healthcheck in `docker compose up` logs
- Port conflicts: change `docker-compose.yml` ports

## License

MIT
