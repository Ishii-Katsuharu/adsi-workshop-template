"use client";

import { useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { clockIn, clockOut, getToday, getMonthly } from "@/lib/attendance-api";
import type { TodayStatusResponse, MonthlyAttendanceResponse } from "@/types/attendance";
import { ApiError } from "@/lib/api-client";

function formatMinutes(minutes: number | null): string {
  if (minutes === null || minutes === undefined) return "-";
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}h ${m}m`;
}

function formatTime(time: string | null): string {
  if (!time) return "-";
  return time.slice(0, 5);
}

export default function DashboardPage() {
  const { user, loading: authLoading } = useAuth();
  const router = useRouter();
  const [todayStatus, setTodayStatus] = useState<TodayStatusResponse | null>(null);
  const [monthly, setMonthly] = useState<MonthlyAttendanceResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const currentYearMonth = new Date().toISOString().slice(0, 7);

  useEffect(() => {
    if (!authLoading && !user) {
      router.push("/login");
    }
  }, [user, authLoading, router]);

  const fetchData = useCallback(async () => {
    try {
      setError(null);
      const [today, monthlyData] = await Promise.all([
        getToday(),
        getMonthly(currentYearMonth),
      ]);
      setTodayStatus(today);
      setMonthly(monthlyData);
    } catch (e) {
      if (e instanceof ApiError) {
        setError(e.message);
      } else {
        setError("データの取得に失敗しました");
      }
    } finally {
      setLoading(false);
    }
  }, [currentYearMonth]);

  useEffect(() => {
    if (user) {
      fetchData();
    }
  }, [user, fetchData]);

  async function handleClockIn() {
    try {
      setError(null);
      await clockIn();
      await fetchData();
    } catch (e) {
      if (e instanceof ApiError) {
        setError(e.message);
      }
    }
  }

  async function handleClockOut() {
    try {
      setError(null);
      await clockOut();
      await fetchData();
    } catch (e) {
      if (e instanceof ApiError) {
        setError(e.message);
      }
    }
  }

  if (authLoading || loading) {
    return <div className="text-gray-500">読み込み中...</div>;
  }

  if (!user) return null;

  const status = todayStatus?.status ?? "NOT_CLOCKED_IN";
  const attendance = todayStatus?.attendance;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">ダッシュボード</h1>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {/* 打刻セクション */}
      <section className="bg-white border border-gray-200 rounded-lg p-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">今日の勤務</h2>

        <div className="flex items-center gap-4 mb-4">
          <StatusBadge status={status} />
          {status === "NOT_CLOCKED_IN" && (
            <button
              onClick={handleClockIn}
              className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 font-medium"
            >
              出勤
            </button>
          )}
          {status === "WORKING" && (
            <button
              onClick={handleClockOut}
              className="bg-orange-600 text-white px-6 py-2 rounded-md hover:bg-orange-700 font-medium"
            >
              退勤
            </button>
          )}
        </div>

        {attendance && (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
            <div>
              <span className="text-gray-500">出勤</span>
              <p className="font-medium">{formatTime(attendance.clockIn)}</p>
            </div>
            <div>
              <span className="text-gray-500">退勤</span>
              <p className="font-medium">{formatTime(attendance.clockOut)}</p>
            </div>
            <div>
              <span className="text-gray-500">休憩</span>
              <p className="font-medium">
                {attendance.breakRecord
                  ? `${formatTime(attendance.breakRecord.startTime)}-${formatTime(attendance.breakRecord.endTime)}`
                  : "-"}
              </p>
            </div>
            <div>
              <span className="text-gray-500">勤務時間</span>
              <p className="font-medium">{formatMinutes(attendance.workDurationMinutes)}</p>
            </div>
          </div>
        )}
      </section>

      {/* 当月サマリー */}
      {monthly && (
        <section className="bg-white border border-gray-200 rounded-lg p-6">
          <h2 className="text-lg font-semibold text-gray-800 mb-4">
            当月サマリー（{monthly.yearMonth}）
          </h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <SummaryCard label="出勤日数" value={`${monthly.workDays}日`} />
            <SummaryCard label="総勤務時間" value={formatMinutes(monthly.totalWorkDurationMinutes)} />
            <SummaryCard label="総残業時間" value={formatMinutes(monthly.totalOvertimeMinutes)} />
            <SummaryCard label="有給取得" value="-" />
          </div>
        </section>
      )}
    </div>
  );
}

function StatusBadge({ status }: { status: string }) {
  const config = {
    NOT_CLOCKED_IN: { label: "未出勤", color: "bg-gray-100 text-gray-700" },
    WORKING: { label: "勤務中", color: "bg-green-100 text-green-700" },
    CLOCKED_OUT: { label: "退勤済", color: "bg-blue-100 text-blue-700" },
  }[status] ?? { label: status, color: "bg-gray-100 text-gray-700" };

  return (
    <span className={`px-3 py-1 rounded-full text-sm font-medium ${config.color}`}>
      {config.label}
    </span>
  );
}

function SummaryCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="bg-gray-50 rounded-lg p-4">
      <p className="text-sm text-gray-500">{label}</p>
      <p className="text-xl font-semibold text-gray-900">{value}</p>
    </div>
  );
}
