# ドメインモデル設計

## ドメイン概要図

```
┌─────────────┐       ┌─────────────────┐       ┌──────────────┐
│  Department │1    * │    Employee      │1    * │  Attendance  │
│             │───────│                  │───────│              │
│ id          │       │ id               │       │ id           │
│ name        │       │ name             │       │ date         │
│ managerId   │       │ email            │       │ clockIn      │
│             │       │ departmentId     │       │ clockOut     │
└─────────────┘       │ role             │       │ status       │
                      │ position         │       └──────┬───────┘
                      │ active           │              │1
                      └───────┬──────────┘              │
                              │1                        │1
                              │                  ┌──────┴───────┐
                              │                  │ BreakRecord   │
                              │                  │              │
                              │                  │ id           │
                              │                  │ startTime    │
                              │                  │ endTime      │
                              │                  │ (default     │
                              │                  │  12:00-13:00)│
                              │                  └──────────────┘
                              │1
                              │*
                      ┌───────┴──────────┐
                      │  LeaveRequest    │
                      │                  │
                      │ id               │
                      │ date             │
                      │ type             │
                      │ reason           │
                      │ status           │
                      └──────────────────┘
```

## Entity

### Employee（社員）

| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | 社員ID（自動採番） |
| name | String | 氏名 |
| email | String | メールアドレス |
| password | String | ハッシュ化パスワード |
| department | Department | 所属部署 |
| role | Role (enum) | EMPLOYEE / MANAGER / ADMIN |
| position | String | 役職 |
| active | boolean | 有効フラグ（論理削除用） |
| paidLeaveBalance | int | 有給残日数 |
| version | Long | 楽観ロック |

ビジネスルール:
- 有給残日数は 0 未満にならない
- 4/1 に残日数を 20 にリセットする（繰越なし）

### Department（部署）

| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | 部署ID |
| name | String | 部署名 |
| managerId | Long | 部署長の社員ID（= 上長） |
| version | Long | 楽観ロック |

ビジネスルール:
- 部署長は自動的にその部署の「上長」になる
- 部署長自身の承認は管理者が行う

### Attendance（勤怠）

| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | 勤怠ID |
| employee | Employee | 対象社員 |
| date | LocalDate | 勤務日 |
| clockIn | LocalTime | 出勤時刻 |
| clockOut | LocalTime | 退勤時刻 |
| modifiedManually | boolean | 手動修正されたか |
| modificationReason | String | 修正理由 |
| approvalStatus | ApprovalStatus (enum) | PENDING / APPROVED / REJECTED |
| version | Long | 楽観ロック |

ビジネスルール:
- 同一社員・同一日の勤怠は 1 レコード
- 事後登録・修正は当月内のみ（月が変わったら修正不可）
- 手動修正時は理由が必須

### BreakRecord（休憩記録）

| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | 休憩ID |
| attendance | Attendance | 対応する勤怠 |
| startTime | LocalTime | 休憩開始 |
| endTime | LocalTime | 休憩終了 |

ビジネスルール:
- 1 つの勤怠に対して 1 件の休憩レコード（デフォルト 12:00〜13:00）
- 出勤打刻時にデフォルト値で自動作成される
- 変更は勤怠修正として扱う（修正理由必須）
- 休憩開始 < 休憩終了
- 出勤〜退勤の範囲内でなければならない

### LeaveRequest（有給申請）

| フィールド | 型 | 説明 |
|-----------|-----|------|
| id | Long | 申請ID |
| employee | Employee | 申請者 |
| date | LocalDate | 取得日 |
| type | LeaveType (enum) | FULL_DAY / AM_HALF / PM_HALF |
| reason | String | 理由 |
| status | ApprovalStatus (enum) | PENDING / APPROVED / REJECTED |
| version | Long | 楽観ロック |

ビジネスルール:
- 有給残日数が 0 の場合は申請不可
- 半休は 0.5 日消費
- 承認時に残日数を減算する

## Value Object

### WorkDuration（勤務時間）

- totalMinutes: int（分単位）
- 計算ロジック: 退勤 − 出勤 − 休憩合計
- 所定時間（435分 = 7h15m）との差分で残業を算出

### TimeRange（時間範囲）

- start: LocalTime
- end: LocalTime
- 所定労働時間や休憩の表現に使用

## Enum

### Role

| 値 | 説明 |
|----|------|
| EMPLOYEE | 一般社員 |
| MANAGER | 上長（部署長） |
| ADMIN | 管理者 |

### ApprovalStatus

| 値 | 説明 |
|----|------|
| PENDING | 未承認（申請中） |
| APPROVED | 承認済 |
| REJECTED | 却下（差し戻し） |

### LeaveType

| 値 | 説明 |
|----|------|
| FULL_DAY | 全日休 |
| AM_HALF | 午前半休 |
| PM_HALF | 午後半休 |

## Service

### AttendanceService

- `clockIn(employeeId)` — 出勤打刻
- `clockOut(employeeId)` — 退勤打刻
- `modifyAttendance(employeeId, date, clockIn, clockOut, reason)` — 手動修正
- `calculateWorkDuration(attendance)` — 勤務時間計算
- `calculateOvertime(attendance)` — 残業時間計算
- `getMonthlyAttendance(employeeId, yearMonth)` — 月次勤怠取得

### BreakService

- `createDefaultBreak(attendance)` — デフォルト休憩（12:00〜13:00）を自動作成
- `modifyBreak(attendanceId, startTime, endTime)` — 休憩時間を変更

### LeaveService

- `applyLeave(employeeId, date, type, reason)` — 有給申請
- `getBalance(employeeId)` — 残日数取得
- `resetAnnualLeave()` — 年度切替（4/1 に全員リセット）

### ApprovalService

- `approveAttendance(attendanceId, approverId)` — 勤怠承認
- `rejectAttendance(attendanceId, approverId)` — 勤怠差し戻し
- `approveLeave(leaveRequestId, approverId)` — 有給承認
- `rejectLeave(leaveRequestId, approverId)` — 有給却下
- `getPendingCount(approverId)` — 未承認件数取得

### EmployeeService

- `create(dto)` — 社員登録
- `update(id, dto)` — 社員編集
- `deactivate(id)` — 無効化（論理削除）
- `resetPassword(id)` — パスワードリセット

### ReportService

- `generateMonthlyReport(yearMonth, scope)` — 月次 CSV 生成

## Repository（interface）

- `EmployeeRepository`
- `DepartmentRepository`
- `AttendanceRepository`
- `BreakRecordRepository`
- `LeaveRequestRepository`
