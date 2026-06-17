# Phase 1: REST API Specification

## 1. Conventions

| Aspect | Value |
|--------|-------|
| Base URL | `http://localhost:8080/api/v1` |
| Content-Type | `application/json` |
| Auth Header | `Authorization: Bearer <jwt_token>` |
| Pagination | `?page=0&size=10&sort=createdAt,desc` |
| Date Format | ISO 8601 (`2026-06-16T10:30:00Z`) |
| Currency | INR (₹) displayed in responses |

---

## 2. Authentication (`/auth`)

### POST `/auth/register`
**Access:** Public
**Body:**
```json
{
  "name": "Rahul Sharma",
  "email": "rahul.sharma@example.com",
  "password": "SecurePass123!",
  "role": "EMPLOYEE"
}
```
**Response 201:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "name": "Rahul Sharma",
    "email": "rahul.sharma@example.com",
    "role": "EMPLOYEE",
    "status": "ACTIVE",
    "createdAt": "2026-06-16T10:30:00Z"
  }
}
```

### POST `/auth/login`
**Access:** Public
**Body:**
```json
{
  "email": "rahul.sharma@example.com",
  "password": "SecurePass123!"
}
```
**Response 200:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": 1,
      "name": "Rahul Sharma",
      "email": "rahul.sharma@example.com",
      "role": "EMPLOYEE"
    }
  }
}
```

### POST `/auth/refresh`
**Access:** Public (requires valid refresh token)
**Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```
**Response 200:** New access token (same structure as login, only access token returned).

### POST `/auth/logout`
**Access:** Authenticated
**Response 200:**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

### GET `/auth/me`
**Access:** Authenticated
**Response 200:** Current user's profile (`UserResponse`)

---

## 3. Users (`/users`)

### GET `/users`
**Roles:** ADMIN
**Query Params:** `?page=0&size=20&sort=name,asc`
**Response 200:** `PagedResponse<UserResponse>`

### GET `/users/{id}`
**Roles:** ADMIN (self: any user can view their own)
**Response 200:** `UserResponse`

### PUT `/users/{id}`
**Roles:** ADMIN, self
**Body:** `UserUpdateRequest` (name, email)
**Response 200:** Updated `UserResponse`

### PUT `/users/{id}/status`
**Roles:** ADMIN
**Body:** `{ "status": "SUSPENDED" }` ( ACTIVE | INACTIVE | SUSPENDED )
**Response 200:** Updated `UserResponse`

### DELETE `/users/{id}`
**Roles:** ADMIN
**Response 204:** No Content

---

## 4. Employees (`/employees`)

### POST `/employees`
**Roles:** ADMIN, MANAGER
**Body:**
```json
{
  "firstName": "Priya",
  "lastName": "Patel",
  "email": "priya.patel@example.com",
  "phone": "+91-9876543210",
  "designation": "Senior Software Engineer",
  "salary": 1250000.00,
  "dateOfJoining": "2025-03-15",
  "departmentId": 2,
  "managerId": 1
}
```
**Response 201:** `EmployeeResponse`

### GET `/employees`
**Roles:** ADMIN, MANAGER (sees all); EMPLOYEE (sees own)
**Query Params:** `?page=0&size=20&sort=createdAt,desc&departmentId=2&status=ACTIVE`
**Response 200:** `PagedResponse<EmployeeResponse>`

### GET `/employees/{id}`
**Roles:** ADMIN, MANAGER (any); EMPLOYEE (own only)
**Response 200:** `EmployeeResponse`

### GET `/employees/search`
**Roles:** ADMIN, MANAGER
**Query Params:** `?query=priya&department=Engineering`
**Response 200:** `PagedResponse<EmployeeResponse>`

### PUT `/employees/{id}`
**Roles:** ADMIN, MANAGER
**Body:** `EmployeeUpdateRequest`
**Response 200:** `EmployeeResponse`

### DELETE `/employees/{id}`
**Roles:** ADMIN
**Response 204:**

### GET `/employees/{id}/leaves`
**Roles:** Same as GET single
**Response 200:** `PagedResponse<LeaveResponse>`

### GET `/employees/{id}/expenses`
**Roles:** Same as GET single
**Response 200:** `PagedResponse<ExpenseResponse>`

---

## 5. Departments (`/departments`)

### POST `/departments`
**Roles:** ADMIN
**Body:**
```json
{
  "name": "Engineering",
  "description": "Software development, DevOps, QA",
  "budget": 5000000.00
}
```
**Response 201:** `DepartmentResponse`

### GET `/departments`
**Roles:** Any authenticated
**Response 200:** `PagedResponse<DepartmentResponse>`

### GET `/departments/{id}`
**Roles:** Any authenticated
**Response 200:** `DepartmentResponse`

### PUT `/departments/{id}`
**Roles:** ADMIN
**Body:** `DepartmentUpdateRequest`
**Response 200:** `DepartmentResponse`

### DELETE `/departments/{id}`
**Roles:** ADMIN
**Response 204:**

### PUT `/departments/{id}/manager`
**Roles:** ADMIN, MANAGER
**Body:** `{ "employeeId": 5 }`
**Response 200:** `DepartmentResponse`

### GET `/departments/{id}/employees`
**Roles:** Any authenticated
**Response 200:** `List<EmployeeResponse>`

---

## 6. Leave Requests (`/leaves`)

### POST `/leaves`
**Roles:** EMPLOYEE (own), ADMIN (any)
**Body:**
```json
{
  "leaveType": "SICK",
  "startDate": "2026-06-20",
  "endDate": "2026-06-22",
  "reason": "Fever and rest prescribed"
}
```
**Response 201:** `LeaveResponse` (status: PENDING)

### GET `/leaves`
**Roles:** ADMIN, MANAGER (all); EMPLOYEE (own only)
**Query:** `?page=0&size=10&sort=createdAt,desc&status=PENDING`
**Response 200:** `PagedResponse<LeaveResponse>`

### GET `/leaves/{id}`
**Roles:** Same as list
**Response 200:** `LeaveResponse`

### PUT `/leaves/{id}`
**Roles:** Owner only if status=PENDING
**Body:** `LeaveUpdateRequest`
**Response 200:** `LeaveResponse`

### DELETE `/leaves/{id}`
**Roles:** Owner only if status=PENDING
**Response 204:**

### POST `/leaves/{id}/approve`
**Roles:** MANAGER, ADMIN
**Response 200:** `LeaveResponse` (status: APPROVED)

### POST `/leaves/{id}/reject`
**Roles:** MANAGER, ADMIN
**Response 200:** `LeaveResponse` (status: REJECTED)

---

## 7. Expenses (`/expenses`)

### POST `/expenses`
**Roles:** EMPLOYEE (own), ADMIN (any)
**Body:**
```json
{
  "amount": 1250.50,
  "category": "TRAVEL",
  "description": "Cab to client site in Pune",
  "expenseDate": "2026-06-10",
  "receiptUrl": "https://s3.example.com/receipts/abc.jpg"
}
```
**Response 201:** `ExpenseResponse`

### GET `/expenses`
**Roles:** ADMIN, MANAGER (all); EMPLOYEE (own)
**Query:** `?page=0&size=10&sort=createdAt,desc&status=APPROVED&category=TRAVEL`
**Response 200:** `PagedResponse<ExpenseResponse>`

### GET `/expenses/{id}`
**Roles:** Same as list
**Response 200:** `ExpenseResponse`

### PUT `/expenses/{id}`
**Roles:** Owner only if status=PENDING
**Body:** `ExpenseUpdateRequest`
**Response 200:** `ExpenseResponse`

### DELETE `/expenses/{id}`
**Roles:** Owner only if status=PENDING
**Response 204:**

### POST `/expenses/{id}/approve`
**Roles:** MANAGER, ADMIN
**Response 200:** `ExpenseResponse` (status: APPROVED)

### POST `/expenses/{id}/reject`
**Roles:** MANAGER, ADMIN
**Response 200:** `ExpenseResponse` (status: REJECTED)

---

## 8. Dashboard Analytics (`/dashboard`)

All endpoints require ADMIN or MANAGER role.

### GET `/dashboard/summary`
**Response 200:**
```json
{
  "totalEmployees": 50,
  "totalDepartments": 5,
  "pendingLeaves": 8,
  "approvedLeavesThisMonth": 12,
  "pendingExpenses": 5,
  "totalExpensesThisMonth": 245000.00,
  "newEmployeesThisMonth": 3
}
```

### GET `/dashboard/employees-per-department`
**Response 200:**
```json
[
  { "departmentName": "Engineering", "count": 15 },
  { "departmentName": "Sales", "count": 10 }
]
```

### GET `/dashboard/monthly-expenses`
**Query:** `?year=2026`
**Response 200:**
```json
{
  "year": 2026,
  "data": [
    { "month": "January", "totalAmount": 180000 },
    { "month": "February", "totalAmount": 210000 }
  ]
}
```

### GET `/dashboard/department-expenses`
**Query:** `?year=2026&month=6`
**Response 200:**
```json
[
  { "departmentName": "Engineering", "totalAmount": 95000 },
  { "departmentName": "Sales", "totalAmount": 45000 }
]
```

### GET `/dashboard/leave-trends`
**Query:** `?year=2026`
**Response 200:** Monthly leave counts per type.

---

## 9. AI Copilot (`/copilot`)

### POST `/copilot/query`
**Roles:** Any authenticated
**Rate Limit:** 10 requests/minute per user
**Body:**
```json
{
  "question": "Which department spent the most this month?"
}
```
**Response 200:**
```json
{
  "success": true,
  "data": {
    "answer": "Engineering spent ₹245,000 this month, which is 35% higher than Sales.",
    "intent": "EXPENSE_ANALYSIS",
    "dataQueryTimeMs": 45,
    "aiResponseTimeMs": 320
  }
}
```

**Response 429 (Rate Limited):**
```json
{
  "success": false,
  "message": "Rate limit exceeded. Try again in 60 seconds."
}
```

**Response 503 (AI Service Unavailable):**
```json
{
  "success": false,
  "message": "AI service is temporarily unavailable. Please try again later."
}
```

---

## 10. Audit Logs (`/audit-logs`)

### GET `/audit-logs`
**Roles:** ADMIN
**Query:** `?page=0&size=20&sort=createdAt,desc&action=LEAVE_APPROVAL`
**Response 200:** `PagedResponse<AuditLogResponse>`

### GET `/audit-logs/{id}`
**Roles:** ADMIN
**Response 200:** `AuditLogResponse`

---

## 11. Error Response Format (Universal)

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "Email format is invalid",
    "password": "Password must be at least 8 characters"
  },
  "timestamp": "2026-06-16T10:30:00Z"
}
```

## 12. HTTP Status Codes

| Status | Meaning |
|--------|---------|
| 200 | Success (GET, PUT) |
| 201 | Created (POST) |
| 204 | No Content (DELETE) |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (missing/invalid token) |
| 403 | Forbidden (valid token, insufficient role) |
| 404 | Not Found |
| 409 | Conflict (unique constraint violation) |
| 429 | Too Many Requests |
| 500 | Internal Server Error |
