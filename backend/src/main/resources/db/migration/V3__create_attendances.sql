CREATE TABLE attendances (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    date DATE NOT NULL,
    clock_in TIME,
    clock_out TIME,
    modified_manually BOOLEAN NOT NULL DEFAULT FALSE,
    modification_reason VARCHAR(500),
    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_att_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT uq_att_emp_date UNIQUE (employee_id, date)
);

CREATE INDEX idx_att_emp_date ON attendances(employee_id, date);
CREATE INDEX idx_att_date_status ON attendances(date, approval_status);
