CREATE TABLE break_records (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    attendance_id BIGINT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_br_attendance FOREIGN KEY (attendance_id) REFERENCES attendances(id)
);
