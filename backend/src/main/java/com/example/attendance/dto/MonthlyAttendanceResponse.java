package com.example.attendance.dto;

import java.util.List;

public record MonthlyAttendanceResponse(
    String yearMonth,
    List<AttendanceResponse> attendances,
    int totalWorkDurationMinutes,
    int totalOvertimeMinutes,
    int workDays
) {}
