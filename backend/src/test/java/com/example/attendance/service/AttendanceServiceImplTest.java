package com.example.attendance.service;

import com.example.attendance.domain.WorkDurationCalculator;
import com.example.attendance.dto.AttendanceModifyRequest;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.BreakRecord;
import com.example.attendance.entity.Employee;
import com.example.attendance.enums.ApprovalStatus;
import com.example.attendance.exception.BusinessRuleException;
import com.example.attendance.exception.ConflictException;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.BreakRecordRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private BreakRecordRepository breakRecordRepository;

    @Mock
    private EntityManager entityManager;

    private AttendanceServiceImpl service;

    private final Long employeeId = 1L;

    @BeforeEach
    void setUp() {
        service = new AttendanceServiceImpl(
            attendanceRepository,
            breakRecordRepository,
            new WorkDurationCalculator(),
            entityManager
        );
    }

    @Test
    @DisplayName("clockIn: 正常 → 出勤レコード作成 + デフォルト休憩")
    void clockIn_noExisting_createsAttendanceAndBreak() {
        when(entityManager.getReference(Employee.class, employeeId))
            .thenReturn(Employee.builder().id(employeeId).build());
        when(attendanceRepository.findByEmployeeIdAndDate(employeeId, LocalDate.now()))
            .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class)))
            .thenAnswer(invocation -> {
                Attendance a = invocation.getArgument(0);
                a.setId(100L);
                return a;
            });
        when(breakRecordRepository.save(any(BreakRecord.class)))
            .thenAnswer(invocation -> {
                BreakRecord b = invocation.getArgument(0);
                b.setId(200L);
                return b;
            });

        var result = service.clockIn(employeeId);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.clockIn()).isNotNull();
        assertThat(result.breakRecord()).isNotNull();
        assertThat(result.breakRecord().startTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(result.breakRecord().endTime()).isEqualTo(LocalTime.of(13, 0));
        verify(attendanceRepository).save(any(Attendance.class));
        verify(breakRecordRepository).save(any(BreakRecord.class));
    }

    @Test
    @DisplayName("clockIn: 二重出勤 → BusinessRuleException")
    void clockIn_alreadyClockedIn_throwsException() {
        var existing = Attendance.builder()
            .id(1L)
            .employee(Employee.builder().id(employeeId).build())
            .date(LocalDate.now())
            .clockIn(LocalTime.of(9, 15))
            .build();
        when(attendanceRepository.findByEmployeeIdAndDate(employeeId, LocalDate.now()))
            .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.clockIn(employeeId))
            .isInstanceOf(ConflictException.class)
            .hasMessage("既に出勤済みです");
    }

    @Test
    @DisplayName("clockOut: 正常 → 退勤時刻記録")
    void clockOut_working_recordsClockOut() {
        var existing = Attendance.builder()
            .id(1L)
            .employee(Employee.builder().id(employeeId).build())
            .date(LocalDate.now())
            .clockIn(LocalTime.of(9, 15))
            .approvalStatus(ApprovalStatus.PENDING)
            .build();
        var breakRecord = BreakRecord.builder()
            .id(10L)
            .attendance(existing)
            .startTime(LocalTime.of(12, 0))
            .endTime(LocalTime.of(13, 0))
            .build();

        when(attendanceRepository.findByEmployeeIdAndDate(employeeId, LocalDate.now()))
            .thenReturn(Optional.of(existing));
        when(attendanceRepository.save(any(Attendance.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(breakRecordRepository.findByAttendanceId(1L))
            .thenReturn(Optional.of(breakRecord));

        var result = service.clockOut(employeeId);

        assertThat(result.clockOut()).isNotNull();
        assertThat(result.workDurationMinutes()).isNotNull();
    }

    @Test
    @DisplayName("clockOut: 未出勤 → BusinessRuleException")
    void clockOut_notClockedIn_throwsException() {
        when(attendanceRepository.findByEmployeeIdAndDate(employeeId, LocalDate.now()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.clockOut(employeeId))
            .isInstanceOf(ConflictException.class)
            .hasMessage("出勤打刻がありません");
    }

    @Test
    @DisplayName("clockOut: 二重退勤 → BusinessRuleException")
    void clockOut_alreadyClockedOut_throwsException() {
        var existing = Attendance.builder()
            .id(1L)
            .employee(Employee.builder().id(employeeId).build())
            .date(LocalDate.now())
            .clockIn(LocalTime.of(9, 15))
            .clockOut(LocalTime.of(17, 30))
            .build();
        when(attendanceRepository.findByEmployeeIdAndDate(employeeId, LocalDate.now()))
            .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.clockOut(employeeId))
            .isInstanceOf(ConflictException.class)
            .hasMessage("既に退勤済みです");
    }

    @Test
    @DisplayName("modify: 正常 → 修正反映 + PENDING リセット")
    void modify_validRequest_updatesAttendance() {
        var today = LocalDate.now();
        var existing = Attendance.builder()
            .id(1L)
            .employee(Employee.builder().id(employeeId).build())
            .date(today)
            .clockIn(LocalTime.of(9, 15))
            .clockOut(LocalTime.of(17, 30))
            .approvalStatus(ApprovalStatus.APPROVED)
            .modifiedManually(false)
            .build();
        var breakRecord = BreakRecord.builder()
            .id(10L)
            .attendance(existing)
            .startTime(LocalTime.of(12, 0))
            .endTime(LocalTime.of(13, 0))
            .build();

        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(attendanceRepository.save(any(Attendance.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(breakRecordRepository.findByAttendanceId(1L))
            .thenReturn(Optional.of(breakRecord));
        when(breakRecordRepository.save(any(BreakRecord.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var request = new AttendanceModifyRequest(
            LocalTime.of(8, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(12, 30),
            "打刻忘れ修正"
        );

        var result = service.modifyAttendance(employeeId, 1L, request);

        assertThat(result.clockIn()).isEqualTo(LocalTime.of(8, 0));
        assertThat(result.clockOut()).isEqualTo(LocalTime.of(18, 0));
        assertThat(result.modifiedManually()).isTrue();
        assertThat(result.approvalStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("modify: 当月外 → BusinessRuleException")
    void modify_pastMonth_throwsException() {
        var lastMonth = LocalDate.now().minusMonths(1);
        var existing = Attendance.builder()
            .id(1L)
            .employee(Employee.builder().id(employeeId).build())
            .date(lastMonth)
            .clockIn(LocalTime.of(9, 15))
            .clockOut(LocalTime.of(17, 30))
            .approvalStatus(ApprovalStatus.PENDING)
            .build();

        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(existing));

        var request = new AttendanceModifyRequest(
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            "修正"
        );

        assertThatThrownBy(() -> service.modifyAttendance(employeeId, 1L, request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessage("当月の勤怠のみ修正可能です");
    }

    @Test
    @DisplayName("modify: 他人の勤怠 → BusinessRuleException")
    void modify_otherEmployee_throwsException() {
        var existing = Attendance.builder()
            .id(1L)
            .employee(Employee.builder().id(999L).build())
            .date(LocalDate.now())
            .clockIn(LocalTime.of(9, 15))
            .clockOut(LocalTime.of(17, 30))
            .approvalStatus(ApprovalStatus.PENDING)
            .build();

        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(existing));

        var request = new AttendanceModifyRequest(
            LocalTime.of(9, 0), LocalTime.of(18, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0),
            "修正"
        );

        assertThatThrownBy(() -> service.modifyAttendance(employeeId, 1L, request))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessage("この勤怠を修正する権限がありません");
    }
}
