# Unit 5: 月次レポート

## 目的

管理者向けの月次勤怠レポート（CSV出力）を実装する。

## ユーザーストーリー

- **US-6**: 月次勤怠レポート

## スコープ

### Backend

- **ReportService** (interface + impl)
  - generateMonthlyReport(yearMonth, departmentId?, employeeId?)
  - 集計: 出勤日数、勤務時間合計、残業時間合計、有給取得日数
  - CSV フォーマット生成
- **ReportController** — `/api/reports/monthly`
- **テスト**
  - ReportService 単体テスト（集計ロジック）
  - ReportController WebMvcTest（CSVレスポンス確認）
  - 統合テスト

### Frontend

- レポート出力画面
  - 対象年月セレクタ
  - 対象範囲（全員 / 部署 / 個人）
  - プレビューテーブル
  - CSV ダウンロードボタン

## テーブル

- attendances（集計対象、読み取りのみ）
- employees（社員情報参照）
- leave_requests（有給取得日数カウント）

## API エンドポイント

| メソッド | パス | 権限 |
|---------|------|------|
| GET | /api/reports/monthly?yearMonth=&departmentId=&employeeId= | ADMIN |

## 出力項目

CSV カラム:
1. 社員ID
2. 氏名
3. 部署
4. 出勤日数
5. 勤務時間合計（時:分）
6. 残業時間合計（時:分）
7. 有給取得日数

## 依存

- unit_1_employee（社員・部署情報）
- unit_2_attendance（勤怠データの集計）

## 完了条件

- [ ] 管理者が月次レポートをCSVダウンロードできる
- [ ] 対象範囲（全員/部署/個人）でフィルタリングできる
- [ ] 集計値が正しい
- [ ] テストカバレッジ 80% 以上
