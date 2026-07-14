package com.example.attendance.repository;

import com.example.attendance.entity.BreakRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BreakRecordRepository extends JpaRepository<BreakRecord, Long> {

    Optional<BreakRecord> findByAttendanceId(Long attendanceId);
}
