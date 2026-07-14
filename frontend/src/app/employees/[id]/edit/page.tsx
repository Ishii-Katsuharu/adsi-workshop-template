"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { apiFetch } from "@/lib/api-client";
import type { EmployeeResponse, EmployeeUpdateRequest, PasswordResetRequest } from "@/types/employee";

const ROLES = [
  { value: "EMPLOYEE", label: "一般社員" },
  { value: "MANAGER", label: "上長" },
  { value: "ADMIN", label: "管理者" },
];

export default function EditEmployeePage() {
  const { user, loading } = useAuth();
  const router = useRouter();
  const params = useParams();
  const id = params.id as string;

  const [employee, setEmployee] = useState<EmployeeResponse | null>(null);
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [newPassword, setNewPassword] = useState("");
  const [passwordMessage, setPasswordMessage] = useState("");

  useEffect(() => {
    if (!loading && user?.role === "ADMIN") {
      apiFetch<EmployeeResponse>(`/employees/${id}`)
        .then(setEmployee)
        .catch((e) => setError(e.message));
    }
  }, [id, user, loading]);

  if (loading) return <div>読み込み中...</div>;
  if (!user || user.role !== "ADMIN") {
    router.push("/");
    return null;
  }
  if (!employee) return <div>読み込み中...</div>;

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");
    setIsSubmitting(true);

    const formData = new FormData(e.currentTarget);
    const request: EmployeeUpdateRequest = {
      name: formData.get("name") as string,
      email: formData.get("email") as string,
      departmentId: Number(formData.get("departmentId")),
      role: formData.get("role") as string,
      position: (formData.get("position") as string) || undefined,
    };

    try {
      const updated = await apiFetch<EmployeeResponse>(`/employees/${id}`, {
        method: "PUT",
        body: JSON.stringify(request),
      });
      setEmployee(updated);
      router.push("/employees");
    } catch (e) {
      setError(e instanceof Error ? e.message : "更新に失敗しました");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResetPassword = async () => {
    if (!newPassword || newPassword.length < 8) {
      setPasswordMessage("パスワードは8文字以上にしてください");
      return;
    }
    try {
      const request: PasswordResetRequest = { newPassword };
      await apiFetch<void>(`/employees/${id}/reset-password`, {
        method: "POST",
        body: JSON.stringify(request),
      });
      setPasswordMessage("パスワードをリセットしました");
      setNewPassword("");
    } catch (e) {
      setPasswordMessage(e instanceof Error ? e.message : "リセットに失敗しました");
    }
  };

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">社員編集</h1>

      {error && (
        <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4 bg-white p-6 rounded-lg shadow">
        <div>
          <label htmlFor="name" className="block text-sm font-medium text-gray-700">氏名</label>
          <input id="name" name="name" type="text" required maxLength={100} defaultValue={employee.name}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700">メールアドレス</label>
          <input id="email" name="email" type="email" required maxLength={255} defaultValue={employee.email}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="departmentId" className="block text-sm font-medium text-gray-700">部署ID</label>
          <input id="departmentId" name="departmentId" type="number" required min={1} defaultValue={employee.departmentId}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="role" className="block text-sm font-medium text-gray-700">役割</label>
          <select id="role" name="role" required defaultValue={employee.role}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
            {ROLES.map((r) => (
              <option key={r.value} value={r.value}>{r.label}</option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="position" className="block text-sm font-medium text-gray-700">役職</label>
          <input id="position" name="position" type="text" maxLength={100} defaultValue={employee.position || ""}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div className="flex gap-4 pt-4">
          <button type="submit" disabled={isSubmitting}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50">
            {isSubmitting ? "更新中..." : "更新"}
          </button>
          <button type="button" onClick={() => router.push("/employees")}
            className="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300">
            キャンセル
          </button>
        </div>
      </form>

      <div className="mt-8 bg-white p-6 rounded-lg shadow">
        <h2 className="text-lg font-medium text-gray-900 mb-4">パスワードリセット</h2>
        <div className="flex gap-4 items-end">
          <div className="flex-1">
            <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700">新しいパスワード</label>
            <input
              id="newPassword"
              type="password"
              minLength={8}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          <button type="button" onClick={handleResetPassword}
            className="px-4 py-2 bg-orange-500 text-white rounded-md hover:bg-orange-600">
            リセット
          </button>
        </div>
        {passwordMessage && (
          <p className="mt-2 text-sm text-gray-600">{passwordMessage}</p>
        )}
      </div>
    </div>
  );
}
