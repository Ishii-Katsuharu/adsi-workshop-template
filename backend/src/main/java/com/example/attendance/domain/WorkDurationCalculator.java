package com.example.attendance.domain;

import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Component
public class WorkDurationCalculator {

    private static final int REGULAR_MINUTES = 435;
    private static final LocalTime NIGHT_START = LocalTime.of(22, 0);
    private static final double NIGHT_MULTIPLIER = 1.25;

    public WorkDuration calculate(
            LocalTime clockIn,
            LocalTime clockOut,
            LocalTime breakStart,
            LocalTime breakEnd) {

        int totalMinutes = (int) ChronoUnit.MINUTES.between(clockIn, clockOut);
        int breakMinutes = (int) ChronoUnit.MINUTES.between(breakStart, breakEnd);
        int totalWorkMinutes = totalMinutes - breakMinutes;

        int nightWorkMinutes = calculateNightMinutes(clockIn, clockOut, breakStart, breakEnd);
        int dayWorkMinutes = totalWorkMinutes - nightWorkMinutes;

        int regularWorkMinutes = Math.min(dayWorkMinutes, REGULAR_MINUTES);
        int overtimeMinutes = Math.max(0, dayWorkMinutes - REGULAR_MINUTES);
        int nightOvertimeMinutes = (int) Math.round(nightWorkMinutes * NIGHT_MULTIPLIER);
        int totalOvertimeMinutes = overtimeMinutes + nightOvertimeMinutes;

        return new WorkDuration(
            totalWorkMinutes,
            regularWorkMinutes,
            overtimeMinutes,
            nightWorkMinutes,
            nightOvertimeMinutes,
            totalOvertimeMinutes
        );
    }

    private int calculateNightMinutes(
            LocalTime clockIn, LocalTime clockOut,
            LocalTime breakStart, LocalTime breakEnd) {

        if (!clockOut.isAfter(NIGHT_START)) {
            return 0;
        }

        LocalTime nightEffectiveStart = clockIn.isAfter(NIGHT_START) ? clockIn : NIGHT_START;
        int nightMinutes = (int) ChronoUnit.MINUTES.between(nightEffectiveStart, clockOut);

        int nightBreakOverlap = calculateOverlap(
            breakStart, breakEnd, nightEffectiveStart, clockOut);
        return nightMinutes - nightBreakOverlap;
    }

    private int calculateOverlap(
            LocalTime start1, LocalTime end1,
            LocalTime start2, LocalTime end2) {

        LocalTime overlapStart = start1.isAfter(start2) ? start1 : start2;
        LocalTime overlapEnd = end1.isBefore(end2) ? end1 : end2;

        if (overlapStart.isBefore(overlapEnd)) {
            return (int) ChronoUnit.MINUTES.between(overlapStart, overlapEnd);
        }
        return 0;
    }
}
