# Insurance Claim System

A comprehensive insurance claim management demo system with microservices architecture.

## Architecture

```
insurance-demo/
├── claim-api/           # Claim Service (Port 8082)
├── policy-service/      # Policy Service (Port 8083)
├── notification-service/ # Notification Service (Port 8084)
├── react-web/           # React Web Application (Port 3000)
├── react-native-mobile/ # React Native Mobile App
└── docker-compose.yml   # Docker Compose Configuration
```

## Tech Stack

### Backend
- Java 21
- Spring Boot 3.2.x
- Spring Security + JWT Authentication
- Spring Data JPA
- PostgreSQL
- Maven
- OpenAPI/Swagger
- OpenFeign (Microservices Communication)

### Frontend (Web)
- React 18
- TypeScript
- React Router v6
- Axios
- Material UI v5
- Recharts

### Mobile
- React Native
- TypeScript
- React Navigation
- AsyncStorage

## Database

PostgreSQL with tables: users, roles, claims, policies, claim_attachments, notifications.

## Getting Started

### Prerequisites
- Java 21+
- Maven
- Docker & Docker Compose
- Node.js 18+

### Quick Start with Docker

```bash
cd insurance-demo
docker-compose up -d
```

### Running Locally

1. **Start PostgreSQL**
```bash
docker run -d -p 5432:5432 -e POSTGRES_DB=insurance_claim_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:16
```

2. **Run Backend Services**
```bash
cd claim-api && mvn spring-boot:run
cd policy-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

3. **Run React Web**
```bash
cd react-web && npm install && npm run dev
```

## API Documentation

Swagger UI available at:
- Claim API: http://localhost:8082/swagger-ui.html
- Policy Service: http://localhost:8083/swagger-ui.html
- Notification Service: http://localhost:8084/swagger-ui.html

## Demo Accounts

| Username | Password | Role |
|----------|----------|------|
| john.doe | password123 | CUSTOMER |
| mike.johnson | password123 | ADJUSTER |
| admin | password123 | ADMIN |

## API Endpoints

### Authentication
- `POST /auth/login` - User login

### Claims
- `POST /claims` - Submit new claim
- `GET /claims/my` - Get user's claims
- `GET /claims/{id}` - Get claim details
- `PUT /claims/{id}/status` - Update claim status

### Dashboard
- `GET /dashboard/stats` - Get statistics

## Features

- JWT Authentication with Role-Based Access Control
- Claim submission and tracking
- Policy validation via microservice call
- Email notification simulation
- Dashboard with statistics chart
- Responsive React Web UI
- React Native Mobile App

##Integration test
cd c:\Users\85007\claim\insurance-demo\claim-api
mvn test -Dtest=ClaimControllerIntegrationTest -Dspring.profiles.active=test
