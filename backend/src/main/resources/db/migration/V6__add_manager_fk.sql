ALTER TABLE departments
    ADD CONSTRAINT fk_dept_manager FOREIGN KEY (manager_id) REFERENCES employees(id);
