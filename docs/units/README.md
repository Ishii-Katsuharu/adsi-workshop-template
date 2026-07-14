# Unit of Work 分割

## 依存図

```
Phase A: unit_0_foundation（共通基盤）
              │
              ▼
Phase B: ┌─────────────────────┬──────────────────────┐
         │                     │                      │
         ▼                     ▼                      │
   unit_1_employee       unit_2_attendance             │
   (社員管理+認証)       (打刻+休憩+計算)              │
         │                     │                      │
         └──────────┬──────────┘                      │
                    ▼                                  │
Phase C:   unit_3_approval                            │
           (承認フロー)                                │
                    │                                  │
                    ▼                                  ▼
Phase D:   unit_4_leave            unit_5_report
           (有給休暇)              (月次レポート)
```

## Phase 割り当てと担当

| Phase | Unit | 概要 | 依存先 | 担当 |
|-------|------|------|--------|------|
| A | unit_0_foundation | プロジェクト骨格・Flyway・共通設定 | なし | 共同 |
| B | unit_1_employee | 社員CRUD + 認証 + 部署 | unit_0 | 担当者A |
| B | unit_2_attendance | 打刻 + 休憩 + 勤務時間計算 | unit_0 | 担当者B |
| C | unit_3_approval | 勤怠承認フロー | unit_1 + unit_2 | 担当者A |
| D | unit_4_leave | 有給申請・承認・残日数 | unit_1 + unit_3 | 担当者B |
| D | unit_5_report | 月次CSV出力 | unit_1 + unit_2 | 担当者A |

## 並行実装の流れ

1. **Phase A**: 2人で共同で基盤を構築（半日〜1日）
2. **Phase B**: 担当者Aが社員管理、担当者Bが打刻を**並行実装**
3. **Phase C**: 承認フロー（Phase B の両方が必要なため合流）
4. **Phase D**: 有給とレポートを**並行実装**

## インターフェース先行の原則

Phase A で以下を定義し、Phase B 以降で本体を実装する:
- Flyway マイグレーション（全テーブル）
- Entity クラス（フィールドのみ）
- Enum（Role, ApprovalStatus, LeaveType）
- 共通例外クラス
- SecurityConfig の骨格
