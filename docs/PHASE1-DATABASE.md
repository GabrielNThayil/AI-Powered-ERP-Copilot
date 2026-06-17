# Phase 1: Database Design & ERD

## 1. Entity Relationship Diagram

```
                    ┌──────────────┐
                    │    roles     │
                    │──────────────│
                    │ id (PK)      │
                    │ name         │
                    │ description  │
                    └──────┬───────┘
                           │
                           │ 1:N
                           │
                    ┌──────┴───────┐
      ┌─────────────│    users     │◄───────────────────┐
      │             │──────────────│                    │
      │             │ id (PK)      │                    │
      │             │ name         │                    │
      │             │ email        │                    │
      │             │ password     │                    │
      │             │ role_id (FK) │                    │
      │             │ status       │                    │
      │             │ created_at   │                    │
      │             │ updated_at   │                    │
      │             └──────┬───────┘                    │
      │                    │                           │
      │                    │ 1:1 (nullable)           │
      │                    │                           │
      │             ┌──────┴───────┐                  │
      │             │   employees  │                   │
      │             │──────────────│                   │
      │             │ id (PK)      │◄─────────┐        │
      │             │ employee_code│          │        │
      │             │ name         │          │        │
      │             │ email        │          │        │
      │             │ phone        │          │        │
      │             │ designation  │          │        │
      │             │ salary       │          │        │
      │             │ joining_date │          │        │
      │             │ dept_id(FK)──┼──────────┘        │
      │             │ status       │                   │
      │             │ user_id(FK)  │                   │
      │             │ mgr_id (FK)  │───────────────────┘
      │             │ created_at   │    (self-reference)
      │             │ updated_at   │
      │             └──────┬───────┘
      │                    │
      │                    │ 1:N
      │                    │
      │        ┌───────────┴────────────────┐
      │        │                            │
      │┌───────▼────────┐          ┌─────────▼─────────┐
      ││  leave_requests│          │     expenses      │
      ││────────────────│          │─────────────────  │
      ││ id (PK)        │          │ id (PK)           │
      ││ employee_id(FK)│          │ employee_id (FK)  │
      ││ leave_type     │          │ amount            │
      ││ start_date     │          │ category          │
      ││ end_date       │          │ description       │
      ││ reason         │          │ expense_date      │
      ││ status         │          │ status            │
      ││ approved_by(FK)│          │ approved_by (FK)  │
      ││ approved_at    │          │ approved_at       │
      ││ created_at     │          │ receipt_url       │
      ││ updated_at     │          │ created_at        │
      │└────────────────┘          │ updated_at        │
      │                            └───────────────────┘
      │
      │            ┌──────────────┐
      └───────────►│ departments  │
                   │──────────────│
                   │ id (PK)      │
                   │ name         │
                   │ description  │
                   │ budget       │
                   │ manager_id(FK)│────► employees.id
                   │ created_at   │
                   │ updated_at   │
                   └──────────────┘

                    ┌──────────────┐
                    │  audit_logs  │
                    │──────────────│
                    │ id (PK)      │
                    │ user_id(FK)  │────► users.id
                    │ action       │
                    │ entity_type  │
                    │ entity_id    │
                    │ details      │
                    │ ip_address   │
                    │ created_at   │
                    └──────────────┘
```

---

## 2. Schema Definition (Final State)

### 2.1 roles

```sql
CREATE TYPE user_role AS ENUM ('ADMIN', 'MANAGER', 'EMPLOYEE');

CREATE TABLE roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP
);
```

### 2.2 users

```sql
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(100) NOT NULL UNIQUE,
    first_name  VARCHAR(50) NOT NULL,
    last_name   VARCHAR(50) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role_id     BIGINT NOT NULL,
    status      VARCHAR(20) DEFAULT 'ACTIVE',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### 2.3 departments (without manager_id initially)

```sql
CREATE TABLE departments (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    budget      DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP
);
```

### 2.4 employees

```sql
CREATE TABLE employees (
    id               BIGSERIAL PRIMARY KEY,
    employee_code    VARCHAR(20) NOT NULL UNIQUE,
    first_name       VARCHAR(50) NOT NULL,
    last_name        VARCHAR(50) NOT NULL,
    email            VARCHAR(150) NOT NULL UNIQUE,
    phone            VARCHAR(20),
    designation      VARCHAR(100) NOT NULL,
    salary           DECIMAL(12,2) NOT NULL,
    date_of_joining  DATE NOT NULL,
    employment_status VARCHAR(20) DEFAULT 'ACTIVE',
    department_id    BIGINT,
    user_id          BIGINT UNIQUE,
    manager_id       BIGINT,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,
    CONSTRAINT fk_employees_dept   FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_employees_user   FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_employees_mgr  FOREIGN KEY (manager_id) REFERENCES employees(id)
);
```

### 2.5 leave_requests

```sql
CREATE TABLE leave_requests (
    id            BIGSERIAL PRIMARY KEY,
    employee_id   BIGINT NOT NULL,
    leave_type    VARCHAR(30) NOT NULL,   -- SICK, CASUAL, PAID, UNPAID, MATERNITY, PATERNITY, BEREAVEMENT
    start_date    DATE NOT NULL,
    end_date      DATE NOT NULL,
    reason        TEXT,
    status        VARCHAR(20) DEFAULT 'PENDING',
    approved_by   BIGINT,
    approved_at   TIMESTAMP,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP,
    CONSTRAINT fk_leaves_employee    FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_leaves_approved_by FOREIGN KEY (approved_by) REFERENCES employees(id),
    CONSTRAINT chk_leave_dates       CHECK (end_date >= start_date)
);
```

### 2.6 expenses

```sql
CREATE TABLE expenses (
    id            BIGSERIAL PRIMARY KEY,
    employee_id   BIGINT NOT NULL,
    amount        DECIMAL(12,2) NOT NULL CHECK (amount > 0),
    category      VARCHAR(50) NOT NULL,  -- TRAVEL, MEALS, OFFICE_SUPPLIES, SOFTWARE, HARDWARE, TRAINING, OTHER
    description   TEXT,
    expense_date  DATE NOT NULL,
    status        VARCHAR(20) DEFAULT 'PENDING',
    approved_by   BIGINT,
    approved_at   TIMESTAMP,
    receipt_url   VARCHAR(500),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP,
    CONSTRAINT fk_expenses_employee    FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_expenses_approved_by FOREIGN KEY (approved_by) REFERENCES employees(id)
);
```

### 2.7 audit_logs

```sql
CREATE TABLE audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT,
    action      VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id   BIGINT,
    details     TEXT,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 2.8 departments — manager_fk added after employees exist

```sql
ALTER TABLE departments
    ADD COLUMN manager_id BIGINT UNIQUE,
    ADD CONSTRAINT fk_departments_manager FOREIGN KEY (manager_id) REFERENCES employees(id);
```

---

## 3. Indexes

```sql
-- roles
CREATE INDEX idx_roles_name ON roles(name);

-- users
CREATE INDEX idx_users_username   ON users(username);
CREATE INDEX idx_users_email      ON users(email);
CREATE INDEX idx_users_role_id    ON users(role_id);
CREATE INDEX idx_users_status     ON users(status);

-- employees
CREATE INDEX idx_employees_code         ON employees(employee_code);
CREATE INDEX idx_employees_email        ON employees(email);
CREATE INDEX idx_employees_dept_id      ON employees(department_id);
CREATE INDEX idx_employees_user_id      ON employees(user_id);
CREATE INDEX idx_employees_manager_id   ON employees(manager_id);
CREATE INDEX idx_employees_status       ON employees(employment_status);
CREATE INDEX idx_employees_joining      ON employees(date_of_joining);

-- departments
CREATE INDEX idx_departments_manager ON departments(manager_id);

-- leave_requests
CREATE INDEX idx_leaves_employee  ON leave_requests(employee_id);
CREATE INDEX idx_leaves_status    ON leave_requests(status);
CREATE INDEX idx_leaves_approved  ON leave_requests(approved_by);

-- expenses
CREATE INDEX idx_expenses_employee  ON expenses(employee_id);
CREATE INDEX idx_expenses_status    ON expenses(status);
CREATE INDEX idx_expenses_category  ON expenses(category);
CREATE INDEX idx_expenses_approved  ON expenses(approved_by);

-- audit_logs
CREATE INDEX idx_audit_user_id    ON audit_logs(user_id);
CREATE INDEX idx_audit_action     ON audit_logs(action);
CREATE INDEX idx_audit_entity     ON audit_logs(entity_type);
CREATE INDEX idx_audit_created    ON audit_logs(created_at);
```

---

## 4. Flyway Migration Strategy

Migration files under `src/main/resources/db/migration/`:

| File | Description | Note |
|------|-------------|------|
| `V1__create_roles_table.sql` | Create roles | Independent table |
| `V2__create_users_table.sql` | Create users + FK to roles | Depends on V1 |
| `V3__create_departments_table.sql` | Create departments (sans manager) | Independent |
| `V4__create_employees_table.sql` | Create employees + FKs to users/dept | Depends on V2, V3 |
| `V5__create_leave_requests_table.sql` | Create leave_requests | Depends on V4 |
| `V6__create_expenses_table.sql` | Create expenses | Depends on V4 |
| `V7__create_audit_logs_table.sql` | Create audit_logs | Depends on V2 |
| `V8__add_department_manager_fk.sql` | Add manager_id to departments | Depends on V3, V4 |
| `V9__add_all_indexes.sql` | Create all indexes | Depends on all tables |
| `V10__insert_seed_data.sql` | Insert roles, admin, sample depts/employees | Depends on all |

---

## 5. Normalization & Constraints Summary

| Table | 1NF | 2NF | 3NF | Constraints |
|-------|-----|-----|-----|-------------|
| roles | ✓ | ✓ | ✓ | UNIQUE(name) |
| users | ✓ | ✓ | ✓ | UNIQUE(email), FK(role_id) |
| departments | ✓ | ✓ | ✓ | UNIQUE(name), FK(manager_id) |
| employees | ✓ | ✓ | ✓ | UNIQUE(employee_code, email, user_id) |
| leave_requests | ✓ | ✓ | ✓ | CHECK(end_date >= start_date), FKs |
| expenses | ✓ | ✓ | ✓ | CHECK(amount > 0), FKs |
| audit_logs | ✓ | ✓ | ✓ | FK(user_id, nullable) |

**Audit Fields:** Every table (except roles/audit_logs) has `created_at` and `updated_at`.

**Soft Delete:** No physical row deletion for employees; use `employment_status = INACTIVE | TERMINATED`.
