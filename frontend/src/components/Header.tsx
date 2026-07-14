"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";

export function Header() {
  const { user, logout } = useAuth();
  const router = useRouter();

  const handleLogout = async () => {
    await logout();
    router.push("/login");
  };

  return (
    <header className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
      <Link href="/" className="text-xl font-bold text-gray-900">
        勤怠管理システム
      </Link>
      <div className="flex items-center gap-4">
        {user && (
          <>
            <span className="text-sm text-gray-600">
              {user.name}（{user.role}）
            </span>
            <button
              onClick={handleLogout}
              className="text-sm text-gray-500 hover:text-gray-700 px-3 py-1 border border-gray-300 rounded-md"
            >
              ログアウト
            </button>
          </>
        )}
        {!user && (
          <Link href="/login" className="text-sm text-blue-600 hover:text-blue-800">
            ログイン
          </Link>
        )}
      </div>
    </header>
  );
}
