import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { Header } from "../Header";

describe("Header", () => {
  it("アプリ名のリンクが表示される", () => {
    render(<Header />);
    expect(screen.getByText("勤怠管理システム")).toBeInTheDocument();
  });

  it("ナビゲーションリンクが表示される", () => {
    render(<Header />);
    const navLinks = screen.getAllByRole("link");
    expect(navLinks.length).toBeGreaterThanOrEqual(2);
  });
});
