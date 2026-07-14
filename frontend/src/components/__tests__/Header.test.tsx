import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { Header } from "../Header";

vi.mock("@/lib/auth", () => ({
  useAuth: () => ({
    user: { id: 1, name: "テストユーザー", role: "ADMIN" },
    loading: false,
    login: vi.fn(),
    logout: vi.fn(),
  }),
}));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: vi.fn() }),
}));

describe("Header", () => {
  it("アプリ名のリンクが表示される", () => {
    render(<Header />);
    expect(screen.getByText("勤怠管理システム")).toBeInTheDocument();
  });

  it("ユーザー名とログアウトボタンが表示される", () => {
    render(<Header />);
    const userLabels = screen.getAllByText(/テストユーザー/);
    expect(userLabels.length).toBeGreaterThanOrEqual(1);
    const logoutButtons = screen.getAllByText("ログアウト");
    expect(logoutButtons.length).toBeGreaterThanOrEqual(1);
  });
});
