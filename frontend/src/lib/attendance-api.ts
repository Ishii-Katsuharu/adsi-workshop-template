import { apiFetch } from "./api-client";
import type {
  AttendanceModifyRequest,
  AttendanceResponse,
  MonthlyAttendanceResponse,
  TodayStatusResponse,
} from "@/types/attendance";

export function clockIn(): Promise<AttendanceResponse> {
  return apiFetch<AttendanceResponse>("/attendances/clock-in", {
    method: "POST",
  });
}

export function clockOut(): Promise<AttendanceResponse> {
  return apiFetch<AttendanceResponse>("/attendances/clock-out", {
    method: "POST",
  });
}

export function getToday(): Promise<TodayStatusResponse> {
  return apiFetch<TodayStatusResponse>("/attendances/today");
}

export function getMonthly(yearMonth: string): Promise<MonthlyAttendanceResponse> {
  return apiFetch<MonthlyAttendanceResponse>(`/attendances/monthly?yearMonth=${yearMonth}`);
}

export function modifyAttendance(
  id: number,
  request: AttendanceModifyRequest
): Promise<AttendanceResponse> {
  return apiFetch<AttendanceResponse>(`/attendances/${id}`, {
    method: "PUT",
    body: JSON.stringify(request),
  });
}
