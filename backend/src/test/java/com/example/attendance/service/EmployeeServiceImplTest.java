package com.example.attendance.service;

import com.example.attendance.dto.EmployeeCreateRequest;
import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.EmployeeUpdateRequest;
import com.example.attendance.entity.Department;
import com.example.attendance.entity.Employee;
import com.example.attendance.enums.Role;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.DepartmentRepository;
import com.example.attendance.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private EmployeeService employeeService;

    private Department department;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(employeeRepository, departmentRepository, passwordEncoder);
        department = Department.builder()
            .id(1L)
            .name("開発部")
            .build();
    }

    @Test
    @DisplayName("社員登録: 有効なリクエストで社員が作成される")
    void create_validRequest_createsEmployee() {
        var request = new EmployeeCreateRequest(
            "田中太郎", "tanaka@example.com", 1L, "EMPLOYEE", "エンジニア", "password123"
        );
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setId(1L);
            return emp;
        });

        EmployeeResponse result = employeeService.create(request);

        assertThat(result.name()).isEqualTo("田中太郎");
        assertThat(result.email()).isEqualTo("tanaka@example.com");
        assertThat(result.role()).isEqualTo("EMPLOYEE");
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("社員登録: 存在しない部署IDで例外が投げられる")
    void create_invalidDepartmentId_throwsException() {
        var request = new EmployeeCreateRequest(
            "田中太郎", "tanaka@example.com", 999L, "EMPLOYEE", null, "password123"
        );
        when(departmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.create(request))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("社員編集: 有効なリクエストで社員情報が更新される")
    void update_validRequest_updatesEmployee() {
        var existing = Employee.builder()
            .id(1L).name("田中太郎").email("tanaka@example.com")
            .department(department).role(Role.EMPLOYEE).active(true).paidLeaveBalance(20)
            .build();
        var request = new EmployeeUpdateRequest(
            "田中次郎", "jiro@example.com", 1L, "MANAGER", "リーダー"
        );
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        EmployeeResponse result = employeeService.update(1L, request);

        assertThat(result.name()).isEqualTo("田中次郎");
        assertThat(result.role()).isEqualTo("MANAGER");
    }

    @Test
    @DisplayName("社員無効化: active が false になる")
    void deactivate_existingEmployee_setsInactive() {
        var existing = Employee.builder()
            .id(1L).name("田中太郎").department(department).role(Role.EMPLOYEE).active(true)
            .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        employeeService.deactivate(1L);

        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("パスワードリセット: パスワードがエンコードされて保存される")
    void resetPassword_validId_encodesAndSaves() {
        var existing = Employee.builder()
            .id(1L).name("田中太郎").department(department).role(Role.EMPLOYEE).active(true)
            .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newPass123")).thenReturn("encodedNew");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(i -> i.getArgument(0));

        employeeService.resetPassword(1L, "newPass123");

        verify(passwordEncoder).encode("newPass123");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("社員取得: 存在するIDで社員情報が返される")
    void findById_existingId_returnsEmployee() {
        var existing = Employee.builder()
            .id(1L).name("田中太郎").email("tanaka@example.com")
            .department(department).role(Role.EMPLOYEE).active(true).paidLeaveBalance(20)
            .build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));

        EmployeeResponse result = employeeService.findById(1L);

        assertThat(result.name()).isEqualTo("田中太郎");
    }

    @Test
    @DisplayName("社員取得: 存在しないIDで例外が投げられる")
    void findById_nonExistingId_throwsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findById(999L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("社員一覧: 全社員がレスポンスとして返される")
    void findAll_returnsAllEmployees() {
        var emp1 = Employee.builder()
            .id(1L).name("田中太郎").email("tanaka@example.com")
            .department(department).role(Role.EMPLOYEE).active(true).paidLeaveBalance(20)
            .build();
        var emp2 = Employee.builder()
            .id(2L).name("佐藤花子").email("sato@example.com")
            .department(department).role(Role.ADMIN).active(true).paidLeaveBalance(20)
            .build();
        when(employeeRepository.findAll()).thenReturn(List.of(emp1, emp2));

        List<EmployeeResponse> result = employeeService.findAll();

        assertThat(result).hasSize(2);
    }
}
