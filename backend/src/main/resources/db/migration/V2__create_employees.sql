CREATE TABLE employees (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    department_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    position VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    paid_leave_balance INT NOT NULL DEFAULT 20,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_emp_department FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE INDEX idx_emp_dept ON employees(department_id);
CREATE INDEX idx_emp_active ON employees(active);
