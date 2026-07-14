package com.example.attendance.controller;

import com.example.attendance.config.SecurityConfig;
import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.LoginResponse;
import com.example.attendance.exception.BusinessRuleException;
import com.example.attendance.exception.GlobalExceptionHandler;
import com.example.attendance.service.AuthService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("ログイン: 正しい認証情報で200が返される")
    void login_validCredentials_returns200() throws Exception {
        var employeeResp = new EmployeeResponse(1L, "田中太郎", "tanaka@example.com", 1L, "開発部", "ADMIN", "エンジニア", true, 20);
        var loginResp = new LoginResponse(employeeResp);
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResp);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(1L, "password123"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.employee.name").value("田中太郎"));
    }

    @Test
    @DisplayName("ログイン: 認証失敗で400が返される")
    void login_invalidCredentials_returns400() throws Exception {
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new BusinessRuleException("社員IDまたはパスワードが正しくありません"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest(1L, "wrong"))))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("ログアウト: セッションが無効化され204が返される")
    void logout_authenticated_returns204() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("employeeId", 1L);
        session.setAttribute("employeeRole", "ADMIN");

        mockMvc.perform(post("/api/auth/logout").session(session))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("現在ユーザー: セッションがある場合に200が返される")
    void me_withSession_returns200() throws Exception {
        var employeeResp = new EmployeeResponse(1L, "田中太郎", "tanaka@example.com", 1L, "開発部", "ADMIN", "エンジニア", true, 20);
        when(authService.getCurrentUser(1L)).thenReturn(employeeResp);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("employeeId", 1L);
        session.setAttribute("employeeRole", "ADMIN");

        mockMvc.perform(get("/api/auth/me").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("田中太郎"));
    }

    @Test
    @DisplayName("現在ユーザー: セッションがない場合に401が返される")
    void me_withoutSession_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }
}
