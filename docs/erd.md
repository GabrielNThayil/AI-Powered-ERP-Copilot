# Entity Relationship Diagram (ERD)

## Tables Overview

### 1. `users`
- Stores authentication and authorization information.
- Fields:
  - `id` (PK, UUID)
  - `name` (VARCHAR)
  - `email` (VARCHAR, unique)
  - `password` (VARCHAR)
  - `role` (ENUM: ADMIN, MANAGER, EMPLOYEE)
  - `status` (ENUM: ACTIVE, INACTIVE, SUSPENDED)
  - `created_at` (TIMESTAMP)
  - `updated_at` (TIMESTAMP)

### 2. `roles` (Optional - if we want to separate roles table, but we'll keep it simple with enum in users)
- Note: We are using enum for role in users table for simplicity. If we need more complex role-permission mapping, we would have a separate roles table and join table.

### 3. `employees`
- Stores employee information.
- Fields:
  - `id` (PK, UUID)
  - `employee_id` (VARCHAR, unique) - e.g., EMP001
  - `user_id` (FK to users.id)
  - `name` (VARCHAR)
  - `email` (VARCHAR)
  - `phone` (VARCHAR)
  - `designation` (VARCHAR)
  - `salary` (DECIMAL)
  - `date_of_joining` (DATE)
  - `department_id` (FK to departments.id)
  - `employment_status` (ENUM: FULL_TIME, PART_TIME, CONTRACT, INTERN)
  - `created_at` (TIMESTAMP)
  - `updated_at` (TIMESTAMP)

### 4. `departments`
- Stores department information.
- Fields:
  - `id` (PK, UUID)
  - `name` (VARCHAR, unique)
  - `budget` (DECIMAL)
  - `manager_id` (FK to employees.id) - optional
  - `description` (TEXT)
  - `created_at` (TIMESTAMP)
  - `updated_at` (TIMESTAMP)

### 5. `leave_requests`
- Stores leave requests submitted by employees.
- Fields:
  - `id` (PK, UUID)
  - `employee_id` (FK to employees.id)
  - `leave_type` (VARCHAR) - e.g., SICK, VACATION, PERSONAL
  - `start_date` (DATE)
  - `end_date` (DATE)
  - `reason` (TEXT)
  - `status` (ENUM: PENDING, APPROVED, REJECTED)
  - `approved_by` (FK to employees.id, nullable) - who approved/rejected
  - `approved_at` (TIMESTAMP, nullable)
  - `created_at` (TIMESTAMP)
  - `updated_at` (TIMESTAMP)

### 6. `expenses`
- Stores expense claims submitted by employees.
- Fields:
  - `id` (PK, UUID)
  - `employee_id` (FK to employees.id)
  - `amount` (DECIMAL)
  - `category` (VARCHAR) - e.g., TRAVEL, FOOD, OFFICE_SUPPLIES
  - `description` (TEXT)
  - `expense_date` (DATE)
  - `status` (ENUM: PENDING, APPROVED, REJECTED)
  - `approved_by` (FK to employees.id, nullable)
  - `approved_at` (TIMESTAMP, nullable)
  - `created_at` (TIMESTAMP)
  - `updated_at` (TIMESTAMP)

### 7. `audit_logs`
- Stores audit trail for critical operations.
- Fields:
  - `id` (PK, UUID)
  - `user_id` (FK to users.id) - who performed the action
  - `action` (VARCHAR) - e.g., CREATE_EMPLOYEE, APPROVE_LEAVE
  - `entity_type` (VARCHAR) - e.g., EMPLOYEE, LEAVE_REQUEST
  - `entity_id` (VARCHAR) - ID of the entity affected
  - `details` (JSONB) - additional details about the change
  - `timestamp` (TIMESTAMP)

## Relationships

### 1. Users and Employees
- One-to-One: One user can be associated with one employee record (for employees). 
  - Note: Not all users are employees (e.g., ADMIN might not have an employee record). 
  - We'll make the relationship from employees to users optional? Actually, every employee must have a user account for login. 
  - But users can exist without being an employee (if we have separate admin users not tied to employee). 
  - We'll set: `employees.user_id` is a foreign key to `users.id` and is required (not null).

### 2. Departments and Employees
- Many-to-One: Many employees belong to one department.
- One-to-One (optional): One department can have one manager (who is an employee). 
  - `departments.manager_id` references `employees.id`.

### 3. Leave Requests and Employees
- Many-to-One: Many leave requests belong to one employee (the requester).
- Many-to-One: Leave requests can be approved by one employee (the approver). 
  - `leave_requests.approved_by` references `employees.id`.

### 4. Expenses and Employees
- Many-to-One: Many expenses belong to one employee (the submitter).
- Many-to-One: Expenses can be approved by one employee (the approver). 
  - `expenses.approved_by` references `employees.id`.

### 5. Audit Logs and Users
- Many-to-One: Many audit log entries are associated with one user (the performer).

## Indexes
- Primary keys on all `id` columns.
- Unique constraints: 
  - `users.email`
  - `employees.employee_id`
  - `employees.email` (if we want to keep it unique, but it might be same as user email)
  - `departments.name`
- Foreign key indexes (automatically created by most databases, but we'll ensure in Flyway).
- Additional indexes for query performance:
  - `leave_requests.employee_id`, `leave_requests.status`
  - `expenses.employee_id`, `expenses.status`
  - `audit_logs.user_id`, `audit_logs.timestamp`

## Notes
- We use UUIDs for primary keys to avoid exposing sequential IDs and for better security.
- Audit logs use JSONB for flexible details storage (PostgreSQL specific).
- Enum fields are implemented as VARCHAR with check constraints or using PostgreSQL ENUM types (we'll use VARCHAR for portability and use @Enumerated in Java).
- All tables have `created_at` and `updated_at` audit trails.