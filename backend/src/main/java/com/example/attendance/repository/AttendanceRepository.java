package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdAndDateBetweenOrderByDateAsc(
            Long employeeId, LocalDate start, LocalDate end);

    List<Attendance> findByDateAndClockOutIsNull(LocalDate date);
}
