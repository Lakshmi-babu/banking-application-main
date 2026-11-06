# Spring Boot Banking Application

A backend banking application built using Java, Spring Boot, Spring Data JPA,
MySQL, Apache Kafka, Redis, JWT authentication, and Docker.

## Project Overview

This application provides REST APIs for banking operations, including
customer account management, deposits, withdrawals, fund transfers,
transaction history, and administrative monitoring.

It uses a microservices-oriented architecture with asynchronous event
processing through Apache Kafka.

## Technology Stack

- Java 21
- Spring Boot 4
- Spring MVC and Spring Data JPA
- Spring Security and JWT
- MySQL 8
- Apache Kafka
- Redis
- Maven
- Docker
- Jenkins
- Git
- JUnit
- Postman
- Linux

## Key Features

### Banking Operations
- Customer and account management
- Deposit and withdrawal operations
- Account-to-account fund transfers
- Transaction history and pagination
- Transaction filtering
- Request validation and exception handling

### Security
- JWT-based authentication
- ADMIN and CUSTOMER roles
- Role-based API authorization
- Protected administrative endpoints

### Reliability
- Transactional database operations
- Idempotency for supported financial operations
- Database row locking
- Transactional Outbox Pattern
- Kafka event publishing
- Dead Letter Topic monitoring and retry support

### Monitoring and Administration
- Audit log management
- Outbox event monitoring
- Kafka retry monitoring
- Dead Letter Topic health and message monitoring
- Application health, readiness, and liveness endpoints

### Infrastructure
- MySQL persistence
- Redis integration
- Kafka messaging
- Docker containerization
- Service discovery and API Gateway integration

## Architecture

The application uses the following supporting components:

- Banking Service — banking business operations and REST APIs
- Auth Service — authentication
- API Gateway — API routing
- Eureka Server — service discovery
- Notification Service — Kafka event consumption and notifications
- Kafka — asynchronous event messaging
- Redis — caching support
- MySQL — persistent application data

## Prerequisites

- Java 21
- Maven (or the included Maven wrapper)
- MySQL 8
- Apache Kafka
- Redis
- Docker (for containerized deployment)

## Build

Clone the repository and navigate to the project directory.

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd banking-application-main
./mvnw clean package
./mvnw test
java -jar target/bankingApplication-0.0.1-SNAPSHOT.jar
docker build -t banking-application:latest .

```

## Health Checks

GET /actuator/health

GET /actuator/health/readiness

GET /actuator/health/liveness

## Administrative APIs

These endpoints require ADMIN authorization:

- GET /api/audit-logs
- GET /api/admin/kafka/dlt/health
- GET /api/admin/kafka/dlt/summary
- GET /api/admin/kafka/dlt/messages
- POST /api/admin/kafka/dlt/retry

## Security Notes

- Configure database credentials and JWT secrets through environment variables or a secure secrets manager.
- Never commit passwords, JWT tokens, or other credentials.
- Use HTTPS and appropriate access controls in production.

## Author

Lakshmi Babu

Java Backend Developer | Spring Boot | Microservices
