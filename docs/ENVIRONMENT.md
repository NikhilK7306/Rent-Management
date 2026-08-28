# Development Environment Report

## Operating System
- **OS**: Windows 10 Home (Version 2009)
- **Architecture**: 64-bit (x64)
- **CPU**: AMD Ryzen 3 3200G with Radeon Vega Graphics

## Hardware Resources
- **RAM**: ~6 GB
- **Disk Space (C:)**: ~254 GB free / ~54 GB used

## Java / JVM
- **Java Version**: 21.0.12 (Eclipse Adoptium Temurin)
- **JAVA_HOME**: `C:\Program Files\Eclipse Adoptium\jdk-21.0.12.8-hotspot`
- **javac**: Available (bundled with JDK)

## Build Tools
- **Maven**: 3.9.9
- **Gradle**: Not installed (not required)

## Node.js / Frontend
- **Node.js**: 20.19.0 (LTS)
- **npm**: 10.8.2
- **npx**: 10.8.2
- **Angular CLI**: 18.2.21
- **Angular Framework**: 18.x (aligned with CLI)
- **TypeScript**: ~5.4.x (Angular 18 default)
- **RxJS**: ~7.8.x (Angular 18 default)

## Docker
- **Docker**: 29.7.2
- **Docker Compose**: v5.4.0 (compose plugin)

## Database
- **PostgreSQL Client (psql)**: Not installed locally (will run via Docker)

## Version Compatibility Matrix

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 21 | LTS, compatible with Spring Boot 3.x |
| Maven | 3.9.9 | Compatible with Java 21 |
| Node.js | 20.19.0 | LTS (Iron), compatible with Angular 18 |
| npm | 10.8.2 | Bundled with Node 20 |
| Angular CLI | 18.2.21 | Stable |
| Angular | 18.x | Active support |
| TypeScript | ~5.4.x | Angular 18 requirement |
| RxJS | ~7.8.x | Angular 18 requirement |
| Docker | 29.7.2 | Current stable |
| Docker Compose | v5.4.0 | Compose v2 plugin |
| PostgreSQL | 16.x | Via Docker image |

## Selected Versions for Project

```properties
# Backend
java.version=21
spring-boot.version=3.3.2
spring-security.version=6.3.3
spring-data-jpa.version=3.3.2
liquibase.version=4.29.0
postgresql.version=42.7.3
jjwt.version=0.12.5

# Frontend
angular.version=18.2.x
typescript.version=5.4.x
rxjs.version=7.8.x
node.version=20.19.0

# Database
postgresql.version=16-alpine

# Infrastructure
docker.compose.version=2.29.x (v5.4.0 plugin)
```

## Required Ports

| Service | Port | Protocol |
|---------|------|----------|
| Frontend (Angular Dev Server) | 4200 | HTTP |
| Backend (Spring Boot) | 8080 | HTTP |
| PostgreSQL | 5432 | TCP |

## Required Environment Variables

### Backend (Spring Boot)
```bash
# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=rentdb
DB_USERNAME=rentuser
DB_PASSWORD=rentpass

# JWT
JWT_SECRET=dev-secret-key-min-256-bits-for-hmac-sha256
JWT_EXPIRATION=86400000

# Server
SERVER_PORT=8080

# Liquibase
LIQUIBASE_CHANGELOG=classpath:db/changelog/db.changelog-master.yaml
```

### Frontend (Angular)
```bash
# API Base URL
API_BASE_URL=http://localhost:8080/api
```

### Docker Compose
```bash
# Database (also used by backend)
POSTGRES_DB=rentdb
POSTGRES_USER=rentuser
POSTGRES_PASSWORD=rentpass
POSTGRES_PORT=5432

# Backend
JWT_SECRET=dev-secret-key-min-256-bits-for-hmac-sha256
JWT_EXPIRATION=86400000

# Frontend
NG_API_BASE_URL=http://localhost:8080/api
```

## Development Commands

### Backend
```bash
# Build and run tests
cd backend
mvn clean test
mvn clean package

# Run locally (requires PostgreSQL running)
mvn spring-boot:run
```

### Frontend
```bash
# Clean install and build
cd frontend
npm ci
npm run build

# Run dev server
npm start

# Run tests
npm test -- --watch=false --browsers=ChromeHeadless
```

### Docker
```bash
# Build and start all services
docker compose up --build

# Start in background
docker compose up -d --build

# View logs
docker compose logs -f

# Stop and remove
docker compose down

# Stop and remove with volumes (database data)
docker compose down -v
```

## .nvmrc (for Node version management)
```
20.19.0
```

## Notes
- PostgreSQL will run exclusively in Docker for development
- No local PostgreSQL installation required
- All database changes managed via Liquibase migrations
- JWT secret must be at least 256 bits (32 chars) for HS256
- Development admin user seeded via Liquibase migration