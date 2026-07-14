package com.example.attendance.domain;

public record WorkDuration(
    int totalWorkMinutes,
    int regularWorkMinutes,
    int overtimeMinutes,
    int nightWorkMinutes,
    int nightOvertimeMinutes,
    int totalOvertimeMinutes
) {}
