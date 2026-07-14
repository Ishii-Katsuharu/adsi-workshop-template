# Unit 3: 承認フロー

## 目的

上長による勤怠承認・差し戻しフローを実装する。

## ユーザーストーリー

- **US-5**: 勤怠承認フロー

## スコープ

### Backend

- **ApprovalService** (interface + impl)
  - approveAttendance / rejectAttendance / getPendingAttendances / getPendingCount
  - 上長判定: 部署の manager_id と一致するか
- **ApprovalController** — `/api/approvals/attendances`
- **テスト**
  - ApprovalService 単体テスト（権限チェック含む）
  - ApprovalController WebMvcTest
  - 統合テスト（打刻 → 承認の一連フロー）

### Frontend

- 承認一覧画面（勤怠タブ）
  - 承認待ち勤怠テーブル
  - 承認 / 差し戻しボタン
  - 手動修正行のハイライト + 理由表示
- ダッシュボードの未承認バッジ（上長のみ）
- Sidebar に承認メニュー表示（MANAGER / ADMIN のみ）

## テーブル

- attendances.approval_status を操作
- departments.manager_id を参照

## API エンドポイント

| メソッド | パス | 権限 |
|---------|------|------|
| GET | /api/approvals/attendances | MANAGER, ADMIN |
| POST | /api/approvals/attendances/{id}/approve | MANAGER, ADMIN |
| POST | /api/approvals/attendances/{id}/reject | MANAGER, ADMIN |
| GET | /api/approvals/pending-count | MANAGER, ADMIN |

## ビジネスルール

- 上長は自部署メンバーの勤怠のみ承認可能
- ADMIN は全員の勤怠を承認可能
- 承認済みの勤怠は再修正不可

## 依存

- unit_1_employee（部署・上長判定、権限）
- unit_2_attendance（勤怠レコードの参照・状態更新）

## 完了条件

- [ ] 上長が部下の勤怠を承認/差し戻しできる
- [ ] 権限チェックが正しく動作する
- [ ] 未承認件数がダッシュボードに表示される
- [ ] テストカバレッジ 80% 以上
