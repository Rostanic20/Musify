# Musify Backend

REST API backend for the Musify music streaming platform. Built with Kotlin and Ktor, following clean architecture principles with domain-driven design.

## Tech Stack

- **Language:** Kotlin 1.9
- **Framework:** Ktor 2.3 (Netty engine)
- **Database:** PostgreSQL (Exposed ORM, Flyway migrations, HikariCP connection pool)
- **Caching:** Redis (Jedis client) with in-memory fallback (cache4k)
- **Storage:** AWS S3, local filesystem (pluggable via StorageService interface)
- **Auth:** JWT (access + refresh tokens), OAuth2 with PKCE, 2FA (TOTP)
- **Payments:** Stripe (subscriptions, webhooks)
- **Monitoring:** Sentry (error tracking), Micrometer + CloudWatch (metrics), Prometheus
- **DI:** Koin
- **Audio:** FFmpeg transcoding, HLS/DASH adaptive streaming

## Architecture

```
src/main/kotlin/com/musify/
  Application.kt              # Server entry point and plugin configuration
  core/                        # Cross-cutting concerns (config, media, monitoring, storage, tasks)
  data/repository/             # Repository implementations (Exposed queries)
  database/                    # Schema definitions, migrations, backup
  di/                          # Koin dependency injection modules
  domain/
    entities/                  # Domain models
    repository/                # Repository interfaces
    services/                  # Business logic (recommendations, search, offline, social)
    usecase/                   # Use cases (auth, songs, search, playlists, etc.)
  infrastructure/              # Cache, email, logging, middleware
  plugins/                     # Rate limiting, security headers
  presentation/controller/     # HTTP route handlers
  routes/                      # Legacy route definitions
  security/                    # JWT configuration
```

## Prerequisites

- JDK 17+
- PostgreSQL 14+ (or use the default in-memory H2 for development)
- Redis (optional, for caching)
- FFmpeg (optional, for audio transcoding)

## Getting Started

1. Clone the repository and copy the environment file:
   ```bash
   cp .env.example .env
   ```

2. Configure your `.env` with database credentials and any required secrets.

3. Run from your IDE (IntelliJ / Android Studio) or via Gradle:
   ```bash
   ./gradlew run
   ```

4. The server starts on `http://localhost:8080` by default.

## Environment Variables

Key configuration (see `EnvironmentConfig.kt` for full list):

| Variable | Default | Description |
|---|---|---|
| `ENVIRONMENT` | `development` | `development`, `staging`, `production` |
| `SERVER_PORT` | `8080` | HTTP server port |
| `DATABASE_URL` | `jdbc:h2:mem:test` | JDBC connection string |
| `JWT_SECRET` | dev default | Token signing secret (required in production) |
| `REDIS_ENABLED` | `false` | Enable Redis caching |
| `STORAGE_TYPE` | `local` | `local`, `s3` |
| `STRIPE_API_KEY` | - | Stripe secret key for payments |

## API Documentation

See the [`docs/`](docs/) directory for endpoint documentation:

- [Search API](docs/SEARCH_API.md)
- [Interaction API](docs/API_INTERACTION_CONTROLLER.md)

## Testing

```bash
./gradlew test
```

Tests use an in-memory H2 database and mocked external services.

## Deployment

Build a fat JAR for deployment:
```bash
./gradlew buildFatJar
java -jar build/libs/musify-backend-fat.jar
```

An `nginx.conf` is included for reverse proxy configuration.
