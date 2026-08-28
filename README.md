# Rent Management System

A multi-phase rent management application built with Angular, Spring Boot, and PostgreSQL.

## Tech Stack

- **Frontend**: Angular 18, TypeScript, RxJS
- **Backend**: Spring Boot 3.3, Java 21, Spring Security, Spring Data JPA
- **Database**: PostgreSQL 16 with Liquibase migrations
- **Infrastructure**: Docker, Docker Compose

## Project Structure

```
rent-management-system/
├── backend/                 # Spring Boot application
│   ├── src/main/java/      # Java source code
│   ├── src/main/resources/ # Config, Liquibase migrations
│   ├── src/test/java/      # Unit/integration tests
│   └── docker/             # Backend Dockerfile
├── frontend/               # Angular application
│   ├── src/               # TypeScript source code
│   └── docker/            # Frontend Dockerfile
├── database/              # Database scripts (if any)
├── docs/                  # Documentation
├── docker-compose.yml     # Multi-service orchestration
├── .env.example          # Environment variable template
└── .gitignore
```

## Quick Start

### Prerequisites
- Docker 24+ and Docker Compose v2+
- Or locally: Java 21, Maven 3.9+, Node.js 20+, npm 10+

### Using Docker (Recommended)

```bash
# Copy environment template
cp .env.example .env

# Build and start all services
docker compose up --build

# Or run in background
docker compose up -d --build
```

Services will be available at:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080/api
- Health Check: http://localhost:8080/api/health
- PostgreSQL: localhost:5432

### Local Development

#### Backend
```bash
cd backend
# Requires PostgreSQL running on localhost:5432 with credentials from .env
mvn spring-boot:run
```

#### Frontend
```bash
cd frontend
npm ci
npm start
```

## Default Credentials (Development Only)

| Field | Value |
|-------|-------|
| Mobile | 9876543210 |
| Password | Admin@123 |

**⚠️ Never use these credentials in production!**

## API Endpoints (Phase 1)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | /api/health | Public | Health check |
| POST | /api/auth/login | Public | Admin login |
| GET | /api/admin/dashboard | Admin | Dashboard data |

## Development Commands

### Backend
```bash
cd backend
mvn clean test          # Run tests
mvn clean package       # Build JAR
mvn spring-boot:run     # Run locally
```

### Frontend
```bash
cd frontend
npm ci                  # Clean install
npm run build           # Production build
npm start               # Dev server
npm test                # Run tests
```

### Docker
```bash
docker compose up --build        # Build and start
docker compose up -d --build     # Background
docker compose logs -f           # Follow logs
docker compose down              # Stop
docker compose down -v           # Stop + remove volumes
```

## Phase 1 Features

- [x] Development environment setup
- [x] Project structure
- [x] Docker Compose orchestration
- [x] PostgreSQL with Liquibase
- [x] Admin authentication (JWT)
- [x] Admin login page
- [x] Admin dashboard shell
- [x] Route guards & HTTP interceptors
- [x] Logout functionality
- [x] Health endpoint

## Upcoming Phases

- Phase 2: Property Management
- Phase 3: Tenant Management
- Phase 4: Rent Generation & Tracking
- Phase 5: Payments (Cash/Online/Partial)
- Phase 6: Reports & Statements
- Phase 7: Notifications & Reminders