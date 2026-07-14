package com.example.attendance.service;

import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    EmployeeResponse getCurrentUser(Long employeeId);
}
