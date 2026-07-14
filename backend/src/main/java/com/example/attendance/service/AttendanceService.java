package com.example.attendance.service;

import com.example.attendance.dto.AttendanceModifyRequest;
import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.dto.TodayStatusResponse;

import java.time.YearMonth;

public interface AttendanceService {

    AttendanceResponse clockIn(Long employeeId);

    AttendanceResponse clockOut(Long employeeId);

    AttendanceResponse modifyAttendance(Long employeeId, Long attendanceId, AttendanceModifyRequest request);

    TodayStatusResponse getToday(Long employeeId);

    MonthlyAttendanceResponse getMonthly(Long employeeId, YearMonth yearMonth);

    void autoClockOutOvernight();
}
