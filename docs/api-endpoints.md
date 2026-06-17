# API Endpoints

## Base URL
`/api/v1`

## Authentication
- **POST** `/auth/register` - Register a new user
- **POST** `/auth/login` - Login and receive JWT token
- **POST** `/auth/refresh` - Refresh access token using refresh token
- **POST** `/auth/logout` - Logout (invalidate token on client side, server-side blacklist optional)

## User Management
- **GET** `/users/profile` - Get current user's profile
- **PUT** `/users/profile` - Update current user's profile
- **PUT** `/users/password` - Change password
- **GET** `/users` (Admin only) - Get all users with pagination
- **GET** `/users/{id}` (Admin only) - Get user by ID
- **PUT** `/users/{id}` (Admin only) - Update user
- **DELETE** `/users/{id}` (Admin only) - Delete user

## Employee Management
- **GET** `/employees` - Get all employees with pagination, filtering, sorting
- **GET** `/employees/{id}` - Get employee by ID
- **POST** `/employees` - Create new employee (Admin/Manager)
- **PUT** `/employees/{id}` - Update employee (Admin/Manager)
- **DELETE** `/employees/{id}` (Admin only) - Delete employee
- **GET** `/employees/search` - Search employees by name, email, designation, department

## Department Management
- **GET** `/departments` - Get all departments with pagination
- **GET** `/departments/{id}` - Get department by ID
- **POST** `/departments` - Create new department (Admin/Manager)
- **PUT** `/departments/{id}` - Update department (Admin/Manager)
- **DELETE** `/departments/{id}` (Admin only) - Delete department
- **PUT** `/departments/{id}/manager` - Assign manager to department (Admin/Manager)
- **GET** `/departments/{id}/analytics` - Get department analytics (Manager/Admin)

## Leave Management
- **GET** `/leaves` - Get leave requests with filtering (employee can see own, manager can see team/department, admin sees all)
- **GET** `/leaves/{id}` - Get leave request by ID
- **POST** `/leaves` - Submit new leave request (Employee)
- **PUT** `/leaves/{id}/cancel` - Cancel leave request (Employee, only if pending)
- **PUT** `/leaves/{id}/approve` - Approve leave request (Manager)
- **PUT** `/leaves/{id}/reject` - Reject leave request (Manager)
- **GET** `/employees/{employeeId}/leaves` - Get leave history for an employee

## Expense Management
- **GET** `/expenses` - Get expense claims with filtering
- **GET** `/expenses/{id}` - Get expense claim by ID
- **POST** `/expenses` - Submit new expense claim (Employee)
- **PUT** `/expenses/{id}/approve` - Approve expense claim (Manager)
- **PUT** `/expenses/{id}/reject` - Reject expense claim (Manager)
- **GET** `/employees/{employeeId}/expenses` - Get expense history for an employee

## Dashboard & Analytics
- **GET** `/dashboard/summary` - Get overall summary metrics
- **GET** `/dashboard/employees-per-department` - Get employee count per department
- **GET** `/dashboard/pending-leaves` - Get count of pending leave requests
- **GET** `/dashboard/approved-leaves` - Get count of approved leave requests (with date filtering)
- **GET** `/dashboard/pending-expenses` - Get count of pending expense claims
- **GET** `/dashboard/approved-expenses` - Get count of approved expense claims (with date filtering)
- **GET** `/dashboard/monthly-expense-trends` - Get monthly expense trends for last 6 months
- **GET** `/dashboard/department-expense-breakdown` - Get expense breakdown by department
- **GET** `/dashboard/new-employees-month` - Get count of new employees this month

## AI Copilot
- **POST** `/copilot/query` - Ask a question to the AI Copilot
  - Request body: `{ "question": "string" }`
  - Response: `{ "answer": "string", "suggestions": ["string"] }`
- **GET** `/copilot/suggestions` - Get suggested questions based on user role and recent activity

## Audit Logs
- **GET** `/audit-logs` - Get audit logs with filtering (Admin only)
- **GET** `/audit-logs/{id}` - Get audit log by ID (Admin only)

## Health & Monitoring
- **GET** `/actuator/health` - Health check endpoint
- **GET** `/actuator/metrics` - Metrics endpoint
- **GET** `/actuator/info` - Application info
- **GET** `/actuator/loggers` - Logger configuration

## Swagger/OpenAPI Documentation
- **GET** `/swagger-ui.html` - Swagger UI interface
- **GET** `/v3/api-docs` - OpenAPI JSON specification
- **GET** `/swagger-ui/index.html` - Alternative Swagger UI

## Notes
### Security
- All endpoints except `/auth/*` require a valid JWT token in the Authorization header: `Bearer <token>`
- Role-based access control is enforced at the service/method level using `@PreAuthorize` annotations.

### Pagination & Sorting
- List endpoints support pagination: `?page=0&size=20&sort=field,desc`
- Filtering is implemented via query parameters specific to each endpoint.

### Versioning
- API is versioned in the URL (`/api/v1/*`) to allow for future breaking changes.

### Error Handling
- All endpoints return consistent error responses:
  ```json
  {
    "timestamp": "2026-06-16T10:30:00.000+00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "path": "/api/v1/employees"
  }
  ```

### Request/Response Format
- All request and response bodies are in JSON format.
- Dates are formatted as ISO 8601 strings.
- Decimal numbers are represented as strings or numbers based on precision requirements.

### Example Responses
#### Successful Response
```json
{
  "data": {
    // resource data
  },
  "message": "Operation successful",
  "timestamp": "2026-06-16T10:30:00.000+00:00"
}
```

#### Paginated Response
```json
{
  "content": [
    // array of resources
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {...}
  },
  "totalElements": 150,
  "totalPages": 8,
  "last": false,
  "size": 20,
  "number": 0,
  "first": true
}
```