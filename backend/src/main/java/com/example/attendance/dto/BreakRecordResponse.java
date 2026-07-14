package com.example.attendance.dto;

import com.example.attendance.entity.BreakRecord;

import java.time.LocalTime;

public record BreakRecordResponse(
    Long id,
    LocalTime startTime,
    LocalTime endTime
) {
    public static BreakRecordResponse from(BreakRecord entity) {
        return new BreakRecordResponse(
            entity.getId(),
            entity.getStartTime(),
            entity.getEndTime()
        );
    }
}
