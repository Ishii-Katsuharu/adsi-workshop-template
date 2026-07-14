package com.example.attendance.integration;

import com.example.attendance.dto.EmployeeCreateRequest;
import com.example.attendance.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession loginAsAdmin() throws Exception {
        var loginRequest = new LoginRequest(1L, "admin123");
        var result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();
        return (MockHttpSession) result.getRequest().getSession();
    }

    @Test
    @DisplayName("統合: ログイン → 社員登録 → 社員一覧取得")
    void loginAndCreateEmployee() throws Exception {
        MockHttpSession session = loginAsAdmin();

        var createRequest = new EmployeeCreateRequest(
            "佐藤花子", "sato@example.com", 1L, "EMPLOYEE", "エンジニア", "password123"
        );

        mockMvc.perform(post("/api/employees")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("佐藤花子"));

        mockMvc.perform(get("/api/employees").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("統合: 未認証アクセスで401が返される")
    void unauthenticatedAccess_returns401() throws Exception {
        mockMvc.perform(get("/api/employees"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("統合: 一般社員は社員管理にアクセスできない (403)")
    void employeeRoleCannotAccessEmployeeManagement() throws Exception {
        MockHttpSession adminSession = loginAsAdmin();

        var createRequest = new EmployeeCreateRequest(
            "一般社員", "employee@example.com", 1L, "EMPLOYEE", null, "password123"
        );
        mockMvc.perform(post("/api/employees")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated());

        var loginRequest = new LoginRequest(2L, "password123");
        var result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();
        MockHttpSession employeeSession = (MockHttpSession) result.getRequest().getSession();

        mockMvc.perform(get("/api/employees").session(employeeSession))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("統合: ログアウト後にアクセスできない")
    void logoutThenAccess_returns401() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(post("/api/auth/logout").session(session))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").session(session))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("統合: 現在ユーザー取得")
    void getCurrentUser() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/api/auth/me").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("管理者"))
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
