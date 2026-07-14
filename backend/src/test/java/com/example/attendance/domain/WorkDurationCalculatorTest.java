package com.example.attendance.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class WorkDurationCalculatorTest {

    private final WorkDurationCalculator calculator = new WorkDurationCalculator();

    @Test
    @DisplayName("所定通り勤務: 9:15-17:30, 休憩12:00-13:00 → 435分, 残業0")
    void calculate_regularHours_noOvertime() {
        var result = calculator.calculate(
            LocalTime.of(9, 15),
            LocalTime.of(17, 30),
            LocalTime.of(12, 0),
            LocalTime.of(13, 0)
        );

        assertThat(result.totalWorkMinutes()).isEqualTo(435);
        assertThat(result.regularWorkMinutes()).isEqualTo(435);
        assertThat(result.overtimeMinutes()).isEqualTo(0);
        assertThat(result.nightWorkMinutes()).isEqualTo(0);
        assertThat(result.nightOvertimeMinutes()).isEqualTo(0);
        assertThat(result.totalOvertimeMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("1時間残業: 9:15-18:30, 休憩12:00-13:00 → 495分, 残業60分")
    void calculate_oneHourOvertime_60minutes() {
        var result = calculator.calculate(
            LocalTime.of(9, 15),
            LocalTime.of(18, 30),
            LocalTime.of(12, 0),
            LocalTime.of(13, 0)
        );

        assertThat(result.totalWorkMinutes()).isEqualTo(495);
        assertThat(result.overtimeMinutes()).isEqualTo(60);
        assertThat(result.nightWorkMinutes()).isEqualTo(0);
        assertThat(result.totalOvertimeMinutes()).isEqualTo(60);
    }

    @Test
    @DisplayName("早出勤務: 8:00-17:30, 休憩12:00-13:00 → 510分, 残業75分")
    void calculate_earlyStart_overtimeIncludesEarlyMinutes() {
        var result = calculator.calculate(
            LocalTime.of(8, 0),
            LocalTime.of(17, 30),
            LocalTime.of(12, 0),
            LocalTime.of(13, 0)
        );

        assertThat(result.totalWorkMinutes()).isEqualTo(510);
        assertThat(result.totalOvertimeMinutes()).isEqualTo(75);
    }

    @Test
    @DisplayName("深夜勤務: 9:15-23:00, 休憩12:00-13:00 → 深夜60分×1.25=75分換算")
    void calculate_nightWork_multiplierApplied() {
        var result = calculator.calculate(
            LocalTime.of(9, 15),
            LocalTime.of(23, 0),
            LocalTime.of(12, 0),
            LocalTime.of(13, 0)
        );

        assertThat(result.totalWorkMinutes()).isEqualTo(765);
        assertThat(result.nightWorkMinutes()).isEqualTo(60);
        assertThat(result.nightOvertimeMinutes()).isEqualTo(75);
        // 通常残業: (765-60) - 435 = 270分, 深夜換算: 75分, 合計: 345分
        assertThat(result.overtimeMinutes()).isEqualTo(270);
        assertThat(result.totalOvertimeMinutes()).isEqualTo(345);
    }

    @Test
    @DisplayName("退勤未打刻: clockOut=null → 指定の現在時刻で仮計算")
    void calculate_noClockOut_usesCurrentTime() {
        var currentTime = LocalTime.of(15, 0);
        var result = calculator.calculate(
            LocalTime.of(9, 15),
            currentTime,
            LocalTime.of(12, 0),
            LocalTime.of(13, 0)
        );

        // 9:15-15:00 = 345分 - 休憩60分 = 285分
        assertThat(result.totalWorkMinutes()).isEqualTo(285);
        assertThat(result.totalOvertimeMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("休憩変更: 9:15-17:30, 休憩12:00-12:30 → 465分, 残業30分")
    void calculate_shorterBreak_moreWorkTime() {
        var result = calculator.calculate(
            LocalTime.of(9, 15),
            LocalTime.of(17, 30),
            LocalTime.of(12, 0),
            LocalTime.of(12, 30)
        );

        assertThat(result.totalWorkMinutes()).isEqualTo(465);
        assertThat(result.totalOvertimeMinutes()).isEqualTo(30);
    }
}
