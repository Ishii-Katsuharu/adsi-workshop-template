import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { withBasePath, apiFetch, ApiError } from "../api-client";

describe("withBasePath", () => {
  const originalEnv = process.env;

  beforeEach(() => {
    vi.resetModules();
    process.env = { ...originalEnv };
  });

  afterEach(() => {
    process.env = originalEnv;
  });

  it("basePath が未設定のときはパスをそのまま返す", () => {
    delete process.env.NEXT_PUBLIC_BASE_PATH;
    expect(withBasePath("/api/employees")).toBe("/api/employees");
  });

  it("basePath が設定されているときはプレフィックスを付与する", () => {
    process.env.NEXT_PUBLIC_BASE_PATH = "/codeeditor/default/absports/3000";
    expect(withBasePath("/api/employees")).toBe(
      "/codeeditor/default/absports/3000/api/employees"
    );
  });

  it("http で始まる URL はそのまま返す", () => {
    process.env.NEXT_PUBLIC_BASE_PATH = "/codeeditor/default/absports/3000";
    expect(withBasePath("http://example.com/api")).toBe(
      "http://example.com/api"
    );
  });
});

describe("apiFetch", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("正常レスポンスを JSON として返す", async () => {
    const mockData = { id: 1, name: "テスト" };
    (fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve(mockData),
    });

    const result = await apiFetch("/employees");
    expect(result).toEqual(mockData);
  });

  it("エラーレスポンスで ApiError をスローする", async () => {
    (fetch as ReturnType<typeof vi.fn>).mockResolvedValue({
      ok: false,
      status: 404,
      json: () => Promise.resolve({ message: "Not found" }),
    });

    await expect(apiFetch("/employees/999")).rejects.toThrow(ApiError);
    await expect(apiFetch("/employees/999")).rejects.toMatchObject({
      status: 404,
      message: "Not found",
    });
  });
});
