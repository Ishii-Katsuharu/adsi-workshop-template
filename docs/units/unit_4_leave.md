# Unit 4: 有給休暇

## 目的

有給休暇の申請・承認・残日数管理を実装する。

## ユーザーストーリー

- **US-4**: 有給休暇の申請・管理

## スコープ

### Backend

- **LeaveRequestRepository** — JPA Repository interface
- **LeaveService** (interface + impl)
  - applyLeave / getMyLeaves / getBalance / resetAnnualLeave
  - 残日数チェック（0 なら申請不可）
  - 半休は 0.5 日消費
- **ApprovalService に追加**
  - approveLeave / rejectLeave / getPendingLeaves
  - 承認時に残日数を減算
- **LeaveController** — `/api/leaves`
- **ApprovalController に追加** — `/api/approvals/leaves`
- **テスト**
  - LeaveService 単体テスト（残日数計算・バリデーション）
  - LeaveController WebMvcTest
  - 統合テスト（申請 → 承認 → 残日数反映）

### Frontend

- 有給申請画面（残日数表示・申請フォーム・履歴テーブル）
- 承認一覧画面に「有給承認」タブを追加

## テーブル

- leave_requests（unit_0 で作成済み）
- employees.paid_leave_balance を操作

## API エンドポイント

| メソッド | パス | 権限 |
|---------|------|------|
| POST | /api/leaves | 認証済み |
| GET | /api/leaves | 認証済み |
| GET | /api/leaves/balance | 認証済み |
| GET | /api/approvals/leaves | MANAGER, ADMIN |
| POST | /api/approvals/leaves/{id}/approve | MANAGER, ADMIN |
| POST | /api/approvals/leaves/{id}/reject | MANAGER, ADMIN |

## ビジネスルール

- 年間 20 日付与、毎年 4/1 に一括リセット
- 繰越なし（年度末で消滅）
- 半休（AM_HALF / PM_HALF）は 0.5 日消費
- 残日数 0 で申請不可
- 承認時に残日数を減算（却下時は戻さない＝申請段階では減算しない）

## 依存

- unit_1_employee（社員情報・残日数フィールド）
- unit_3_approval（承認フローの仕組みを再利用）

## 完了条件

- [ ] 有給申請ができる（全日・半休）
- [ ] 残日数が正しく計算される
- [ ] 上長が有給を承認/却下できる
- [ ] 残日数 0 で申請がブロックされる
- [ ] テストカバレッジ 80% 以上
