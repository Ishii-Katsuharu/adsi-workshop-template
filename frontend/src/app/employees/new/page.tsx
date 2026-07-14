"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { apiFetch } from "@/lib/api-client";
import type { EmployeeCreateRequest, EmployeeResponse } from "@/types/employee";

const ROLES = [
  { value: "EMPLOYEE", label: "一般社員" },
  { value: "MANAGER", label: "上長" },
  { value: "ADMIN", label: "管理者" },
];

export default function NewEmployeePage() {
  const { user, loading } = useAuth();
  const router = useRouter();
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (loading) return <div>読み込み中...</div>;
  if (!user || user.role !== "ADMIN") {
    router.push("/");
    return null;
  }

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");
    setIsSubmitting(true);

    const formData = new FormData(e.currentTarget);
    const request: EmployeeCreateRequest = {
      name: formData.get("name") as string,
      email: formData.get("email") as string,
      departmentId: Number(formData.get("departmentId")),
      role: formData.get("role") as string,
      position: (formData.get("position") as string) || undefined,
      password: formData.get("password") as string,
    };

    try {
      await apiFetch<EmployeeResponse>("/employees", {
        method: "POST",
        body: JSON.stringify(request),
      });
      router.push("/employees");
    } catch (e) {
      setError(e instanceof Error ? e.message : "登録に失敗しました");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">社員登録</h1>

      {error && (
        <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4 bg-white p-6 rounded-lg shadow">
        <div>
          <label htmlFor="name" className="block text-sm font-medium text-gray-700">氏名</label>
          <input id="name" name="name" type="text" required maxLength={100}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700">メールアドレス</label>
          <input id="email" name="email" type="email" required maxLength={255}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="departmentId" className="block text-sm font-medium text-gray-700">部署ID</label>
          <input id="departmentId" name="departmentId" type="number" required min={1}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="role" className="block text-sm font-medium text-gray-700">役割</label>
          <select id="role" name="role" required
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
            {ROLES.map((r) => (
              <option key={r.value} value={r.value}>{r.label}</option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="position" className="block text-sm font-medium text-gray-700">役職</label>
          <input id="position" name="position" type="text" maxLength={100}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="password" className="block text-sm font-medium text-gray-700">パスワード</label>
          <input id="password" name="password" type="password" required minLength={8}
            className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div className="flex gap-4 pt-4">
          <button type="submit" disabled={isSubmitting}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50">
            {isSubmitting ? "登録中..." : "登録"}
          </button>
          <button type="button" onClick={() => router.push("/employees")}
            className="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300">
            キャンセル
          </button>
        </div>
      </form>
    </div>
  );
}
