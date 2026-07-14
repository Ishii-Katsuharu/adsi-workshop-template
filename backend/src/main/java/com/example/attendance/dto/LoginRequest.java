package com.example.attendance.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
    @NotNull Long employeeId,
    @NotNull String password
) {}
