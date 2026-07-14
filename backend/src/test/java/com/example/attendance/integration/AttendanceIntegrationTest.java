package com.example.attendance.integration;

import com.example.attendance.dto.AttendanceModifyRequest;
import com.example.attendance.service.StubCurrentUserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class AttendanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private StubCurrentUserProvider stubCurrentUserProvider;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery(
            "INSERT INTO departments (name, version, created_at, updated_at) " +
            "VALUES ('テスト部署', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
            .executeUpdate();
        entityManager.createNativeQuery(
            "INSERT INTO employees (name, email, password, department_id, role, active, paid_leave_balance, version, created_at, updated_at) " +
            "VALUES ('テスト太郎', 'test@example.com', '$2a$10$dummy', " +
            "(SELECT id FROM departments WHERE name = 'テスト部署'), " +
            "'EMPLOYEE', true, 20, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)")
            .executeUpdate();
        entityManager.flush();

        Long empId = ((Number) entityManager.createNativeQuery(
            "SELECT id FROM employees WHERE email = 'test@example.com'")
            .getSingleResult()).longValue();
        stubCurrentUserProvider.setEmployeeId(empId);
    }

    @Test
    @DisplayName("出勤 → 退勤 → 月次一覧の一連フロー")
    void fullFlow_clockIn_clockOut_monthly() throws Exception {
        // 出勤
        mockMvc.perform(post("/api/attendances/clock-in"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clockIn").exists())
            .andExpect(jsonPath("$.breakRecord.startTime").value("12:00"))
            .andExpect(jsonPath("$.breakRecord.endTime").value("13:00"));

        // 退勤
        mockMvc.perform(post("/api/attendances/clock-out"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clockOut").exists())
            .andExpect(jsonPath("$.workDurationMinutes").isNumber());

        // 今日のステータス
        mockMvc.perform(get("/api/attendances/today"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CLOCKED_OUT"));

        // 月次一覧
        String yearMonth = java.time.YearMonth.now().toString();
        mockMvc.perform(get("/api/attendances/monthly").param("yearMonth", yearMonth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.workDays").value(1));
    }

    @Test
    @DisplayName("二重出勤 → 409 Conflict")
    void clockIn_twice_returns409() throws Exception {
        mockMvc.perform(post("/api/attendances/clock-in"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/attendances/clock-in"))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("出勤 → 修正 → 承認ステータスがPENDINGのまま")
    void modify_afterClockIn_resetsToPending() throws Exception {
        mockMvc.perform(post("/api/attendances/clock-in"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/attendances/clock-out"))
            .andExpect(status().isOk());

        // ID 取得のため today を呼ぶ
        var todayResult = mockMvc.perform(get("/api/attendances/today"))
            .andExpect(status().isOk())
            .andReturn();

        String responseBody = todayResult.getResponse().getContentAsString();
        Long attendanceId = objectMapper.readTree(responseBody).get("attendance").get("id").asLong();

        var request = new AttendanceModifyRequest(
            LocalTime.of(8, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            "打刻修正テスト"
        );

        mockMvc.perform(put("/api/attendances/" + attendanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.modifiedManually").value(true))
            .andExpect(jsonPath("$.approvalStatus").value("PENDING"))
            .andExpect(jsonPath("$.clockIn").value("08:00"))
            .andExpect(jsonPath("$.clockOut").value("18:00"));
    }
}
