package com.example.attendance.service;

import org.springframework.stereotype.Component;

@Component
public class StubCurrentUserProvider implements CurrentUserProvider {

    private Long employeeId = 1L;

    @Override
    public Long getCurrentEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
}
