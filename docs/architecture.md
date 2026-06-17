# AI-Powered ERP Copilot - Architecture Document

## Overview
This document outlines the architecture of the AI-Powered ERP Copilot built with Java 21, Spring Boot 3, and PostgreSQL. The system follows a clean layered architecture to ensure maintainability, scalability, and separation of concerns.

## Architectural Layers

### 1. Controller Layer
- **Responsibility**: Handles HTTP requests and responses.
- **Components**: REST controllers annotated with `@RestController`.
- **Features**: 
  - Input validation using `@Valid`.
  - Exception handling via `@ExceptionHandler`.
  - Role-based access control using Spring Security.
  - Pagination and sorting support.

### 2. Service Layer
- **Responsibility**: Contains business logic.
- **Components**: Service interfaces and implementations annotated with `@Service`.
- **Features**:
  - Transaction management using `@Transactional`.
  - Dependency injection via constructor injection.
  - Business rules validation.
  - Integration with repositories and external services (e.g., Gemini API).

### 3. Repository Layer
- **Responsibility**: Data access layer.
- **Components**: Spring Data JPA repositories extending `JpaRepository`.
- **Features**:
  - CRUD operations.
  - Custom queries using `@Query`.
  - Pagination and sorting support.
  - Native SQL queries for complex analytics.

### 4. DTO (Data Transfer Object) Layer
- **Responsibility**: Transfer data between layers and external clients.
- **Components**: Plain Java classes (with Lombok) in the `dto` package.
- **Features**:
  - Separation from entities to prevent overexposure.
  - Validation using Bean Validation annotations.
  - Mapping to/from entities using MapStruct.

### 5. Entity Layer
- **Responsibility**: Represents database tables.
- **Components**: JPA entities annotated with `@Entity`.
- **Features**:
  - Proper JPA annotations (`@Id`, `@Column`, `@ManyToOne`, etc.).
  - Audit fields (`createdAt`, `updatedAt`).
  - Constraints and indexes.

### 6. Security Layer
- **Responsibility**: Authentication and authorization.
- **Components**: Spring Security configuration, JWT utilities, filters.
- **Features**:
  - JWT-based authentication.
  - Role-Based Access Control (RBAC) with roles: ADMIN, MANAGER, EMPLOYEE.
  - Password encoding using BCrypt.
  - Stateless authentication suitable for REST APIs.

### 7. Configuration Layer
- **Responsibility**: Application configuration.
- **Components**: Java-based configuration classes.
- **Features**:
  - Database connection settings.
  - Spring Cache configuration.
  - Swagger/OpenAPI configuration.
  - CORS configuration.
  - Actuator endpoints configuration.

### 8. Exception Handling
- **Responsibility**: Centralized error handling.
- **Components**: `@ControllerAdvice` and custom exception classes.
- **Features**:
  - Global exception handling for REST APIs.
  - Consistent error response format.
  - Logging of exceptions.

### 9. Utilities
- **Responsibility**: Helper classes and common functions.
- **Components**: Utility classes in the `util` package.
- **Features**:
  - Common constants.
  - Helper methods for validation, conversion, etc.

## Key Technologies

- **Java 21**: LTS version with modern features.
- **Spring Boot 3**: Framework for building microservices.
- **Spring Data JPA**: ORM for database operations.
- **PostgreSQL**: Robust open-source relational database.
- **Flyway**: Database migration tool.
- **Lombok**: Reduces boilerplate code.
- **MapStruct**: Object mapping library.
- **Spring Security 6**: Authentication and authorization.
- **JWT**: Token-based authentication.
- **BCrypt**: Password hashing.
- **Spring Cache**: Caching abstraction (using Caffeine or Redis).
- **Swagger/OpenAPI 3**: API documentation.
- **Spring Boot Actuator**: Production-ready features.
- **JUnit 5 & Mockito**: Testing framework.
- **Gemini API**: AI integration for the Copilot feature.

## Design Principles

- **SOLID**: Single responsibility, Open-closed, Liskov substitution, Interface segregation, Dependency inversion.
- **DRY**: Don't Repeat Yourself.
- **KISS**: Keep It Simple, Stupid.
- **YAGNI**: You Aren't Gonna Need It.
- **Clean Code**: Readable, maintainable, and testable code.

## Communication Flow

1. Client sends HTTP request to Controller.
2. Controller validates input and delegates to Service.
3. Service executes business logic, uses Repository for data access.
4. Repository interacts with database via JPA.
5. Response flows back: Repository -> Service -> Controller -> Client.
6. Cross-cutting concerns (logging, security, transactions) handled via AOP.

## Database Design
- Normalized to 3NF.
- Proper indexing for query performance.
- Foreign key constraints for data integrity.
- Audit trails for critical entities.
- Flyway for version-controlled migrations.

## API Design
- RESTful principles.
- Consistent naming and versioning (/api/v1/*).
- Standard HTTP status codes.
- JSON request/response format.
- Pagination using Spring Data Page.
- Filtering and sorting capabilities.
- Swagger UI for interactive documentation.

## Security Considerations
- Stateless JWT authentication.
- Passwords hashed with BCrypt.
- Role-based access control at method level.
- Input validation to prevent injection attacks.
- CORS configuration for frontend integration.
- Secure headers (to be configured in production).

## Scalability & Performance
- Horizontal scaling potential (stateless services).
- Caching for frequently accessed data (analytics).
- Database connection pooling.
- Asynchronous processing for long-running tasks (to be implemented).
- Pagination to handle large datasets.

## Monitoring & Observability
- Spring Boot Actuator for health checks and metrics.
- Logging with SLF4J and Logback.
- Distributed tracing (to be integrated in future).
- Audit logging for critical operations.

## Deployment
- Docker containerization.
- Docker Compose for local development.
- Environment-specific configuration (dev/prod).
- CI/CD pipeline ready (to be implemented).

## Future Enhancements
- Redis for caching and session store.
- Message queue (RabbitMQ/Kafka) for decoupling.
- Elasticsearch for advanced search.
- Microservices architecture for larger scale.
- Advanced AI features (predictive analytics, anomaly detection).