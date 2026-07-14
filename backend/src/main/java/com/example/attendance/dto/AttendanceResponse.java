package com.example.attendance.dto;

import com.example.attendance.domain.WorkDuration;
import com.example.attendance.entity.Attendance;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceResponse(
    Long id,
    LocalDate date,
    LocalTime clockIn,
    LocalTime clockOut,
    BreakRecordResponse breakRecord,
    Integer workDurationMinutes,
    Integer overtimeMinutes,
    Integer nightOvertimeMinutes,
    boolean modifiedManually,
    String modificationReason,
    String approvalStatus
) {
    public static AttendanceResponse from(
            Attendance entity,
            BreakRecordResponse breakRecord,
            WorkDuration workDuration) {

        return new AttendanceResponse(
            entity.getId(),
            entity.getDate(),
            entity.getClockIn(),
            entity.getClockOut(),
            breakRecord,
            workDuration != null ? workDuration.totalWorkMinutes() : null,
            workDuration != null ? workDuration.totalOvertimeMinutes() : null,
            workDuration != null ? workDuration.nightOvertimeMinutes() : null,
            entity.isModifiedManually(),
            entity.getModificationReason(),
            entity.getApprovalStatus().name()
        );
    }
}
