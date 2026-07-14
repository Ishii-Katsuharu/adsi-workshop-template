package com.example.attendance.service;

import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.LoginResponse;
import com.example.attendance.exception.BusinessRuleException;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        var employee = employeeRepository.findById(request.employeeId())
            .orElseThrow(() -> new BusinessRuleException("社員IDまたはパスワードが正しくありません"));

        if (!employee.isActive()) {
            throw new BusinessRuleException("社員IDまたはパスワードが正しくありません");
        }

        if (!passwordEncoder.matches(request.password(), employee.getPassword())) {
            throw new BusinessRuleException("社員IDまたはパスワードが正しくありません");
        }

        return new LoginResponse(EmployeeResponse.from(employee));
    }

    @Override
    public EmployeeResponse getCurrentUser(Long employeeId) {
        var employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));
        return EmployeeResponse.from(employee);
    }
}
