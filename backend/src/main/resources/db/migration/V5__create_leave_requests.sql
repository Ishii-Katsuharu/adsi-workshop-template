CREATE TABLE leave_requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lr_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE INDEX idx_lr_emp_date ON leave_requests(employee_id, date);
CREATE INDEX idx_lr_status ON leave_requests(status);
