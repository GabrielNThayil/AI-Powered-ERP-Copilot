# AI-Powered ERP Copilot

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Database Design](#database-design)
- [API Documentation](#api-documentation)
- [Setup Instructions](#setup-instructions)
- [Docker Instructions](#docker-instructions)
- [Testing](#testing)
- [Screenshots](#screenshots)
- [Resume Bullet Points](#resume-bullet-points)
- [License](#license)

## Overview
The AI-Powered ERP Copilot is a comprehensive Enterprise Resource Planning (ERP) system enhanced with artificial intelligence capabilities. Built with modern Java technologies (Java 21, Spring Boot 3), this system provides core ERP functionalities including employee management, department management, leave and expense processing, along with an AI-powered copilot that enables natural language querying of ERP data.

## Features
### Core ERP Modules
- **User Management**: Registration, login, JWT authentication, role-based access control (ADMIN, MANAGER, EMPLOYEE)
- **Employee Management**: Create, read, update, delete employees; assign to departments; search and filter
- **Department Management**: Create, read, update, delete departments; assign managers; view department budgets
- **Leave Management**: Employees can submit leave requests; managers can approve/reject; employees can cancel pending requests
- **Expense Management**: Employees can submit expense claims; managers can approve/reject; track expenses by category and date
- **Audit Logging**: Comprehensive logging of all create, update, delete operations for compliance and tracking
- **Analytics Dashboard**: Key metrics and trends for informed decision-making
- **AI Copilot**: Natural language interface powered by Gemini AI for querying ERP data

### AI Copilot Capabilities
The AI Copilot can answer questions such as:
- "Which department spent the most this month?"
- "How many pending leave requests are there?"
- "What was the total expense this month?"
- "How many new employees joined this month?"
- "Show the employee count per department"
- "What is the total number of employees?"
- And many more...

### Security
- Role-Based Access Control (RBAC) with three roles: ADMIN, MANAGER, EMPLOYEE
- JWT-based authentication with token expiration
- Password encoding using BCrypt
- Stateless authentication suitable for REST APIs
- Input validation and sanitization
- CORS configuration for frontend integration

## Technology Stack
- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.2.0
- **ORM**: Spring Data JPA (Hibernate)
- **Database**: PostgreSQL 15+
- **Database Migrations**: Flyway
- **Security**: Spring Security 6, JWT, BCrypt
- **API Documentation**: Springdoc OpenAPI (Swagger UI)
- **Validation**: Jakarta Validation
- **Logging**: SLF4J with Logback
- **Mapping**: MapStruct (DTO-entity mapping)
- **Caching**: Caffeine (for analytics endpoints)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito
- **AI Integration**: Google Gemini API
- **Containerization**: Docker, Docker Compose
- **Development Tools**: Lombok (reduces boilerplate)

## Architecture
The system follows a clean layered architecture:
1. **Controller Layer**: REST endpoints handling HTTP requests and responses
2. **Service Layer**: Business logic, transaction management, audit logging
3. **Repository Layer**: Data access using Spring Data JPA with custom queries for analytics
4. **DTO Layer**: Data Transfer Objects separating internal entities from API contracts
5. **Entity Layer**: JPA entities representing database tables
6. **Security Layer**: JWT authentication, authorization, password encoding
7. **Configuration Layer**: Application configuration, caching, security settings

See [Architecture Documentation](docs/architecture.md) for detailed information.

## Database Design
The database consists of seven tables with proper relationships:
- **users**: Authentication and authorization information
- **employees**: Employee records linked to users
- **departments**: Organizational departments with budgets and managers
- **leave_requests**: Employee leave requests with approval workflow
- **expenses**: Employee expense claims with approval workflow
- **audit_logs**: Comprehensive audit trail of all operations
- A complete [Entity Relationship Diagram (ERD)](docs/erd.md) is available.

## API Documentation
The system provides a comprehensive REST API with Swagger/OpenAPI 3.0 documentation.

### Accessing API Documentation
Once the application is running:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### API Categories
- **Authentication**: `/api/v1/auth/*` (login, register, refresh token)
- **Employee Management**: `/api/v1/employees/*`
- **Department Management**: `/api/v1/departments/*`
- **Leave Management**: `/api/v1/leave-requests/*`
- **Expense Management**: `/api/v1/expenses/*`
- **Dashboard & Analytics**: `/api/v1/dashboard/*`
- **AI Copilot**: `/api/v1/copilot/*`
- **Audit Logs**: `/api/v1/audit-logs/*` (ADMIN only)

See [API Endpoints Documentation](docs/api-endpoints.md) for detailed endpoint information.

## Setup Instructions
### Prerequisites
- Java 21 JDK
- Maven 3.8+
- PostgreSQL 15+
- (Optional) Docker and Docker Compose for containerized deployment

### Local Development Setup
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd erp-copilot
   ```

2. **Configure database connection**
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/erp_copilot
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

3. **Install required extensions**
   - Ensure the `uuid-ossp` extension is available in PostgreSQL (handled by Flyway migration)

4. **Run Flyway migrations**
   ```bash
   mvn flyway:migrate
   ```

5. **Start the application**
   ```bash
   mvn spring-boot:run
   ```

6. **Access the application**
   - API: http://localhost:8080/api/v1/*
   - Swagger UI: http://localhost:8080/swagger-ui.html

### Environment Variables
The application supports environment variables for configuration:
- `DATABASE_URL`: JDBC URL for PostgreSQL
- `DATABASE_USERNAME`: PostgreSQL username
- `DATABASE_PASSWORD`: PostgreSQL password
- `JWT_SECRET`: Secret key for JWT token signing (change in production!)
- `GEMINI_API_KEY`: API key for Google Gemini AI
- `ALLOWED_ORIGINS`: Comma-separated list of allowed CORS origins (default: http://localhost:3000)

## Docker Instructions
### Prerequisites
- Docker
- Docker Compose

### Running with Docker Compose
1. **Build and start the containers**
   ```bash
   docker-compose up --build
   ```

2. **Access the application**
   - API: http://localhost:8080/api/v1/*
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - PostgreSQL: Available on localhost:5432 (if needed for direct access)

3. **Stop the containers**
   ```bash
   docker-compose down
   ```

### Docker Compose Configuration
The `docker-compose.yml` file defines two services:
- **erp-copilot**: The Spring Boot application
- **postgres**: PostgreSQL 15 database

Environment variables are passed from the `.env` file or can be overridden in the compose file.

## Testing
The project includes comprehensive unit and integration tests:

### Running Tests
```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -DskipIntegrationTests

# Run only integration tests
mvn verify -DskipUnitTests
```

### Test Coverage
- **Service Layer**: Tests for EmployeeService, DepartmentService, LeaveRequestService, ExpenseService, DashboardService, AiCopilotService
- **Repository Layer**: Tests for custom queries in EmployeeRepository and ExpenseRepository
- **Controller Layer**: Tests for EmployeeController (representative example)
- **Test Frameworks**: JUnit 5, Mockito, Spring Test (MockMvc, DataJpaTest, WebMvcTest)

See the `src/test/java` directory for all test classes.

## Screenshots
*(Placeholders for actual screenshots)*

1. **Login Page**
   ![Login Page](docs/screenshots/login.png)

2. **Dashboard Overview**
   ![Dashboard](docs/screenshots/dashboard.png)

3. **Employee Management**
   ![Employee Management](docs/screenshots/employees.png)

4. **AI Copilot Interface**
   ![AI Copilot](docs/screenshots/aicopilot.png)

*(Note: Actual screenshots would be added in a real implementation)*

## Resume Bullet Points
- **Full-Stack Development**: Designed and implemented a full-featured ERP system using Java 21, Spring Boot 3, and PostgreSQL
- **API Development**: Created RESTful APIs with Spring Boot, Spring Data JPA, and Springdoc OpenAPI (Swagger)
- **Authentication & Authorization**: Implemented JWT-based authentication and role-based access control (RBAC) with Spring Security 6
- **Database Design**: Designed normalized database schema with proper relationships, constraints, and indexes; managed migrations with Flyway
- **Business Logic**: Implemented complex business rules for employee management, leave requests, expense claims, and approval workflows
- **Analytics**: Developed dashboard with key performance indicators and trends using caching for performance
- **AI Integration**: Integrated Google Gemini API for natural language querying of ERP data (AI Copilot feature)
- **Testing**: Wrote comprehensive unit and integration tests using JUnit 5 and Mockito achieving high test coverage
- **DevOps**: Containerized application using Docker and Docker Compose for easy deployment
- **Code Quality**: Applied clean code principles, SOLID design, and layered architecture for maintainability
- **Documentation**: Created comprehensive technical documentation including architecture, ERD, API specifications, and setup instructions

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments
- Spring Boot team for the excellent framework
- The open-source community for various libraries used
- Google for the Gemini API
- PostgreSQL team for the robust database system