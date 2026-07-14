# Unit 1: 社員管理 + 認証

## 目的

社員の CRUD と認証（ログイン/ログアウト）を実装する。
他の全 Unit が「認証済みユーザー」を前提にするため、Phase B の最優先 Unit。

## ユーザーストーリー

- **US-7**: 社員管理（管理者による登録・編集・無効化・パスワードリセット）
- 認証: ログイン（社員ID + パスワード）、ログアウト、現在ユーザー取得

## スコープ

### Backend

- **EmployeeRepository** — JPA Repository interface
- **DepartmentRepository** — JPA Repository interface
- **EmployeeService** (interface + impl)
  - create / update / deactivate / resetPassword / findById / findAll
- **AuthService** (interface + impl)
  - login / logout / getCurrentUser
- **EmployeeController** — `/api/employees` CRUD
- **AuthController** — `/api/auth/login`, `/api/auth/logout`, `/api/auth/me`
- **SecurityConfig** — 認証フィルタ設定、エンドポイント権限
- **テスト**
  - EmployeeService 単体テスト
  - EmployeeController WebMvcTest
  - AuthController WebMvcTest
  - 統合テスト（ログイン → 社員操作）

### Frontend

- ログイン画面
- 社員管理画面（一覧・登録・編集・無効化）
- 認証状態管理（ログイン済み判定・リダイレクト）
- Header にユーザー名・ログアウトボタン表示

## テーブル

- employees（unit_0 で作成済み。ここではデータ操作のみ）
- departments（unit_0 で作成済み）

## API エンドポイント

| メソッド | パス | 権限 |
|---------|------|------|
| POST | /api/auth/login | 全員 |
| POST | /api/auth/logout | 認証済み |
| GET | /api/auth/me | 認証済み |
| GET | /api/employees | ADMIN |
| POST | /api/employees | ADMIN |
| GET | /api/employees/{id} | ADMIN |
| PUT | /api/employees/{id} | ADMIN |
| POST | /api/employees/{id}/deactivate | ADMIN |
| POST | /api/employees/{id}/reset-password | ADMIN |

## 依存

- unit_0_foundation（Entity, Flyway, SecurityConfig 骨格）

## 完了条件

- [ ] ログイン/ログアウトが動作する
- [ ] 管理者が社員を CRUD できる
- [ ] 権限のないユーザーは 403 になる
- [ ] テストカバレッジ 80% 以上
