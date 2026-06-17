-- Seed data for ERP Copilot
-- Inserts 5 departments, 50 employees (including managers and an admin),
-- and some sample leave requests and expenses

DO $$
DECLARE
    -- Department data
    dept_names VARCHAR[] := ARRAY['Engineering', 'Sales', 'HR', 'Finance', 'Marketing'];
    dept_budgets DECIMAL[] := ARRAY[500000, 450000, 300000, 400000, 350000];
    dept_descriptions VARCHAR[] := ARRAY[
        'Software development and technical team',
        'Sales and customer acquisition team',
        'Human resources and personnel management',
        'Financial planning and accounting',
        'Marketing and brand management'
    ];
    dept_id UUID;
    dept_index INT;

    -- Employee data
    first_names VARCHAR[] := ARRAY[
        'James', 'Mary', 'John', 'Patricia', 'Robert', 'Jennifer', 'Michael', 'Linda',
        'William', 'Elizabeth', 'David', 'Barbara', 'Richard', 'Susan', 'Joseph', 'Jessica',
        'Thomas', 'Sarah', 'Charles', 'Karen', 'Christopher', 'Nancy', 'Daniel', 'Lisa',
        'Matthew', 'Betty', 'Anthony', 'Helen', 'Mark', 'Sandra', 'Paul', 'Ashley',
        'Andrew', 'Donna', 'Joshua', 'Kimberly', 'Kenneth', 'Emily', 'Kevin', 'Michelle',
        'Brian', 'Dorothy', 'George', 'Lisa', 'Timothy', 'Nancy', 'Edward', 'Sandra',
        'Ronald', 'Betty', 'Jason', 'Margaret', 'Ryan', 'Catherine', 'Gary', 'Elizabeth',
        'Nicholas'
    ];
    last_names VARCHAR[] := ARRAY[
        'Smith', 'Johnson', 'Williams', 'Jones', 'Brown', 'Davis', 'Miller', 'Wilson',
        'Moore', 'Taylor', 'Anderson', 'Thomas', 'Jackson', 'White', 'Harris', 'Martin',
        'Thompson', 'Garcia', 'Martinez', 'Robinson', 'Clark', 'Rodriguez', 'Lewis', 'Lee',
        'Walker', 'Hall', 'Allen', 'Young', 'Hernandez', 'King', 'Wright', 'Lopez',
        'Hill', 'Scott', 'Green', 'Adams', 'Baker', 'Gonzalez', 'Nelson', 'Carter',
        'Mitchell', 'Perez', 'Roberts', 'Turner', 'Phillips', 'Campbell', 'Parker', 'Evans',
        'Edwards', 'Collins', 'Stewart', 'Sanchez', 'Morris', 'Rogers', 'Reed', 'Cook',
        'Morgan', 'Bell', 'Murphy', 'Bailey', 'Rivera', 'Cooper', 'Richardson', 'Cox',
        'Howard', 'Ward', 'Torres', 'Peterson', 'Gray', 'Ramirez', 'James', 'Watson',
        'Brooks', 'Kelly', 'Sanders', 'Price', 'Bennett', 'Wood', 'Barnes', 'Ross',
        'Henderson', 'Coleman', 'Jenkins', 'Perry', 'Powell', 'Long', 'Patterson', 'Hughes'
    ];
    designations VARCHAR[] := ARRAY[
        'Software Engineer', 'Senior Software Engineer', 'Team Lead', 'Manager',
        'Sales Representative', 'Sales Manager', 'HR Specialist', 'HR Manager',
        'Financial Analyst', 'Accountant', 'Senior Accountant', 'Finance Manager',
        'Marketing Coordinator', 'Marketing Manager', 'Digital Marketing Specialist',
        'UX Designer', 'UI Designer', 'Product Manager', 'Data Analyst',
        'Database Administrator', 'Network Engineer', 'Systems Administrator',
        'Customer Support Representative', 'Technical Writer', 'Business Analyst',
        'Operations Manager', 'Quality Assurance Engineer', 'DevOps Engineer',
        'Security Analyst', 'Project Manager', 'Chief Technology Officer'
    ];
    employment_statuses VARCHAR[] := ARRAY['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERN'];
    leave_types VARCHAR[] := ARRAY['VACATION', 'SICK', 'PERSONAL', 'BEREAVEMENT', 'PARENTAL'];
    expense_categories VARCHAR[] := ARRAY[
        'TRAVEL', 'FOOD', 'LODGING', 'OFFICE_SUPPLIES', 'EQUIPMENT',
        'TRAINING', 'CONFERENCE', 'CLIENT_ENTERTAINMENT', 'TRANSPORTATION', 'OTHER'
    ];

    -- Loop variables
    i INT := 0;
    j INT := 0;
    dept_idx INT := 0;
    emp_count INT := 0;
    user_id UUID;
    emp_id UUID;
    first_name VARCHAR;
    last_name VARCHAR;
    email VARCHAR;
    employee_id VARCHAR;
    salary DECIMAL;
    designation VARCHAR;
    status VARCHAR;
    hire_date TIMESTAMP;
    phone VARCHAR;
    is_manager BOOLEAN;
    is_admin BOOLEAN;

BEGIN
    -- Insert departments and capture their IDs
    FOR dept_idx IN 1..array_length(dept_names, 1) LOOP
        INSERT INTO departments (id, name, budget, description)
        VALUES (uuid_generate_v4(), dept_names[dept_idx], dept_budgets[dept_idx], dept_descriptions[dept_idx])
        RETURNING id INTO dept_id;

        -- For each department, create a manager and 9 regular employees
        -- Manager (j=0) and regular employees (j=1..9)
        FOR j IN 0..9 LOOP
            emp_count := emp_count + 1;
            is_manager := (j = 0);
            is_admin := (dept_idx = 3 AND j = 0); -- HR department manager is ADMIN

            -- Generate employee data
            first_name := first_names[((emp_count-1) % array_length(first_names, 1)) + 1];
            last_name := last_names[((emp_count-1) % array_length(last_names, 1)) + 1];
            employee_id := 'EMP' || lpad(emp_count::text, 4, '0');
            email := lower(first_name || '.' || last_name || '@company.com');

            -- Salary based on designation and manager status
            IF is_manager THEN
                salary := 80000 + (random() * 30000); -- 80k-110k
            ELSE
                salary := 50000 + (random() * 40000); -- 50k-90k
            END IF;

            designation := designations[((emp_count-1) % array_length(designations, 1)) + 1];
            status := employment_statuses[1]; -- mostly FULL_TIME, we can vary later
            hire_date := CURRENT_TIMESTAMP - (interval '1 day' * floor(random() * 1825)); -- up to 5 years ago
            phone := '+1-555-' || lpad(floor(random() * 1000)::text, 3, '0') || '-' || lpad(floor(random() * 10000)::text, 4, '0');

            -- Insert user
            INSERT INTO users (id, name, email, password, role, status)
            VALUES (
                uuid_generate_v4(),
                first_name || ' ' || last_name,
                email,
                -- Default password: 'Password123!' hashed with BCrypt (we'll use a placeholder; in real app, use password encoder)
                '$2a$10$N9qo8uLOickgx2ZMRZoMyeGjZBc6KfdIKejvqOFxZnIq4zS0hSAKa', -- bcrypt hash of 'Password123!'
                CASE
                    WHEN is_admin THEN 'ADMIN'
                    WHEN is_manager THEN 'MANAGER'
                    ELSE 'EMPLOYEE'
                END,
                'ACTIVE'
            )
            RETURNING id INTO user_id;

            -- Insert employee
            INSERT INTO employees (
                id, employee_id, user_id, name, email, phone, designation,
                salary, date_of_joining, department_id, employment_status
            )
            VALUES (
                uuid_generate_v4(),
                employee_id,
                user_id,
                first_name || ' ' || last_name,
                email,
                phone,
                designation,
                salary,
                hire_date,
                dept_id,
                status
            );

            -- For some employees, generate leave requests and expenses
            -- We'll do this after inserting all employees to avoid complex nesting
        END LOOP;
    END LOOP;

    -- Now insert some sample leave requests and expenses
    -- We'll select a few employees and create records

    -- Leave requests: 5 per department on average
    INSERT INTO leave_requests (id, employee_id, leave_type, start_date, end_date, reason, status, approved_by, approved_at, created_at, updated_at)
    SELECT
        uuid_generate_v4(),
        e.id,
        leave_types[floor(random() * array_length(leave_types, 1)) + 1],
        e.date_of_joining + (interval '1 day' * floor(random() * 365)),
        e.date_of_joining + (interval '1 day' * floor(random() * 365)) + (interval '1 day' * floor(random() * 10)),
        'Leave request for personal reasons',
        CASE floor(random() * 3)
            WHEN 0 THEN 'PENDING'
            WHEN 1 THEN 'APPROVED'
            ELSE 'REJECTED'
        END,
        CASE WHEN floor(random() * 2) = 0 THEN (SELECT id FROM employees WHERE department_id = e.department_id AND employee_id LIKE 'EMP%01' LIMIT 1) ELSE NULL END,
        CASE WHEN floor(random() * 2) = 0 THEN CURRENT_TIMESTAMP ELSE NULL END,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    FROM employees e
    WHERE floor(random() * 5) = 0 -- about 20% of employees
    LIMIT 25; -- total leave requests

    -- Expenses: 10 per department on average
    INSERT INTO expenses (id, employee_id, amount, category, description, expense_date, status, approved_by, approved_at, created_at, updated_at)
    SELECT
        uuid_generate_v4(),
        e.id,
        round(50 + (random() * 950), 2), -- $50 to $1000
        expense_categories[floor(random() * array_length(expense_categories, 1)) + 1],
        'Expense for business purpose',
        e.date_of_joining + (interval '1 day' * floor(random() * 365)),
        CASE floor(random() * 3)
            WHEN 0 THEN 'PENDING'
            WHEN 1 THEN 'APPROVED'
            ELSE 'REJECTED'
        END,
        CASE WHEN floor(random() * 2) = 0 THEN (SELECT id FROM employees WHERE department_id = e.department_id AND employee_id LIKE 'EMP%01' LIMIT 1) ELSE NULL END,
        CASE WHEN floor(random() * 2) = 0 THEN CURRENT_TIMESTAMP ELSE NULL END,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    FROM employees e
    WHERE floor(random() * 3) = 0 -- about 33% of employees
    LIMIT 50; -- total expenses

    -- Update some managers and the admin to have correct roles (already set in user insertion)
    -- Nothing to do here

    RAISE NOTICE 'Inserted % departments, % employees, and sample leave requests and expenses',
        array_length(dept_names, 1), emp_count;
END $$;