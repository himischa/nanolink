# Nanolink - URL Shortener API

A full-featured URL shortener REST API built with **Spring Boot 4.0.6**, **PostgreSQL 18.3**, and **Java 25**.  
Built as a portfolio project showcasing REST API design, JWT authentication, click analytics, QR code generation, and containerized deployment.

## Features

- **User Management**: Registration & JWT-based authentication (7-day tokens)
- **Link Management**: Create, update, delete, and track shortened links
- **Custom Aliases**: Optional custom short codes (alphanumeric + hyphens, max 50 chars)
- **Link Expiry**: Set expiration dates; returns 410 Gone when expired
- **Click Analytics**: Track clicks with referrer, user agent, IP address; aggregate by date
- **QR Code Generation**: Auto-generate QR codes (PNG) for each link
- **Rate Limiting**: Prevents abuse (10 requests/min per IP for auth endpoints)
- **CORS Support**: Configured for frontend integration
- **Swagger/OpenAPI**: Full API documentation at `/swagger-ui.html`

## Quick Start

### Prerequisites

- **Docker Desktop** or **Docker + WSL 2** (any OS)
- Git

### Setup (All Platforms)

1. **Clone & Navigate**
   ```bash
   git clone https://github.com/yourusername/nanolink.git
   cd nanolink
   ```

2. **Create Environment File**
   ```bash
   cp .env.example .env
   ```
   
   Edit `.env` with your own values:
   ```env
   NANOLINK_DB_PASSWORD=your_secure_db_password_here
   NANOLINK_JWT_SECRET=your_256bit_random_secret_here
   ```
   
   > **Tip**: Generate a secure random secret:
   > - Linux/Mac: `openssl rand -base64 32`
   > - PowerShell: `[Convert]::ToBase64String((1..32|ForEach-Object{[byte](Get-Random -Max 256)})) | Out-String`

3. **Start with Docker Compose**
   ```bash
   docker compose up --build
   ```
   
   Expected output:
   ```
   nanolink-postgres  | database system is ready to accept connections
   nanolink-app       | Started Application in X seconds
   ```

4. **Verify Setup**
   - API Swagger Docs: http://localhost:8080/swagger-ui.html
   - Health Check: http://localhost:8080/v3/api-docs

### Docker Setup Details

#### Docker Desktop (Windows/Mac)
- Works out-of-the-box
- `docker compose up` handles everything
- No additional configuration needed

#### Docker + WSL 2 (Windows)
- Ensure WSL 2 backend is enabled in Docker Desktop settings
- Volume path handling is automatic (no Windows path conversion needed)
- Same commands as Docker Desktop

#### Linux
- Standard Docker installation
- Same commands as above

### Testing the API

#### Option 1: Swagger UI (Recommended)
1. Open http://localhost:8080/swagger-ui.html
2. Try **POST /api/auth/register**:
   ```json
   {
     "email": "test@example.com",
     "username": "testuser",
     "password": "SecurePassword123!"
   }
   ```
3. Copy the returned `token` from response
4. Click "Authorize" button (top-right)
5. Paste: `Bearer {token}`
6. Now try other endpoints (create links, get analytics, etc.)

#### Option 2: curl/PowerShell
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","username":"testuser","password":"SecurePassword123!"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"SecurePassword123!"}'

# Create Link (replace TOKEN with returned token)
curl -X POST http://localhost:8080/api/links \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://example.com","customAlias":"mylink"}'
```

## Project Structure

```
nanolink/
├── src/
│   ├── main/
│   │   ├── java/com/nanolink/
│   │   │   ├── controller/          # REST endpoints
│   │   │   ├── service/             # Business logic
│   │   │   ├── entity/              # JPA entities (User, Link, ClickEvent)
│   │   │   ├── repository/          # Data access
│   │   │   ├── dto/                 # Request/Response DTOs
│   │   │   ├── security/            # JWT auth & Spring Security config
│   │   │   └── NanolinkApplication.java
│   │   └── resources/
│   │       ├── application.yml      # Spring Boot config
│   │       └── db/migration/        # Flyway SQL migrations
│   └── test/
│       └── java/com/nanolink/       # Unit tests (12 passing)
├── Dockerfile                        # Multi-stage build
├── docker-compose.yml               # PostgreSQL + App orchestration
├── pom.xml                          # Maven dependencies
└── README.md                        # This file
```

## API Endpoints

### Authentication (Public)
- **POST** `/api/auth/register` - Create account
- **POST** `/api/auth/login` - Login, get JWT token

### Links (Protected)
- **POST** `/api/links` - Create shortened link
- **GET** `/api/links` - List your links
- **GET** `/api/links/{id}` - Get link details
- **PATCH** `/api/links/{id}` - Update link (alias, expiry)
- **DELETE** `/api/links/{id}` - Delete link
- **GET** `/api/links/{id}/qr` - Get QR code PNG

### Analytics (Protected)
- **GET** `/api/links/{id}/analytics` - Click stats (last 30 days)

### Redirects (Public)
- **GET** `/{shortCode}` - Redirect to original URL (tracks clicks)

## Tech Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Java | 25 | Runtime |
| Spring Boot | 4.0.6 | Web framework |
| Spring Security | 7.0.7 | Authentication |
| JPA/Hibernate | 6.4.10 | ORM |
| PostgreSQL | 18.3 | Database |
| Flyway | 11.14.1 | Schema migrations |
| JWT (jjwt) | 0.12.6 | Token auth |
| ZXing | 3.5.3 | QR codes |
| Springdoc | 2.7.0 | Swagger/OpenAPI |

## Development

### Build Locally (Without Docker)
```bash
# Prerequisites: Java 25, Maven 3.9+, PostgreSQL 18 running locally

mvn clean package
java -jar target/nanolink-0.0.1-SNAPSHOT.jar
```

### Run Tests
```bash
mvn test
```

All 12 unit tests should pass:
- AuthServiceTest (4 tests)
- LinkServiceTest (4 tests)
- RedirectControllerTest (4 tests)

### Database Migrations
Migrations run automatically on startup via Flyway:
- `V1__create_users.sql` - User accounts
- `V2__create_links.sql` - Shortened links
- `V3__create_click_events.sql` - Click tracking

## Troubleshooting

### Issue: Docker can't find `.env` file
**Solution**: Ensure `.env` exists in project root:
```bash
cp .env.example .env
```

### Issue: "Connection refused" when connecting to Postgres
**Cause**: Database container not healthy yet  
**Solution**: Wait for healthcheck to pass (watch `docker compose up` output)

### Issue: Port 5432 or 8080 already in use
**Solution**: Stop other services or change docker-compose.yml ports:
```yaml
ports:
  - "5433:5432"  # Change host port from 5432 to 5433
```

### Issue: Swagger UI shows 500 errors
**Cause**: Spring context not fully loaded  
**Solution**: Wait 10-15 seconds after "Started Application" appears in logs

## Deployment

### To Azure
```bash
# Create resource group
az group create -n nanolink-rg -l eastus

# Deploy with docker compose
az container create \
  --resource-group nanolink-rg \
  --name nanolink \
  --image nanolink:latest \
  --environment-variables NANOLINK_DB_PASSWORD=xxx NANOLINK_JWT_SECRET=xxx
```

### To Docker Hub
```bash
docker build -t yourusername/nanolink:1.0 .
docker push yourusername/nanolink:1.0
```

## Contributing

Improvements welcome! Focus areas for future work:
- Analytics query optimization (currently in-memory)
- Custom domain support
- Team/organization accounts
- Link preview generation
- Bulk API operations

## License

MIT
