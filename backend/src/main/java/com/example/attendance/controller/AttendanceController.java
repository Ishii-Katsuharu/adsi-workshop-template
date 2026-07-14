package com.example.attendance.controller;

import com.example.attendance.dto.AttendanceModifyRequest;
import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.dto.TodayStatusResponse;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final CurrentUserProvider currentUserProvider;

    public AttendanceController(AttendanceService attendanceService, CurrentUserProvider currentUserProvider) {
        this.attendanceService = attendanceService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceResponse> clockIn() {
        Long employeeId = currentUserProvider.getCurrentEmployeeId();
        return ResponseEntity.ok(attendanceService.clockIn(employeeId));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceResponse> clockOut() {
        Long employeeId = currentUserProvider.getCurrentEmployeeId();
        return ResponseEntity.ok(attendanceService.clockOut(employeeId));
    }

    @GetMapping("/today")
    public ResponseEntity<TodayStatusResponse> getToday() {
        Long employeeId = currentUserProvider.getCurrentEmployeeId();
        return ResponseEntity.ok(attendanceService.getToday(employeeId));
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyAttendanceResponse> getMonthly(@RequestParam String yearMonth) {
        Long employeeId = currentUserProvider.getCurrentEmployeeId();
        YearMonth ym = YearMonth.parse(yearMonth);
        return ResponseEntity.ok(attendanceService.getMonthly(employeeId, ym));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceResponse> modify(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceModifyRequest request) {
        Long employeeId = currentUserProvider.getCurrentEmployeeId();
        return ResponseEntity.ok(attendanceService.modifyAttendance(employeeId, id, request));
    }
}
