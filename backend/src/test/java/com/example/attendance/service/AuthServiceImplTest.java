package com.example.attendance.service;

import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.LoginResponse;
import com.example.attendance.entity.Department;
import com.example.attendance.entity.Employee;
import com.example.attendance.enums.Role;
import com.example.attendance.exception.BusinessRuleException;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    private Department department;
    private Employee employee;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(employeeRepository, passwordEncoder);
        department = Department.builder().id(1L).name("開発部").build();
        employee = Employee.builder()
            .id(1L).name("田中太郎").email("tanaka@example.com")
            .password("encodedPassword").department(department)
            .role(Role.EMPLOYEE).active(true).paidLeaveBalance(20)
            .build();
    }

    @Test
    @DisplayName("ログイン: 正しい認証情報でログインレスポンスが返される")
    void login_validCredentials_returnsLoginResponse() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        LoginResponse result = authService.login(new LoginRequest(1L, "password123"));

        assertThat(result.employee().name()).isEqualTo("田中太郎");
    }

    @Test
    @DisplayName("ログイン: 存在しない社員IDで例外が投げられる")
    void login_invalidEmployeeId_throwsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest(999L, "password")))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("ログイン: パスワード不一致で例外が投げられる")
    void login_wrongPassword_throwsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest(1L, "wrongPassword")))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("ログイン: 無効化された社員で例外が投げられる")
    void login_inactiveEmployee_throwsException() {
        employee.setActive(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> authService.login(new LoginRequest(1L, "password123")))
            .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("現在ユーザー取得: 有効なIDで社員情報が返される")
    void getCurrentUser_validId_returnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        var result = authService.getCurrentUser(1L);

        assertThat(result.name()).isEqualTo("田中太郎");
    }

    @Test
    @DisplayName("現在ユーザー取得: 存在しないIDで例外が投げられる")
    void getCurrentUser_invalidId_throwsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
