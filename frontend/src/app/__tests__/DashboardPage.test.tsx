import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import DashboardPage from "../page";

const mockGetToday = vi.fn();
const mockGetMonthly = vi.fn();
const mockClockIn = vi.fn();
const mockClockOut = vi.fn();

vi.mock("@/lib/attendance-api", () => ({
  getToday: (...args: unknown[]) => mockGetToday(...args),
  getMonthly: (...args: unknown[]) => mockGetMonthly(...args),
  clockIn: (...args: unknown[]) => mockClockIn(...args),
  clockOut: (...args: unknown[]) => mockClockOut(...args),
}));

vi.mock("next/navigation", () => ({
  usePathname: () => "/",
  useRouter: () => ({ push: vi.fn() }),
}));

const monthlyDefault = {
  yearMonth: "2026-07",
  attendances: [],
  totalWorkDurationMinutes: 2175,
  totalOvertimeMinutes: 300,
  workDays: 5,
};

describe("DashboardPage", () => {
  beforeEach(() => {
    cleanup();
    mockGetToday.mockReset();
    mockGetMonthly.mockReset();
    mockClockIn.mockReset();
    mockClockOut.mockReset();
    mockGetMonthly.mockResolvedValue(monthlyDefault);
  });

  it("未出勤の場合、出勤ボタンが表示される", async () => {
    mockGetToday.mockResolvedValue({
      status: "NOT_CLOCKED_IN",
      attendance: null,
    });

    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.getByText("未出勤")).toBeInTheDocument();
      expect(screen.getByRole("button", { name: "出勤" })).toBeInTheDocument();
    });
  });

  it("勤務中の場合、退勤ボタンが表示される", async () => {
    mockGetToday.mockResolvedValue({
      status: "WORKING",
      attendance: {
        id: 1,
        date: "2026-07-14",
        clockIn: "09:15",
        clockOut: null,
        breakRecord: { id: 10, startTime: "12:00", endTime: "13:00" },
        workDurationMinutes: 285,
        overtimeMinutes: 0,
        nightOvertimeMinutes: 0,
        modifiedManually: false,
        modificationReason: null,
        approvalStatus: "PENDING",
      },
    });

    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.getByText("勤務中")).toBeInTheDocument();
      expect(screen.getByRole("button", { name: "退勤" })).toBeInTheDocument();
    });
  });

  it("退勤済みの場合、打刻ボタンが表示されない", async () => {
    mockGetToday.mockResolvedValue({
      status: "CLOCKED_OUT",
      attendance: {
        id: 1,
        date: "2026-07-14",
        clockIn: "09:15",
        clockOut: "17:30",
        breakRecord: { id: 10, startTime: "12:00", endTime: "13:00" },
        workDurationMinutes: 435,
        overtimeMinutes: 0,
        nightOvertimeMinutes: 0,
        modifiedManually: false,
        modificationReason: null,
        approvalStatus: "PENDING",
      },
    });

    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.getByText(/退勤済/)).toBeInTheDocument();
      expect(screen.queryByRole("button", { name: "出勤" })).not.toBeInTheDocument();
      expect(screen.queryByRole("button", { name: "退勤" })).not.toBeInTheDocument();
    });
  });

  it("当月サマリーが正しく表示される", async () => {
    mockGetToday.mockResolvedValue({
      status: "NOT_CLOCKED_IN",
      attendance: null,
    });

    render(<DashboardPage />);

    await waitFor(() => {
      expect(screen.getByText("出勤日数")).toBeInTheDocument();
      expect(screen.getByText("総勤務時間")).toBeInTheDocument();
      expect(screen.getByText("総残業時間")).toBeInTheDocument();
    });
  });

  it("出勤ボタン押下で clockIn API が呼ばれる", async () => {
    mockGetToday.mockResolvedValue({
      status: "NOT_CLOCKED_IN",
      attendance: null,
    });
    mockClockIn.mockResolvedValue({
      id: 1,
      date: "2026-07-14",
      clockIn: "09:15",
      clockOut: null,
    });

    const user = userEvent.setup();
    render(<DashboardPage />);

    const button = await screen.findByRole("button", { name: "出勤" });
    await user.click(button);

    expect(mockClockIn).toHaveBeenCalledTimes(1);
  });
});
