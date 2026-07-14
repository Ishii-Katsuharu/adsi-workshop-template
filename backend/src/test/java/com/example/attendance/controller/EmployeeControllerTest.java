package com.example.attendance.controller;

import com.example.attendance.config.SecurityConfig;
import com.example.attendance.dto.EmployeeCreateRequest;
import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.EmployeeUpdateRequest;
import com.example.attendance.dto.PasswordResetRequest;
import com.example.attendance.exception.GlobalExceptionHandler;
import com.example.attendance.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    private MockHttpSession adminSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("employeeId", 1L);
        session.setAttribute("employeeRole", "ADMIN");
        return session;
    }

    private MockHttpSession employeeSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("employeeId", 2L);
        session.setAttribute("employeeRole", "EMPLOYEE");
        return session;
    }

    @Test
    @DisplayName("社員一覧: ADMINロールで200が返される")
    void findAll_asAdmin_returns200() throws Exception {
        var emp = new EmployeeResponse(1L, "田中太郎", "tanaka@example.com", 1L, "開発部", "ADMIN", "エンジニア", true, 20);
        when(employeeService.findAll()).thenReturn(List.of(emp));

        mockMvc.perform(get("/api/employees").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("田中太郎"));
    }

    @Test
    @DisplayName("社員一覧: EMPLOYEEロールで403が返される")
    void findAll_asEmployee_returns403() throws Exception {
        mockMvc.perform(get("/api/employees").session(employeeSession()))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("社員一覧: 未認証で401が返される")
    void findAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/employees"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("社員登録: 有効なリクエストで201が返される")
    void create_validRequest_returns201() throws Exception {
        var request = new EmployeeCreateRequest("佐藤花子", "sato@example.com", 1L, "EMPLOYEE", "デザイナー", "password123");
        var response = new EmployeeResponse(2L, "佐藤花子", "sato@example.com", 1L, "開発部", "EMPLOYEE", "デザイナー", true, 20);
        when(employeeService.create(any(EmployeeCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/employees")
                .session(adminSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("佐藤花子"));
    }

    @Test
    @DisplayName("社員詳細: ADMINロールで200が返される")
    void findById_asAdmin_returns200() throws Exception {
        var response = new EmployeeResponse(1L, "田中太郎", "tanaka@example.com", 1L, "開発部", "ADMIN", "エンジニア", true, 20);
        when(employeeService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/employees/1").session(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("田中太郎"));
    }

    @Test
    @DisplayName("社員編集: 有効なリクエストで200が返される")
    void update_validRequest_returns200() throws Exception {
        var request = new EmployeeUpdateRequest("田中次郎", "jiro@example.com", 1L, "MANAGER", "リーダー");
        var response = new EmployeeResponse(1L, "田中次郎", "jiro@example.com", 1L, "開発部", "MANAGER", "リーダー", true, 20);
        when(employeeService.update(eq(1L), any(EmployeeUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/employees/1")
                .session(adminSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("田中次郎"));
    }

    @Test
    @DisplayName("社員無効化: 204が返される")
    void deactivate_asAdmin_returns204() throws Exception {
        doNothing().when(employeeService).deactivate(1L);

        mockMvc.perform(post("/api/employees/1/deactivate").session(adminSession()))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("パスワードリセット: 204が返される")
    void resetPassword_asAdmin_returns204() throws Exception {
        doNothing().when(employeeService).resetPassword(1L, "newPass123");
        var request = new PasswordResetRequest("newPass123");

        mockMvc.perform(post("/api/employees/1/reset-password")
                .session(adminSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());
    }
}
