# DB 設計

## ER 図

```
┌──────────────────┐       ┌──────────────────────────┐
│   departments    │       │       employees           │
├──────────────────┤       ├──────────────────────────┤
│ PK id            │◄──┐   │ PK id                    │
│    name          │   │   │ FK department_id          │──┐
│ FK manager_id    │───┼──►│    name                   │  │
│    version       │   │   │    email                  │  │
└──────────────────┘   │   │    password               │  │
                       │   │    role                    │  │
                       │   │    position                │  │
                       │   │    active                  │  │
                       │   │    paid_leave_balance      │  │
                       │   │    version                 │  │
                       │   └────────────┬──────────────┘  │
                       │                │                  │
                       └────────────────┼──────────────────┘
                                        │
                         ┌──────────────┼──────────────┐
                         │              │              │
                         ▼              ▼              ▼
          ┌──────────────────┐  ┌─────────────────┐  ┌──────────────────┐
          │   attendances    │  │ leave_requests  │  │                  │
          ├──────────────────┤  ├─────────────────┤  │                  │
          │ PK id            │  │ PK id           │  │                  │
          │ FK employee_id   │  │ FK employee_id  │  │                  │
          │    date          │  │    date         │  │                  │
          │    clock_in      │  │    type         │  │                  │
          │    clock_out     │  │    reason       │  │                  │
          │    modified_     │  │    status       │  │                  │
          │      manually    │  │    version      │  │                  │
          │    modification_ │  └─────────────────┘  │                  │
          │      reason      │                       │                  │
          │    approval_     │                       │                  │
          │      status      │                       │                  │
          │    version       │                       │                  │
          └───────┬──────────┘                       │                  │
                  │                                  │                  │
                  ▼                                  │                  │
          ┌──────────────────┐                       │                  │
          │  break_records   │                       │                  │
          ├──────────────────┤                       │                  │
          │ PK id            │                       │                  │
          │ FK attendance_id │                       │                  │
          │    start_time    │                       │                  │
          │    end_time      │                       │                  │
          └──────────────────┘                       │                  │
```

## テーブル定義

### departments

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 部署ID |
| name | VARCHAR(100) | NOT NULL, UNIQUE | 部署名 |
| manager_id | BIGINT | FK (employees.id), NULL可 | 部署長 |
| version | BIGINT | NOT NULL, DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL | 更新日時 |

### employees

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 社員ID |
| name | VARCHAR(100) | NOT NULL | 氏名 |
| email | VARCHAR(255) | NOT NULL, UNIQUE | メールアドレス |
| password | VARCHAR(255) | NOT NULL | ハッシュ化パスワード |
| department_id | BIGINT | FK (departments.id), NOT NULL | 所属部署 |
| role | VARCHAR(20) | NOT NULL | EMPLOYEE / MANAGER / ADMIN |
| position | VARCHAR(100) | NULL可 | 役職 |
| active | BOOLEAN | NOT NULL, DEFAULT TRUE | 有効フラグ |
| paid_leave_balance | INT | NOT NULL, DEFAULT 20 | 有給残日数 |
| version | BIGINT | NOT NULL, DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL | 更新日時 |

### attendances

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 勤怠ID |
| employee_id | BIGINT | FK (employees.id), NOT NULL | 社員 |
| date | DATE | NOT NULL | 勤務日 |
| clock_in | TIME | NULL可 | 出勤時刻 |
| clock_out | TIME | NULL可 | 退勤時刻 |
| modified_manually | BOOLEAN | NOT NULL, DEFAULT FALSE | 手動修正フラグ |
| modification_reason | VARCHAR(500) | NULL可 | 修正理由 |
| approval_status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 承認状態 |
| version | BIGINT | NOT NULL, DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL | 更新日時 |

UNIQUE 制約: `(employee_id, date)`

### break_records

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 休憩ID |
| attendance_id | BIGINT | FK (attendances.id), NOT NULL | 対応勤怠 |
| start_time | TIME | NOT NULL | 休憩開始 |
| end_time | TIME | NULL可 | 休憩終了 |
| created_at | TIMESTAMP | NOT NULL | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL | 更新日時 |

### leave_requests

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 申請ID |
| employee_id | BIGINT | FK (employees.id), NOT NULL | 申請者 |
| date | DATE | NOT NULL | 取得日 |
| type | VARCHAR(20) | NOT NULL | FULL_DAY / AM_HALF / PM_HALF |
| reason | VARCHAR(500) | NOT NULL | 理由 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 承認状態 |
| version | BIGINT | NOT NULL, DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL | 更新日時 |

## インデックス

| テーブル | インデックス | カラム | 用途 |
|----------|------------|--------|------|
| attendances | idx_att_emp_date | employee_id, date | 社員×日付の検索 |
| attendances | idx_att_date_status | date, approval_status | 承認待ち一覧 |
| leave_requests | idx_lr_emp_date | employee_id, date | 社員×日付の検索 |
| leave_requests | idx_lr_status | status | 承認待ち一覧 |
| employees | idx_emp_dept | department_id | 部署メンバー検索 |
| employees | idx_emp_active | active | 有効社員のみ抽出 |

## Flyway マイグレーション計画

| バージョン | 内容 |
|-----------|------|
| V1__create_departments.sql | departments テーブル作成 |
| V2__create_employees.sql | employees テーブル作成 + FK |
| V3__create_attendances.sql | attendances テーブル作成 + インデックス |
| V4__create_break_records.sql | break_records テーブル作成 |
| V5__create_leave_requests.sql | leave_requests テーブル作成 + インデックス |
| V6__add_manager_fk.sql | departments.manager_id FK 追加（循環参照のため後付け） |
