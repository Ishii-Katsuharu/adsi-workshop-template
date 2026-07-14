# Unit 0: 共通基盤

## 目的

プロジェクトの骨格を構築し、全Unitの前提となる共通コードとインフラを整備する。
このUnitが完了した時点で、テスト実行・アプリ起動ができる状態にする。

## スコープ

### Backend（Spring Boot）

- プロジェクト初期化（Spring Initializr 相当）
  - Spring Boot 3.x
  - 依存: Web, JPA, Flyway, PostgreSQL, Security, Validation, Lombok, JSpecify
  - テスト: JUnit 5, Mockito, H2（テスト用）
- Flyway マイグレーション（全テーブル: V1〜V6）
- Entity クラス（フィールド + JPA アノテーションのみ。ビジネスロジックは各Unitで追加）
  - Department, Employee, Attendance, BreakRecord, LeaveRequest
- Enum 定義
  - Role, ApprovalStatus, LeaveType
- 共通設定
  - `application.yml`（dev / test プロファイル）
  - PostgreSQL（dev）/ H2（test）
  - Jackson 設定（LocalDate/LocalTime のフォーマット）
- 共通エラーハンドリング
  - `GlobalExceptionHandler`（`@RestControllerAdvice`）
  - `ErrorResponse` record
  - 業務例外クラス（`ResourceNotFoundException`, `BusinessRuleException`）
- SecurityConfig 骨格
  - 全エンドポイント permitAll（Phase B で認証を実装）
  - BCryptPasswordEncoder Bean
- ArchUnit テスト
  - レイヤー違反検出

### Frontend（Next.js）

- プロジェクト初期化
  - Next.js 14+ (App Router)
  - TypeScript strict mode
  - Tailwind CSS
  - テスト: Vitest + Testing Library
- 共通レイアウト
  - Header / Sidebar コンポーネント（仮のナビゲーション）
- API クライアント基盤
  - `withBasePath()` ユーティリティ
  - fetch ラッパー（エラーハンドリング・型変換）
  - 共通型定義（ErrorResponse 等）
- 開発サーバー設定
  - proxy 設定（`/api` → backend:8080）
  - SageMaker 用 `dev:sagemaker` スクリプト

### インフラ

- `docker-compose.yml`（PostgreSQL）
- `.env.example`

## テーブル（Flyway）

全テーブルをこの Unit で作成する:
- V1: departments
- V2: employees
- V3: attendances
- V4: break_records
- V5: leave_requests
- V6: departments.manager_id FK

## API

なし（このUnitでは API エンドポイントを実装しない）

## 完了条件

- [ ] `./mvnw test` が成功する（ArchUnit + コンテキスト起動テスト）
- [ ] `./mvnw spring-boot:run` でアプリが起動する
- [ ] Flyway マイグレーションが実行され全テーブルが作成される
- [ ] Frontend の `npm run dev` で画面が表示される
- [ ] `npm test` が成功する
