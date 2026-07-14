"use client";

import { useEffect, useState, useCallback } from "react";
import { getMonthly, modifyAttendance } from "@/lib/attendance-api";
import type { MonthlyAttendanceResponse, AttendanceResponse, AttendanceModifyRequest } from "@/types/attendance";
import { ApiError } from "@/lib/api-client";

function formatMinutes(minutes: number | null): string {
  if (minutes === null || minutes === undefined) return "-";
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}:${String(m).padStart(2, "0")}`;
}

function formatTime(time: string | null): string {
  if (!time) return "-";
  return time.slice(0, 5);
}

function getCurrentYearMonth(): string {
  return new Date().toISOString().slice(0, 7);
}

export default function AttendancePage() {
  const [yearMonth, setYearMonth] = useState(getCurrentYearMonth);
  const [data, setData] = useState<MonthlyAttendanceResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editTarget, setEditTarget] = useState<AttendanceResponse | null>(null);

  const fetchData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await getMonthly(yearMonth);
      setData(result);
    } catch (e) {
      if (e instanceof ApiError) {
        setError(e.message);
      } else {
        setError("データの取得に失敗しました");
      }
    } finally {
      setLoading(false);
    }
  }, [yearMonth]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  function handlePrevMonth() {
    const [y, m] = yearMonth.split("-").map(Number);
    const prev = m === 1 ? `${y - 1}-12` : `${y}-${String(m - 1).padStart(2, "0")}`;
    setYearMonth(prev);
  }

  function handleNextMonth() {
    const [y, m] = yearMonth.split("-").map(Number);
    const next = m === 12 ? `${y + 1}-01` : `${y}-${String(m + 1).padStart(2, "0")}`;
    setYearMonth(next);
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">勤怠一覧</h1>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {/* 年月セレクタ */}
      <div className="flex items-center gap-4">
        <button onClick={handlePrevMonth} className="px-3 py-1 border rounded hover:bg-gray-50">
          ← 前月
        </button>
        <span className="text-lg font-medium">{yearMonth}</span>
        <button onClick={handleNextMonth} className="px-3 py-1 border rounded hover:bg-gray-50">
          翌月 →
        </button>
      </div>

      {loading ? (
        <div className="text-gray-500">読み込み中...</div>
      ) : data ? (
        <>
          {/* テーブル */}
          <div className="overflow-x-auto">
            <table className="w-full text-sm border-collapse">
              <thead>
                <tr className="bg-gray-50 border-b">
                  <th className="text-left px-3 py-2">日付</th>
                  <th className="text-left px-3 py-2">出勤</th>
                  <th className="text-left px-3 py-2">退勤</th>
                  <th className="text-left px-3 py-2">休憩</th>
                  <th className="text-left px-3 py-2">勤務</th>
                  <th className="text-left px-3 py-2">残業</th>
                  <th className="text-left px-3 py-2">状態</th>
                  <th className="text-left px-3 py-2">操作</th>
                </tr>
              </thead>
              <tbody>
                {data.attendances.map((att) => (
                  <tr key={att.id} className="border-b hover:bg-gray-50">
                    <td className="px-3 py-2">{att.date}</td>
                    <td className="px-3 py-2">{formatTime(att.clockIn)}</td>
                    <td className="px-3 py-2">{formatTime(att.clockOut)}</td>
                    <td className="px-3 py-2">
                      {att.breakRecord
                        ? `${formatTime(att.breakRecord.startTime)}-${formatTime(att.breakRecord.endTime)}`
                        : "-"}
                    </td>
                    <td className="px-3 py-2">{formatMinutes(att.workDurationMinutes)}</td>
                    <td className="px-3 py-2">{formatMinutes(att.overtimeMinutes)}</td>
                    <td className="px-3 py-2">
                      <ApprovalBadge status={att.approvalStatus} modified={att.modifiedManually} />
                    </td>
                    <td className="px-3 py-2">
                      {isCurrentMonth(att.date) && (
                        <button
                          onClick={() => setEditTarget(att)}
                          className="text-blue-600 hover:underline text-xs"
                        >
                          修正
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr className="bg-gray-50 font-medium">
                  <td className="px-3 py-2">合計</td>
                  <td className="px-3 py-2" colSpan={3}>{data.workDays}日</td>
                  <td className="px-3 py-2">{formatMinutes(data.totalWorkDurationMinutes)}</td>
                  <td className="px-3 py-2">{formatMinutes(data.totalOvertimeMinutes)}</td>
                  <td className="px-3 py-2" colSpan={2}></td>
                </tr>
              </tfoot>
            </table>
          </div>

          {/* 修正ダイアログ */}
          {editTarget && (
            <ModifyDialog
              attendance={editTarget}
              onClose={() => setEditTarget(null)}
              onSaved={() => {
                setEditTarget(null);
                fetchData();
              }}
            />
          )}
        </>
      ) : null}
    </div>
  );
}

function isCurrentMonth(date: string): boolean {
  return date.slice(0, 7) === getCurrentYearMonth();
}

function ApprovalBadge({ status, modified }: { status: string; modified: boolean }) {
  const config = {
    PENDING: { label: "未承認", color: "bg-yellow-100 text-yellow-700" },
    APPROVED: { label: "承認済", color: "bg-green-100 text-green-700" },
    REJECTED: { label: "差戻し", color: "bg-red-100 text-red-700" },
  }[status] ?? { label: status, color: "bg-gray-100 text-gray-700" };

  return (
    <span className="flex items-center gap-1">
      <span className={`px-2 py-0.5 rounded text-xs font-medium ${config.color}`}>
        {config.label}
      </span>
      {modified && <span className="text-xs text-orange-500" title="手動修正">✎</span>}
    </span>
  );
}

interface ModifyDialogProps {
  attendance: AttendanceResponse;
  onClose: () => void;
  onSaved: () => void;
}

function ModifyDialog({ attendance, onClose, onSaved }: ModifyDialogProps) {
  const [clockIn, setClockIn] = useState(attendance.clockIn?.slice(0, 5) ?? "");
  const [clockOut, setClockOut] = useState(attendance.clockOut?.slice(0, 5) ?? "");
  const [breakStart, setBreakStart] = useState(attendance.breakRecord?.startTime.slice(0, 5) ?? "12:00");
  const [breakEnd, setBreakEnd] = useState(attendance.breakRecord?.endTime.slice(0, 5) ?? "13:00");
  const [reason, setReason] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!reason.trim()) {
      setError("修正理由は必須です");
      return;
    }

    try {
      setSubmitting(true);
      setError(null);
      const request: AttendanceModifyRequest = {
        clockIn,
        clockOut,
        breakStart,
        breakEnd,
        reason: reason.trim(),
      };
      await modifyAttendance(attendance.id, request);
      onSaved();
    } catch (e) {
      if (e instanceof ApiError) {
        setError(e.message);
      } else {
        setError("修正に失敗しました");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <h3 className="text-lg font-semibold mb-4">勤怠修正（{attendance.date}）</h3>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-3 py-2 rounded mb-4 text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm text-gray-600 mb-1">出勤</label>
              <input
                type="time"
                value={clockIn}
                onChange={(e) => setClockIn(e.target.value)}
                className="w-full border rounded px-3 py-2"
                required
              />
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">退勤</label>
              <input
                type="time"
                value={clockOut}
                onChange={(e) => setClockOut(e.target.value)}
                className="w-full border rounded px-3 py-2"
                required
              />
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">休憩開始</label>
              <input
                type="time"
                value={breakStart}
                onChange={(e) => setBreakStart(e.target.value)}
                className="w-full border rounded px-3 py-2"
                required
              />
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">休憩終了</label>
              <input
                type="time"
                value={breakEnd}
                onChange={(e) => setBreakEnd(e.target.value)}
                className="w-full border rounded px-3 py-2"
                required
              />
            </div>
          </div>

          <div>
            <label className="block text-sm text-gray-600 mb-1">修正理由（必須）</label>
            <textarea
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              className="w-full border rounded px-3 py-2 h-20"
              placeholder="修正理由を入力してください"
              required
            />
          </div>

          <div className="flex justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 border rounded text-gray-700 hover:bg-gray-50"
            >
              キャンセル
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
            >
              {submitting ? "保存中..." : "保存"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
