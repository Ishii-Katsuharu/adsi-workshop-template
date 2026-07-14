package com.example.attendance.dto;

import com.example.attendance.entity.Employee;

public record EmployeeResponse(
    Long id,
    String name,
    String email,
    Long departmentId,
    String departmentName,
    String role,
    String position,
    boolean active,
    int paidLeaveBalance
) {
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
            employee.getId(),
            employee.getName(),
            employee.getEmail(),
            employee.getDepartment().getId(),
            employee.getDepartment().getName(),
            employee.getRole().name(),
            employee.getPosition(),
            employee.isActive(),
            employee.getPaidLeaveBalance()
        );
    }
}
