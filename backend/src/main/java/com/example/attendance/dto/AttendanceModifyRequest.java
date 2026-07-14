package com.example.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record AttendanceModifyRequest(
    @NotNull LocalTime clockIn,
    @NotNull LocalTime clockOut,
    @NotNull LocalTime breakStart,
    @NotNull LocalTime breakEnd,
    @NotBlank @Size(max = 500) String reason
) {}
