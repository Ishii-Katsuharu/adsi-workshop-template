# Unit 2: 打刻 + 休憩 + 勤務時間計算

## 目的

出勤・退勤の打刻と休憩管理、勤務時間の自動計算を実装する。
勤怠管理のコア機能。

## ユーザーストーリー

- **US-1**: 出勤・退勤の打刻
- **US-2**: 休憩時間の管理（デフォルト 12:00-13:00、勤怠一覧から編集）
- **US-3**: 勤務時間の自動計算

## スコープ

### Backend

- **AttendanceRepository** — JPA Repository interface
- **BreakRecordRepository** — JPA Repository interface
- **AttendanceService** (interface + impl)
  - clockIn / clockOut / modifyAttendance / getToday / getMonthly
  - 出勤時にデフォルト休憩レコードを自動作成
- **WorkDurationCalculator** — 勤務時間・残業計算ロジック（Value Object）
  - 実労働時間 = 退勤 − 出勤 − 休憩
  - 残業 = 実労働時間 − 435分（7h15m）
- **AttendanceController** — `/api/attendances`
- **テスト**
  - WorkDurationCalculator 単体テスト（計算ロジック重点）
  - AttendanceService 単体テスト
  - AttendanceController WebMvcTest
  - 統合テスト（打刻 → 計算の一連フロー）

### Frontend

- ダッシュボード画面（打刻ボタン・今日の勤務情報・当月サマリー）
- 勤怠一覧画面（月次テーブル・修正ダイアログ）

## テーブル

- attendances（unit_0 で作成済み）
- break_records（unit_0 で作成済み）

## API エンドポイント

| メソッド | パス | 権限 |
|---------|------|------|
| POST | /api/attendances/clock-in | 認証済み |
| POST | /api/attendances/clock-out | 認証済み |
| GET | /api/attendances/today | 認証済み |
| GET | /api/attendances/monthly?yearMonth= | 認証済み |
| PUT | /api/attendances/{id} | 認証済み（本人のみ） |

## ビジネスルール

- 同一社員・同一日の勤怠は 1 レコード
- 出勤打刻時にデフォルト休憩（12:00-13:00）を自動作成
- 事後登録・修正は当月内のみ
- 手動修正時は理由必須
- 修正すると `modified_manually = true` になる

## 依存

- unit_0_foundation（Entity, Flyway）
- 認証は unit_1 だが、テスト時はモックで代替可能（並行実装OK）

## 並行実装の注意

- SecurityContext からのユーザー取得は interface で抽象化する
  - `CurrentUserProvider` interface → unit_1 完了後に本実装を接続
  - テスト時は固定ユーザーを返すスタブを使う

## 完了条件

- [ ] 出勤・退勤の打刻ができる
- [ ] デフォルト休憩が自動設定される
- [ ] 勤務時間・残業時間が正しく計算される
- [ ] 勤怠の手動修正ができる（当月内・理由必須）
- [ ] テストカバレッジ 80% 以上
