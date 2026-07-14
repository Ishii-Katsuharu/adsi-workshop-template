"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import { apiFetch } from "@/lib/api-client";
import type { EmployeeResponse } from "@/types/employee";

export default function EmployeesPage() {
  const { user, loading } = useAuth();
  const router = useRouter();
  const [employees, setEmployees] = useState<EmployeeResponse[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!loading && !user) {
      router.push("/login");
    }
  }, [user, loading, router]);

  useEffect(() => {
    if (user?.role === "ADMIN") {
      apiFetch<EmployeeResponse[]>("/employees")
        .then(setEmployees)
        .catch((e) => setError(e.message));
    }
  }, [user]);

  if (loading) return <div>読み込み中...</div>;
  if (!user) return null;
  if (user.role !== "ADMIN") return <div>アクセス権限がありません</div>;

  const handleDeactivate = async (id: number) => {
    if (!confirm("この社員を無効化しますか？")) return;
    try {
      await apiFetch<void>(`/employees/${id}/deactivate`, { method: "POST" });
      setEmployees((prev) => prev.map((e) => (e.id === id ? { ...e, active: false } : e)));
    } catch (e) {
      setError(e instanceof Error ? e.message : "エラーが発生しました");
    }
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-900">社員管理</h1>
        <Link
          href="/employees/new"
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm"
        >
          社員登録
        </Link>
      </div>

      {error && (
        <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      <div className="bg-white shadow rounded-lg overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">氏名</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">メール</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">部署</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">役割</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状態</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {employees.map((emp) => (
              <tr key={emp.id}>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{emp.id}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{emp.name}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{emp.email}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{emp.departmentName}</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{emp.role}</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className={`px-2 py-1 text-xs rounded-full ${emp.active ? "bg-green-100 text-green-800" : "bg-gray-100 text-gray-800"}`}>
                    {emp.active ? "有効" : "無効"}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm space-x-2">
                  <Link href={`/employees/${emp.id}/edit`} className="text-blue-600 hover:text-blue-800">
                    編集
                  </Link>
                  {emp.active && (
                    <button
                      onClick={() => handleDeactivate(emp.id)}
                      className="text-red-600 hover:text-red-800"
                    >
                      無効化
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
