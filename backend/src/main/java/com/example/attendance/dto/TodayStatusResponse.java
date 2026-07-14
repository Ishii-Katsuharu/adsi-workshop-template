package com.example.attendance.dto;

public record TodayStatusResponse(
    String status,
    AttendanceResponse attendance
) {
    public static final String NOT_CLOCKED_IN = "NOT_CLOCKED_IN";
    public static final String WORKING = "WORKING";
    public static final String CLOCKED_OUT = "CLOCKED_OUT";
}
