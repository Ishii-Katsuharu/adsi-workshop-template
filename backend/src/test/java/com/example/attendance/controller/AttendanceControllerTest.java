package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceModifyRequest;
import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.dto.BreakRecordResponse;
import com.example.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.dto.TodayStatusResponse;
import com.example.attendance.exception.ConflictException;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.CurrentUserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttendanceService attendanceService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    private final Long employeeId = 1L;

    private AttendanceResponse sampleResponse() {
        return new AttendanceResponse(
            1L, LocalDate.now(), LocalTime.of(9, 15), null,
            new BreakRecordResponse(10L, LocalTime.of(12, 0), LocalTime.of(13, 0)),
            null, null, null, false, null, "PENDING"
        );
    }

    @Test
    @DisplayName("POST /api/attendances/clock-in → 200")
    void clockIn_success_returns200() throws Exception {
        when(currentUserProvider.getCurrentEmployeeId()).thenReturn(employeeId);
        when(attendanceService.clockIn(employeeId)).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/attendances/clock-in"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.clockIn").value("09:15:00"));
    }

    @Test
    @DisplayName("POST /api/attendances/clock-in 二重出勤 → 409")
    void clockIn_duplicate_returns409() throws Exception {
        when(currentUserProvider.getCurrentEmployeeId()).thenReturn(employeeId);
        when(attendanceService.clockIn(employeeId))
            .thenThrow(new ConflictException("既に出勤済みです"));

        mockMvc.perform(post("/api/attendances/clock-in"))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/attendances/clock-out → 200")
    void clockOut_success_returns200() throws Exception {
        var response = new AttendanceResponse(
            1L, LocalDate.now(), LocalTime.of(9, 15), LocalTime.of(17, 30),
            new BreakRecordResponse(10L, LocalTime.of(12, 0), LocalTime.of(13, 0)),
            435, 0, 0, false, null, "PENDING"
        );
        when(currentUserProvider.getCurrentEmployeeId()).thenReturn(employeeId);
        when(attendanceService.clockOut(employeeId)).thenReturn(response);

        mockMvc.perform(post("/api/attendances/clock-out"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clockOut").value("17:30:00"))
            .andExpect(jsonPath("$.workDurationMinutes").value(435));
    }

    @Test
    @DisplayName("GET /api/attendances/today → 200")
    void getToday_returns200() throws Exception {
        var todayStatus = new TodayStatusResponse(TodayStatusResponse.WORKING, sampleResponse());
        when(currentUserProvider.getCurrentEmployeeId()).thenReturn(employeeId);
        when(attendanceService.getToday(employeeId)).thenReturn(todayStatus);

        mockMvc.perform(get("/api/attendances/today"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("WORKING"));
    }

    @Test
    @DisplayName("GET /api/attendances/monthly → 200")
    void getMonthly_returns200() throws Exception {
        var monthly = new MonthlyAttendanceResponse("2026-07", List.of(), 0, 0, 0);
        when(currentUserProvider.getCurrentEmployeeId()).thenReturn(employeeId);
        when(attendanceService.getMonthly(eq(employeeId), any(YearMonth.class))).thenReturn(monthly);

        mockMvc.perform(get("/api/attendances/monthly").param("yearMonth", "2026-07"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.yearMonth").value("2026-07"));
    }

    @Test
    @DisplayName("PUT /api/attendances/{id} → 200")
    void modify_validRequest_returns200() throws Exception {
        var request = new AttendanceModifyRequest(
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            "打刻修正"
        );
        var response = new AttendanceResponse(
            1L, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(18, 0),
            new BreakRecordResponse(10L, LocalTime.of(12, 0), LocalTime.of(13, 0)),
            480, 45, 0, true, "打刻修正", "PENDING"
        );
        when(currentUserProvider.getCurrentEmployeeId()).thenReturn(employeeId);
        when(attendanceService.modifyAttendance(eq(employeeId), eq(1L), any(AttendanceModifyRequest.class)))
            .thenReturn(response);

        mockMvc.perform(put("/api/attendances/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.modifiedManually").value(true));
    }

    @Test
    @DisplayName("PUT /api/attendances/{id} バリデーションエラー → 400")
    void modify_missingReason_returns400() throws Exception {
        var request = new AttendanceModifyRequest(
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            ""
        );
        when(currentUserProvider.getCurrentEmployeeId()).thenReturn(employeeId);

        mockMvc.perform(put("/api/attendances/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
