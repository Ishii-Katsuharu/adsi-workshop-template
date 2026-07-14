-- 初期部署データ
INSERT INTO departments (name, version, created_at, updated_at) VALUES
('管理部', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('開発部', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('営業部', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 初期管理者ユーザー (パスワード: admin123)
INSERT INTO employees (name, email, password, department_id, role, position, active, paid_leave_balance, version, created_at, updated_at) VALUES
('管理者', 'admin@example.com', '$2a$10$WVVwJItW9BYBPXs9cK6CQekRF17zCuLTnYc.4L143i9QkkwRItZB6', 1, 'ADMIN', 'システム管理者', TRUE, 20, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
