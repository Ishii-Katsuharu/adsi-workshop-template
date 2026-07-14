package com.example.attendance.service;

import com.example.attendance.dto.EmployeeCreateRequest;
import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.EmployeeUpdateRequest;
import com.example.attendance.entity.Employee;
import com.example.attendance.enums.Role;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.DepartmentRepository;
import com.example.attendance.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EmployeeResponse create(EmployeeCreateRequest request) {
        var department = departmentRepository.findById(request.departmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId()));

        var now = LocalDateTime.now();
        var employee = Employee.builder()
            .name(request.name())
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .department(department)
            .role(Role.valueOf(request.role()))
            .position(request.position())
            .active(true)
            .paidLeaveBalance(20)
            .createdAt(now)
            .updatedAt(now)
            .build();

        var saved = employeeRepository.save(employee);
        return EmployeeResponse.from(saved);
    }

    @Override
    public EmployeeResponse update(Long id, EmployeeUpdateRequest request) {
        var employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", id));

        var department = departmentRepository.findById(request.departmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId()));

        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setDepartment(department);
        employee.setRole(Role.valueOf(request.role()));
        employee.setPosition(request.position());
        employee.setUpdatedAt(LocalDateTime.now());

        var saved = employeeRepository.save(employee);
        return EmployeeResponse.from(saved);
    }

    @Override
    public void deactivate(Long id) {
        var employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", id));

        employee.setActive(false);
        employee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(employee);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        var employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", id));

        employee.setPassword(passwordEncoder.encode(newPassword));
        employee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        var employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        return EmployeeResponse.from(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream()
            .map(EmployeeResponse::from)
            .toList();
    }
}
