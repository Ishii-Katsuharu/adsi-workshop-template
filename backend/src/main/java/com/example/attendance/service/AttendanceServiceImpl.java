package com.example.attendance.service;

import com.example.attendance.domain.WorkDuration;
import com.example.attendance.domain.WorkDurationCalculator;
import com.example.attendance.dto.AttendanceModifyRequest;
import com.example.attendance.dto.AttendanceResponse;
import com.example.attendance.dto.BreakRecordResponse;
import com.example.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.dto.TodayStatusResponse;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.BreakRecord;
import com.example.attendance.entity.Employee;
import com.example.attendance.enums.ApprovalStatus;
import com.example.attendance.exception.BusinessRuleException;
import com.example.attendance.exception.ConflictException;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.BreakRecordRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private static final LocalTime DEFAULT_BREAK_START = LocalTime.of(12, 0);
    private static final LocalTime DEFAULT_BREAK_END = LocalTime.of(13, 0);
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");

    private final AttendanceRepository attendanceRepository;
    private final BreakRecordRepository breakRecordRepository;
    private final WorkDurationCalculator workDurationCalculator;
    private final EntityManager entityManager;

    public AttendanceServiceImpl(
            AttendanceRepository attendanceRepository,
            BreakRecordRepository breakRecordRepository,
            WorkDurationCalculator workDurationCalculator,
            EntityManager entityManager) {
        this.attendanceRepository = attendanceRepository;
        this.breakRecordRepository = breakRecordRepository;
        this.workDurationCalculator = workDurationCalculator;
        this.entityManager = entityManager;
    }

    @Override
    public AttendanceResponse clockIn(Long employeeId) {
        LocalDate today = LocalDate.now(JST);
        attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
            .ifPresent(a -> { throw new ConflictException("既に出勤済みです"); });

        LocalTime now = LocalTime.now(JST).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime timestamp = LocalDateTime.now(JST);

        Employee employeeRef = entityManager.getReference(Employee.class, employeeId);
        Attendance attendance = Attendance.builder()
            .employee(employeeRef)
            .date(today)
            .clockIn(now)
            .modifiedManually(false)
            .approvalStatus(ApprovalStatus.PENDING)
            .createdAt(timestamp)
            .updatedAt(timestamp)
            .build();
        attendance = attendanceRepository.save(attendance);

        BreakRecord breakRecord = BreakRecord.builder()
            .attendance(attendance)
            .startTime(DEFAULT_BREAK_START)
            .endTime(DEFAULT_BREAK_END)
            .createdAt(timestamp)
            .updatedAt(timestamp)
            .build();
        breakRecord = breakRecordRepository.save(breakRecord);

        return toResponse(attendance, breakRecord, null);
    }

    @Override
    public AttendanceResponse clockOut(Long employeeId) {
        LocalDate today = LocalDate.now(JST);
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
            .orElseThrow(() -> new ConflictException("出勤打刻がありません"));

        if (attendance.getClockOut() != null) {
            throw new ConflictException("既に退勤済みです");
        }

        LocalTime now = LocalTime.now(JST).truncatedTo(ChronoUnit.MINUTES);
        attendance.setClockOut(now);
        attendance.setUpdatedAt(LocalDateTime.now(JST));
        attendance = attendanceRepository.save(attendance);

        BreakRecord breakRecord = breakRecordRepository.findByAttendanceId(attendance.getId())
            .orElse(null);

        WorkDuration workDuration = calculateWorkDuration(attendance, breakRecord);
        return toResponse(attendance, breakRecord, workDuration);
    }

    @Override
    public AttendanceResponse modifyAttendance(Long employeeId, Long attendanceId, AttendanceModifyRequest request) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
            .orElseThrow(() -> new ResourceNotFoundException("Attendance", attendanceId));

        if (!attendance.getEmployee().getId().equals(employeeId)) {
            throw new BusinessRuleException("この勤怠を修正する権限がありません");
        }

        YearMonth currentMonth = YearMonth.now();
        YearMonth attendanceMonth = YearMonth.from(attendance.getDate());
        if (!attendanceMonth.equals(currentMonth)) {
            throw new BusinessRuleException("当月の勤怠のみ修正可能です");
        }

        validateBreakTime(request.clockIn(), request.clockOut(), request.breakStart(), request.breakEnd());

        attendance.setClockIn(request.clockIn());
        attendance.setClockOut(request.clockOut());
        attendance.setModifiedManually(true);
        attendance.setModificationReason(request.reason());
        attendance.setApprovalStatus(ApprovalStatus.PENDING);
        attendance.setUpdatedAt(LocalDateTime.now(JST));
        attendance = attendanceRepository.save(attendance);

        BreakRecord breakRecord = breakRecordRepository.findByAttendanceId(attendanceId)
            .orElseThrow(() -> new ResourceNotFoundException("BreakRecord", attendanceId));
        breakRecord.setStartTime(request.breakStart());
        breakRecord.setEndTime(request.breakEnd());
        breakRecord.setUpdatedAt(LocalDateTime.now(JST));
        breakRecord = breakRecordRepository.save(breakRecord);

        WorkDuration workDuration = calculateWorkDuration(attendance, breakRecord);
        return toResponse(attendance, breakRecord, workDuration);
    }

    @Override
    @Transactional(readOnly = true)
    public TodayStatusResponse getToday(Long employeeId) {
        LocalDate today = LocalDate.now(JST);
        var attendanceOpt = attendanceRepository.findByEmployeeIdAndDate(employeeId, today);

        if (attendanceOpt.isEmpty()) {
            return new TodayStatusResponse(TodayStatusResponse.NOT_CLOCKED_IN, null);
        }

        Attendance attendance = attendanceOpt.get();
        BreakRecord breakRecord = breakRecordRepository.findByAttendanceId(attendance.getId())
            .orElse(null);

        String status;
        WorkDuration workDuration = null;

        if (attendance.getClockOut() != null) {
            status = TodayStatusResponse.CLOCKED_OUT;
            workDuration = calculateWorkDuration(attendance, breakRecord);
        } else {
            status = TodayStatusResponse.WORKING;
            workDuration = calculateWorkDurationWithCurrentTime(attendance, breakRecord);
        }

        return new TodayStatusResponse(status, toResponse(attendance, breakRecord, workDuration));
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyAttendanceResponse getMonthly(Long employeeId, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<Attendance> attendances = attendanceRepository
            .findByEmployeeIdAndDateBetweenOrderByDateAsc(employeeId, start, end);

        int totalWorkMinutes = 0;
        int totalOvertimeMinutes = 0;
        int workDays = 0;

        List<AttendanceResponse> responses = attendances.stream().map(attendance -> {
            BreakRecord breakRecord = breakRecordRepository.findByAttendanceId(attendance.getId())
                .orElse(null);
            WorkDuration wd = null;
            if (attendance.getClockOut() != null) {
                wd = calculateWorkDuration(attendance, breakRecord);
            } else if (attendance.getClockIn() != null) {
                wd = calculateWorkDurationWithCurrentTime(attendance, breakRecord);
            }
            return toResponse(attendance, breakRecord, wd);
        }).toList();

        for (AttendanceResponse resp : responses) {
            if (resp.workDurationMinutes() != null) {
                totalWorkMinutes += resp.workDurationMinutes();
                workDays++;
            }
            if (resp.overtimeMinutes() != null) {
                totalOvertimeMinutes += resp.overtimeMinutes();
            }
        }

        return new MonthlyAttendanceResponse(
            yearMonth.toString(),
            responses,
            totalWorkMinutes,
            totalOvertimeMinutes,
            workDays
        );
    }

    @Override
    public void autoClockOutOvernight() {
        LocalDate yesterday = LocalDate.now(JST).minusDays(1);
        List<Attendance> unclosed = attendanceRepository.findByDateAndClockOutIsNull(yesterday);

        LocalTime autoClockOut = LocalTime.of(23, 59);
        for (Attendance attendance : unclosed) {
            attendance.setClockOut(autoClockOut);
            attendance.setUpdatedAt(LocalDateTime.now(JST));
            attendanceRepository.save(attendance);
            log.info("Auto clock-out for employee {} on {}", attendance.getEmployee().getId(), yesterday);
        }
    }

    private WorkDuration calculateWorkDuration(Attendance attendance, BreakRecord breakRecord) {
        if (attendance.getClockIn() == null || attendance.getClockOut() == null) {
            return null;
        }
        LocalTime breakStart = breakRecord != null ? breakRecord.getStartTime() : DEFAULT_BREAK_START;
        LocalTime breakEnd = breakRecord != null ? breakRecord.getEndTime() : DEFAULT_BREAK_END;
        return workDurationCalculator.calculate(
            attendance.getClockIn(), attendance.getClockOut(), breakStart, breakEnd);
    }

    private WorkDuration calculateWorkDurationWithCurrentTime(Attendance attendance, BreakRecord breakRecord) {
        if (attendance.getClockIn() == null) {
            return null;
        }
        LocalTime now = LocalTime.now(JST).truncatedTo(ChronoUnit.MINUTES);
        LocalTime breakStart = breakRecord != null ? breakRecord.getStartTime() : DEFAULT_BREAK_START;
        LocalTime breakEnd = breakRecord != null ? breakRecord.getEndTime() : DEFAULT_BREAK_END;
        return workDurationCalculator.calculate(
            attendance.getClockIn(), now, breakStart, breakEnd);
    }

    private void validateBreakTime(LocalTime clockIn, LocalTime clockOut, LocalTime breakStart, LocalTime breakEnd) {
        if (!breakStart.isBefore(breakEnd)) {
            throw new BusinessRuleException("休憩時間が不正です");
        }
        if (breakStart.isBefore(clockIn) || breakEnd.isAfter(clockOut)) {
            throw new BusinessRuleException("休憩時間が不正です");
        }
    }

    private AttendanceResponse toResponse(Attendance attendance, BreakRecord breakRecord, WorkDuration workDuration) {
        BreakRecordResponse breakResponse = breakRecord != null
            ? BreakRecordResponse.from(breakRecord)
            : null;
        return AttendanceResponse.from(attendance, breakResponse, workDuration);
    }
}
