package com.example.attendance.service;

import com.example.attendance.dto.EmployeeCreateRequest;
import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.EmployeeUpdateRequest;

import java.util.List;

public interface EmployeeService {

    EmployeeResponse create(EmployeeCreateRequest request);

    EmployeeResponse update(Long id, EmployeeUpdateRequest request);

    void deactivate(Long id);

    void resetPassword(Long id, String newPassword);

    EmployeeResponse findById(Long id);

    List<EmployeeResponse> findAll();
}
