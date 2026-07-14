import Link from "next/link";

export function Header() {
  return (
    <header className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between">
      <Link href="/" className="text-xl font-bold text-gray-900">
        勤怠管理システム
      </Link>
      <nav className="flex items-center gap-4">
        <Link href="/" className="text-sm text-gray-600 hover:text-gray-900">
          ダッシュボード
        </Link>
      </nav>
    </header>
  );
}
