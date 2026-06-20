# Spring Boot Auth Module — JWT + Spring Security + PostgreSQL

A production-ready Authentication & Authorization module built with:

- **Spring Boot 3.2** + **Spring Security 6**
- **JWT** (JJWT 0.12) for stateless access tokens
- **Opaque Refresh Tokens** stored in PostgreSQL for revocability
- **BCrypt** (strength 12) for password hashing
- **Role-Based Access Control** via `@PreAuthorize`
- **PostgreSQL** for data persistence
- **Bean Validation** for all request payloads
- **Global Exception Handler** for consistent error responses

---

## Architecture Overview

```
Client
  │
  ▼
┌─────────────────────────────────────────┐
│          AuthController                  │  /auth/*  (public)
│          TestController                  │  /api/test/* (protected)
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│          AuthService                     │
│  ┌──────────────────────────────────┐   │
│  │  Registration  │  Login  │ Refresh│  │
│  └──────────────────────────────────┘   │
└──┬──────────────────────┬───────────────┘
   │                      │
   ▼                      ▼
JwtService          RefreshTokenService
(access tokens)     (opaque UUID tokens)
   │                      │
   ▼                      ▼
           PostgreSQL
        ┌──────────────────┐
        │  users           │
        │  user_roles      │
        │  refresh_tokens  │
        └──────────────────┘

Every request passes through:
  JwtAuthenticationFilter → SecurityFilterChain → Controller
```

---

## Security Design Decisions

| Decision | Choice | Reason |
|---|---|---|
| Session policy | **STATELESS** | JWT is self-contained; no server-side session needed |
| CSRF | **Disabled** | APIs authenticate via Authorization header, not cookies |
| Password hashing | **BCrypt strength 12** | Resistant to brute-force; ~250ms per hash |
| Access token lifetime | **15 minutes** | Short window limits damage from token theft |
| Refresh token type | **Opaque UUID (DB-backed)** | Can be revoked server-side; JWTs cannot |
| Refresh token rotation | **On every login** | Old token invalidated; reduces replay risk |
| Role storage | **DB enum set (EAGER)** | Loaded with user for every request |

---

## Quick Start

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

### 2. Configure the application

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/auth_db
spring.datasource.username=postgres
spring.datasource.password=yourpassword

# Generate a secure key: openssl rand -hex 32
application.security.jwt.secret-key=YOUR_64_CHAR_HEX_KEY
```

> **Production tip:** Move secrets to environment variables or a secrets manager.
> Never commit real credentials to version control.

### 3. Run the application

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

---

## API Reference

### POST /auth/register

Creates a new user account and returns tokens.

**Request:**
```json
{
  "fullName": "Alice Smith",
  "email": "alice@example.com",
  "password": "SecureP@ss1"
}
```

**Response 201:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "a1b2c3d4-e5f6-...",
    "tokenType": "Bearer",
    "expiresIn": 900000,
    "email": "alice@example.com",
    "roles": ["ROLE_USER"]
  }
}
```

---

### POST /auth/login

Authenticates credentials and returns a token pair.

**Request:**
```json
{
  "email": "alice@example.com",
  "password": "SecureP@ss1"
}
```

**Response 200:** Same shape as `/auth/register`.

**Error 401:** Invalid email or password.

---

### POST /auth/refresh

Exchanges a valid refresh token for a new access token.

**Request:**
```json
{
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**Response 200:**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...(new token)...",
    "refreshToken": "a1b2c3d4-...(same token)...",
    ...
  }
}
```

**Error 403:** Token expired or not found.

---

## RBAC Test Endpoints

All require `Authorization: Bearer <access_token>`.

| Endpoint | Allowed Roles | Description |
|---|---|---|
| `GET /api/test/user` | USER, MODERATOR, ADMIN | Basic auth smoke test |
| `GET /api/test/moderator` | MODERATOR, ADMIN | Moderator-restricted content |
| `GET /api/test/admin` | ADMIN only | Admin-only endpoint |
| `GET /api/test/me` | Any authenticated | Returns current user info |

**Example curl:**
```bash
# 1. Login and capture the token
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"SecureP@ss1"}' \
  | jq -r '.data.accessToken')

# 2. Call a protected endpoint
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/test/me
```

---

## Available Roles

| Role | Description |
|---|---|
| `ROLE_USER` | Default role for all registered users |
| `ROLE_MODERATOR` | Elevated access for content moderation |
| `ROLE_ADMIN` | Full system access |

> To assign `ROLE_ADMIN`, update the user's roles directly in the database
> or build a separate admin-only promotion endpoint.

---

## Database Schema

Hibernate auto-creates these tables on startup (`ddl-auto=update`):

```sql
-- Users table
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(100)  NOT NULL,
    email       VARCHAR(150)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    enabled     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP
);

-- Roles join table (one user → many roles)
CREATE TABLE user_roles (
    user_id BIGINT      NOT NULL REFERENCES users(id),
    role    VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Refresh tokens (one active token per user)
CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL UNIQUE REFERENCES users(id),
    token       VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP    NOT NULL
);
```

---

## Running Tests

```bash
./mvnw test
```

Tests use an active PostgreSQL connection. For isolated testing, add
[Testcontainers](https://testcontainers.com/) to spin up a dedicated container per test run.

---

## Production Checklist

- [ ] Replace the JWT secret with a cryptographically random 256-bit key
- [ ] Store secrets in environment variables or a vault (not `application.properties`)
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` (use Flyway/Liquibase for migrations)
- [ ] Enable HTTPS / TLS termination at the load balancer
- [ ] Add rate limiting to `/auth/login` and `/auth/register`
- [ ] Implement logout endpoint that deletes the refresh token
- [ ] Consider adding an admin endpoint to promote users to `ROLE_ADMIN`
- [ ] Set up structured logging and a SIEM integration
