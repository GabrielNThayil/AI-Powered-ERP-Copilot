# Phase 1: System Design & Architecture

## 1. Executive Summary

**Project:** AI-Powered ERP Copilot (`com.erp.copilot`)  
**Purpose:** A production-grade ERP platform with an integrated AI copilot for natural language analytics and management assistance. The platform supports employee management, departments, leave/expense workflows, and AI-driven insights.

**Target Scale:** 100+ concurrent employees supported by robust authentication, caching, and optimized querying.

**Architecture Style:** Layered (Controller → Service → Repository → Entity) with clear separation of concerns, SOLID principles, and enterprise-grade security.

---

## 2. Technology Stack

| Layer | Technology | Version | Rationale |
|-------|-----------|---------|-----------|
| Language | Java | 21 (LTS) | Virtual threads, enhanced security, stable |
| Framework | Spring Boot | 3.2.x | Production-grade, auto-configuration, observability |
| Security | Spring Security 6 | 6.x | Latest security model, built-in CSRF, modern `SecurityFilterChain` |
| JWT Library | JJWT | 0.12.x | Compact, widely-adopted, zero-config |
| ORM | Spring Data JPA | 3.2.x | Standard JPA abstraction, pagination, native queries |
| Database | PostgreSQL | 15+ | ACID compliance, JSON support, production standard |
| Migrations | Flyway | 10.x | Version-controlled schema, CI/CD-friendly, repeatable |
| Mapping | MapStruct | 1.5.x | Compile-time generated mappers, zero reflection overhead |
| Validation | Jakarta Bean Validation | 3.x | Annotation-driven, declarative |
| Cache | Spring Cache (Caffeine) | 3.2.x | Fast in-process cachereddit, TTL support |
| Rate Limiting | Bucket4j | 8.x | Token bucket algorithm, per-user granularity |
| API Docs | SpringDoc OpenAPI | 2.4.x | Swagger UI, OpenAPI 3.1 compliance |
| Monitoring | Spring Boot Actuator | 3.2.x | Health, info, metrics out of the box |
| AI Service | Gemini API | v1 | Free-tier compatible, high-quality NLG |
| Build Tool | Maven | 3.9+ | Standard, dependency management, profiles |
| Container | Docker / Docker Compose | 24.x | Multi-stage builds, reproducible environments |

---

## 3. Architectural Layers

### 3.1 Layer Overview

```
┌──────────────────────────────────────────────┐
│     Client (HTTP/Swagger/Browser)           │
├──────────────────────────────────────────────┤
│  CONTROLLER LAYER (REST API)                  │
│  AuthController, EmployeeController, ...      │
├──────────────────────────────────────────────┤
│  DTO LAYER (Request / Response)              │
│  LoginRequest, EmployeeResponse, ...          │
├──────────────────────────────────────────────┤
│  SERVICE LAYER (Business Logic)              │
│  UserService, EmployeeService, ...            │
├──────────────────────────────────────────────┤
│  REPOSITORY LAYER (Data Access)              │
│  UserRepository, EmployeeRepository, ...      │
├──────────────────────────────────────────────┤
│  ENTITY LAYER (JPA Mappings)                 │
│  User, Employee, Department, ...              │
├──────────────────────────────────────────────┤
│  INFRASTRUCTURE LAYER                        │
│  Security, AI, Audit, Cache, Config          │
└──────────────────────────────────────────────┘
```

### 3.2 Layer Responsibilities

#### Controller Layer
- **Responsibility:** Expose REST endpoints, map HTTP requests, return HTTP responses.
- **Rule:** NO business logic. Only delegation to services and DTO conversion.
- **Annotations:** `@RestController`, `@RequestMapping`, `@Valid`, `@AuthenticationPrincipal`

#### DTO Layer
- **Responsibility:** Define exactly what data crosses the API boundary.
- **Types:** `Request` (input) and `Response` (output) DTOs.
- **Validation:** Jakarta `javax.validation.constraints.*` annotations.
- **Separation:** Zero entity exposure—always DTOs

#### Service Layer
- **Responsibility:** Implement business rules, orchestrate repositories, handle transactions.
- **Interface + Impl:** Each service has an interface and an `@Service` implementation.
- **Transactions:** `@Transactional` at the service level.
- **Security:** `@PreAuthorize` or manual permission checks for ownership.

#### Repository Layer
- **Responsibility:** Data access, custom JPQL/native queries, pagination.
- **Type:** `JpaRepository` + custom interfaces with `@Query` methods.
- **Rule:** Only data access—no business logic.

#### Entity Layer
- **Responsibility:** JPA-mapped Java objects reflecting the relational schema.
- **Annotations:** `@Entity`, `@Table`, `@Id`, `@ManyToOne`, `@OneToMany`, etc.
- **Lifecycle:** `@PrePersist`, `@PreUpdate` for `createdAt`, `updatedAt`.

#### Security Layer
- **Responsibility:** Authentication, authorization, JWT handling, password encoding.
- **Components:**
  - `SecurityConfig` — `SecurityFilterChain`
  - `JwtTokenProvider` — generate/validate JWT
  - `JwtAuthenticationFilter` — intercept requests, validate token
  - `UserDetailsServiceImpl` — load user from DB
  - `PasswordEncoderConfig` — BCrypt

#### Configuration Layer
- **Responsibility:** Beans, CORS, cache, OpenAPI, AI, profiles.
- **Files:** `SecurityConfig`, `OpenApiConfig`, `CacheConfig`, `AiConfig`, `WebConfig`

---

## 4. Package Structure

```
com.erp.copilot/
│
├── ErpCopilotApplication.java
│
├── config/                          # Configuration beans & profiles
│   ├── SecurityConfig.java
│   ├── OpenApiConfig.java
│   ├── CacheConfig.java
│   ├── AiConfig.java
│   └── WebConfig.java
│
├── constants/                       # Application constants
│   └── AppConstants.java
│
├── controller/                      # REST Controllers (thin)
│   ├── AuthController.java
│   ├── UserController.java
│   ├── EmployeeController.java
│   ├── DepartmentController.java
│   ├── LeaveController.java
│   ├── ExpenseController.java
│   ├── DashboardController.java
│   ├── CopilotController.java
│   └── AuditLogController.java
│
├── dto/                             # Data Transfer Objects
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── UserUpdateRequest.java
│   │   ├── EmployeeCreateRequest.java
│   │   ├── EmployeeUpdateRequest.java
│   │   ├── DepartmentCreateRequest.java
│   │   ├── DepartmentUpdateRequest.java
│   │   ├── LeaveApplicationRequest.java
│   │   ├── LeaveActionRequest.java
│   │   ├── ExpenseSubmitRequest.java
│   │   ├── ExpenseActionRequest.java
│   │   └── CopilotQueryRequest.java
│   └── response/
│       ├── ApiResponse.java
│       ├── AuthResponse.java
│       ├── UserResponse.java
│       ├── EmployeeResponse.java
│       ├── DepartmentResponse.java
│       ├── LeaveResponse.java
│       ├── ExpenseResponse.java
│       ├── DashboardSummaryResponse.java
│       ├── ChartDataResponse.java
│       ├── CopilotResponse.java
│       ├── AuditLogResponse.java
│       └── PagedResponse.java
│
├── entity/                          # JPA Entities
│   ├── User.java
│   ├── Role.java
│   ├── Employee.java
│   ├── Department.java
│   ├── LeaveRequest.java
│   ├── Expense.java
│   ├── AuditLog.java
│   └── embedded/
│       └── AuditFields.java
│
├── repository/                      # Spring Data JPA Repositories
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── EmployeeRepository.java
│   ├── DepartmentRepository.java
│   ├── LeaveRequestRepository.java
│   ├── ExpenseRepository.java
│   └── AuditLogRepository.java
│
├── service/                         # Service Interfaces
│   ├── AuthService.java
│   ├── UserService.java
│   ├── EmployeeService.java
│   ├── DepartmentService.java
│   ├── LeaveService.java
│   ├── ExpenseService.java
│   ├── DashboardService.java
│   ├── CopilotService.java
│   └── AuditLogService.java
│
├── service/impl/                    # Service Implementations
│   ├── AuthServiceImpl.java
│   ├── UserServiceImpl.java
│   ├── EmployeeServiceImpl.java
│   ├── DepartmentServiceImpl.java
│   ├── LeaveServiceImpl.java
│   ├── ExpenseServiceImpl.java
│   ├── DashboardServiceImpl.java
│   ├── CopilotServiceImpl.java
│   └── AuditLogServiceImpl.java
│
├── security/                        # Security Layer
│   ├── jwt/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── JwtAuthenticationEntryPoint.java
│   ├── service/
│   │   └── UserDetailsServiceImpl.java
│   └── config/
│       └── PasswordEncoderConfig.java
│
├── exception/                       # Global exception handling
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedAccessException.java
│   ├── BadRequestException.java
│   ├── ConflictException.java
│   └── GlobalExceptionHandler.java
│
├── mapper/                          # MapStruct mappers
│   ├── UserMapper.java
│   ├── EmployeeMapper.java
│   ├── DepartmentMapper.java
│   ├── LeaveMapper.java
│   ├── ExpenseMapper.java
│   └── AuditLogMapper.java
│
├── audit/                           # Audit logging (AOP + annotation)
│   ├── annotation/
│   │   └── Auditable.java
│   └── aspect/
│       └── AuditAspect.java
│
├── ai/                              # AI Copilot integration
│   ├── client/
│   │   └── GeminiClient.java
│   ├── prompt/
│   │   ├── PromptTemplate.java
│   │   └── PromptBuilder.java
│   └── service/
│       └── QueryContextService.java
│
├── cache/
│   └── CacheKeyGenerator.java
│
└── util/
    ├── DateUtil.java
    └── PaginationUtil.java
```

---

## 5. Security Architecture

### 5.1 Authentication Flow

```
Client                                          Server
│                                                │
│ POST /api/v1/auth/login                        │
│ {email, password}                               │
│─────────────────────────────────────────────>│
│                                                │
│              AuthenticationManager             │
│              validates credentials             │
│                                                │
│<─────────────────────────────────────────────│
│  200 OK {accessToken, refreshToken, type}     │
│                                                │
│                                              │
│ GET /api/v1/employees                          │
│ Authorization: Bearer <accessToken>           │
│─────────────────────────────────────────────>│
│              JwtAuthenticationFilter           │
│              validates & sets auth            │
│                                                │
│<─────────────────────────────────────────────│
│  200 OK [employee data]                       │
```

### 5.2 JWT Design

| Claim | Type | Description |
|-------|------|-------------|
| sub | String | User ID (UUID or numeric) |
| email | String | User email |
| role | String | Single role (ADMIN/MANAGER/EMPLOYEE) |
| iat | NumericDate | Issued At |
| exp | NumericDate | Expiration (access: 15 min, refresh: 7 days) |

### 5.3 RBAC Matrix

| Endpoint | Role | Permission |
|----------|------|------------|
| `POST /api/v1/auth/**` | Any | Public |
| `GET /api/v1/users` | ADMIN | List all users |
| `DELETE /api/v1/employees/{id}` | ADMIN | Delete any employee |
| `POST /api/v1/employees` | ADMIN, MANAGER | Create employee |
| `GET /api/v1/employees` | ADMIN, MANAGER | View all employees |
| `GET /api/v1/employees/me` | EMPLOYEE | View own data |
| `POST /api/v1/leaves` | EMPLOYEE | Apply for leave |
| `POST /api/v1/leaves/{id}/approve` | MANAGER, ADMIN | Approve leave |
| `POST /api/v1/expenses` | EMPLOYEE | Submit expense |
| `POST /api/v1/expenses/{id}/approve` | MANAGER, ADMIN | Approve expense |
| `GET /api/v1/dashboard/**` | ADMIN, MANAGER | Analytics |
| `POST /api/v1/copilot/query` | Any authenticated | AI queries |
| `GET /api/v1/audit-logs` | ADMIN | Audit trail |

**Implementation:** `@PreAuthorize("hasRole('ADMIN')")` on controller methods.

### 5.4 Password Security
- **Hashing:** BCrypt with default strength (10 rounds)
- **Storage:** Passwords never logged, never returned in responses
- **Reset:** Placeholder for future email-based reset

---

## 6. AI Copilot Architecture

### 6.1 Component Diagram

```
┌──────────────┐     ┌────────────────────┐     ┌──────────────┐
│   Client     │────▶│ CopilotController  │────▶│CopilotService│
└──────────────┘     └────────────────────┘     └──────┬───────┘
                                                      │
                                 ┌────────────────────┼──────────────────┐
                                 │                    │                  │
                          ┌──────▼─────┐     ┌────────▼────────┐  ┌────▼────┐
                          │QueryIntent │     │  PromptBuilder  │  │RateLimit│
                          │Classifier  │     │                 │  │Service  │
                          └─────┬──────┘     └────────┬────────┘  └────┬────┘
                                │                     │               │
                          ┌─────▼─────┐          ┌──────▼──────┐  ┌────▼────┐
                          │Database   │          │GeminiClient │  │  Cache  │
                          │Queries    │          │             │  │         │
                          └───────────┘          └─────────────┘  └─────────┘
```

### 6.2 Query Processing Pipeline

| Step | Component | Description |
|------|-----------|-------------|
| 1. Receive | `CopilotController` | Validate request, extract user |
| 2. Check Rate Limit | `RateLimitService` | Token bucket per user (10 req/min) |
| 3. Check Cache | `CopilotCacheService` | Return cached response for identical questions (5 min TTL) |
| 4. Classify Intent | `QueryIntentAnalyzer` | Keyword-based + simple NLP to determine data domain |
| 5. Gather Context | Context fetchers | Execute DB queries based on intent |
| 6. Build Prompt | `PromptBuilder` + `PromptTemplate` | Inject data into structured template |
| 7. Call Gemini | `GeminiClient` | HTTP POST to Gemini API |
| 8. Parse & Return | `CopilotService` | Extract text, wrap in `CopilotResponse` |

### 6.3 Prompt Templates

**Template: EXPENSE_ANALYSIS**
```
You are an ERP data analyst. Based on the following expense data:
{expense_data}

Answer this question in a concise, natural language sentence:
{user_question}
If data is insufficient, say "I don't have enough data to answer that."
```

**Template: EMPLOYEE_SUMMARY**
```
You are an HR assistant. Here is employee data:
{employee_data}

Answer:
{user_question}
```

**Template: MANAGEMENT_SUMMARY**
```
You are an executive assistant. Here is the company overview:
Overview: {company_overview}
Departments: {departments_data}
Recent hires: {new_hires}
Pending approvals: {pending_approvals}

Generate a management summary in 2-3 sentences.
```

### 6.4 Error Handling & Fallbacks
- **Rate limit exceeded** → `429 Too Many Requests` with retry-after header
- **Gemini API failure** → Return pre-computed fallback response or DB-only answer
- **Invalid/malicious prompt** → Filtered, return "I can't answer that question."
- **Timeout** → Return partial data with disclaimer

---

## 7. Caching Strategy

| Cache Name | Key | TTL | Type | Invalidation |
|-----------|-----|-----|------|-------------|
| `dashboardSummary` | fixed key | 5 min | Read-heavy | `@CacheEvict` on employee/leave/expense mutations |
| `employeesPerDepartment` | fixed key | 10 min | Read-heavy | `@CacheEvict` on department/employee mutations |
| `monthlyExpenses` | month+year | 15 min | Read-heavy | `@CacheEvict` on expense approval |
| `copilotResponse` | SHA-256(question) | 5 min | Read-heavy | None (auto-expire) |
| `userProfile` | userId | 30 min | Read-heavy | `@CacheEvict` on profile update |

**Cache Provider:** Caffeine (in-memory, high-performance for single-instance app; swappable to Redis for scaling)

---

## 8. Exception Handling Strategy

**Global Handler:** `GlobalExceptionHandler` annotated with `@RestControllerAdvice`

| Exception | HTTP Status | Response Body |
|-----------|-------------|---------------|
| `ResourceNotFoundException` | 404 | `{ "error": "...", "message": "...", "timestamp": "..." }` |
| `UnauthorizedAccessException` | 403 | Same structure |
| `BadRequestException` | 400 | Same structure |
| `ConflictException` | 409 | Same structure |
| `MethodArgumentNotValidException` | 400 | `{ "error": "...", "details": { "field": "message" } }` |
| `AuthenticationException` | 401 | Standard Spring message |
| Fallback (`Exception`) | 500 | Generic server error (hide details in prod) |

**Response Structure:**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "Email is required",
    "password": "Password must be 8+ characters"
  },
  "timestamp": "2026-06-16T10:30:00Z"
}
```

---

## 9. Monitoring & Observability

### 9.1 Actuator Endpoints (Production)
- `GET /actuator/health` — Application + database health
- `GET /actuator/info` — Build version, description
- `GET /actuator/metrics` — JVM, cache, HTTP metrics
- `GET /actuator/metrics/jvm.memory.used` — Memory usage

### 9.2 Log Strategy (SLF4J + Logback)
- **Format:** JSON in production (for ELK/CloudWatch ingestion)
- **Levels:**
  - `ERROR`: Service exceptions, security events
  - `WARN`: Validation failures, rate limit hits
  - `INFO`: Major business events (user login, leave approved)
  - `DEBUG`: Request/response details (dev only)

---

## 10. Environment Profiles

### `dev` Profile (default)
- PostgreSQL on `localhost:5432`
- `show-sql: true`
- CORS open (`http://localhost:3000`)
- Bucket4j rate limit (10 req/min)
- Debug logging

### `prod` Profile
- PostgreSQL via environment variables
- `show-sql: false`
- CORS restricted to frontend origin
- Stricter rate limits
- Info+ logging only
- Actuator endpoints behind auth

---

## 11. Build & Deployment Architecture

### 11.1 Docker Multi-Stage Build

**Stage 1: Build**
- Base: `eclipse-temurin:21-jdk-alpine`
- Maven build → fat JAR

**Stage 2: Run**
- Base: `eclipse-temurin:21-jre-alpine`
- Copy JAR only (smallest image)
- Run as non-root user

### 11.2 Docker Compose

**Services:**
1. `erp-app` — Spring Boot application (port 8080)
2. `erp-db` — PostgreSQL (port 5432)

**Profiles:**
- `dev`: Hot reload, debug, volume mounts
- `prod`: Production configuration, secrets via env

---

## 12. Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Interface + Impl for services** | Enables mocking for unit tests; allows AOP proxying |
| **MapStruct compile-time** | Zero runtime reflection; faster; type-safe |
| **JWT stateless auth** | Scalable; no server-side session storage |
| **Separate request/response DTOs** | Prevents overposting; clear API contract |
| **Flyway over DDL auto** | Production-safe; version-controlled; team collaboration |
| **Caffeine over Redis** | Simpler deployment; sufficient for single-instance app |
| **AOP for audit logging** | Cross-cutting concern; non-intrusive; consistent |
| **Enum for status fields** | Type safety; prevents invalid states at compile time |
