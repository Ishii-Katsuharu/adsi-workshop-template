"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const navItems = [
  { href: "/", label: "ダッシュボード" },
  { href: "/attendance", label: "勤怠一覧" },
  { href: "/leave", label: "有給申請" },
  { href: "/approval", label: "承認一覧" },
  { href: "/employees", label: "社員管理" },
  { href: "/reports", label: "レポート" },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="w-60 bg-gray-50 border-r border-gray-200 min-h-screen p-4">
      <nav className="space-y-1">
        {navItems.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={`block px-3 py-2 rounded-md text-sm ${
                isActive
                  ? "bg-blue-100 text-blue-700 font-medium"
                  : "text-gray-700 hover:bg-gray-100"
              }`}
            >
              {item.label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
