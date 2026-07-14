INSERT INTO departments (name, version, created_at, updated_at)
SELECT 'テスト部署', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM departments WHERE name = 'テスト部署');

INSERT INTO employees (name, email, password, department_id, role, active, paid_leave_balance, version, created_at, updated_at)
SELECT 'テスト太郎', 'test@example.com', '$2a$10$dummyhash', 1, 'EMPLOYEE', true, 20, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE email = 'test@example.com');
