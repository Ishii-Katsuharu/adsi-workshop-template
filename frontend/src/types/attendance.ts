export interface BreakRecordResponse {
  id: number;
  startTime: string;
  endTime: string;
}

export interface AttendanceResponse {
  id: number;
  date: string;
  clockIn: string | null;
  clockOut: string | null;
  breakRecord: BreakRecordResponse | null;
  workDurationMinutes: number | null;
  overtimeMinutes: number | null;
  nightOvertimeMinutes: number | null;
  modifiedManually: boolean;
  modificationReason: string | null;
  approvalStatus: "PENDING" | "APPROVED" | "REJECTED";
}

export interface TodayStatusResponse {
  status: "NOT_CLOCKED_IN" | "WORKING" | "CLOCKED_OUT";
  attendance: AttendanceResponse | null;
}

export interface MonthlyAttendanceResponse {
  yearMonth: string;
  attendances: AttendanceResponse[];
  totalWorkDurationMinutes: number;
  totalOvertimeMinutes: number;
  workDays: number;
}

export interface AttendanceModifyRequest {
  clockIn: string;
  clockOut: string;
  breakStart: string;
  breakEnd: string;
  reason: string;
}
