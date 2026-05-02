# wakaba 設計

## アーキテクチャ概要

### コンポーネント構成

```
[Browser]
    │ HTTPS
    ▼
[Nginx] ← Cloudflare Tunnel
    │
    ├──► [Next.js 15 (Frontend)] :3000
    │        └── Auth.js v5 (GitHub OAuth)
    │
    └──► [Spring Boot API] :8080
             ├── GitHubSyncService
             ├── GoalService
             ├── ContributionService
             └── DiscordDigestJob (Spring Scheduler)
                  │
                  └──► [Discord Webhook] (外部)

[PostgreSQL] :5432
[GitHub API] (外部) ← GitHubSyncService が呼び出す
```

### 処理フロー

**認証**

1. ブラウザ → Next.js: GitHub OAuth ログイン要求
2. Auth.js v5 が GitHub OAuth フローを処理し、セッション確立
3. `accounts` テーブルに GitHub `access_token` を保存
4. Next.js Server Action → バックエンド API: `X-User-Id` ヘッダーで認証

**データ同期 (手動 + 定時)**

1. ユーザーが「同期」ボタン押下 または Spring Scheduler 発火 (毎時 0 分)
2. GitHubSyncService が GitHub GraphQL API (`contributionsCollection`) を呼び出し
3. commit / PR / issue / review の日別カウントを `contributions` テーブルに upsert
4. ゴール進捗は都度 `contributions` から集計して返す

**週次 Discord Digest**

1. Spring Scheduler: 毎週月曜 09:00 JST に発火
2. DiscordDigestJob が前週 (月〜日) の contribution 集計とゴール進捗を取得
3. Discord Webhook URL (`app_config`) に Embed 形式で POST

---

## 技術スタック

| レイヤー | 採用技術 | 備考 |
|---|---|---|
| フロントエンド | Next.js 15 + Tailwind CSS | 標準スタック |
| 認証 | Auth.js v5 — **GitHub OAuth** | GitHub API アクセスが必要なため Discord/Google から変更 |
| バックエンド | Spring Boot / Kotlin + MyBatis + Flyway | 標準スタック |
| DB | PostgreSQL | 標準スタック |
| スケジューラ | Spring `@Scheduled` | Spring Boot 組み込み、追加依存なし |
| GitHub データ取得 | GitHub GraphQL API v4 | `contributionsCollection` で commit/PR/issue/review を一括取得。REST v3 より効率的でレート制限消費が少ない |
| HTTP クライアント | Spring `RestClient` (Spring Boot 3.2+) | Discord Webhook 送信用 |
| インフラ | Docker Compose + Nginx + Cloudflare Tunnel | 標準スタック |

**標準スタックからの変更点:** Auth.js のプロバイダを GitHub に変更。GitHub `access_token` をセッションに保持するため `callbacks.session` のカスタマイズが必要。

---

## データモデル

### Auth.js Adapter テーブル (V1 マイグレーション)

Auth.js v5 PostgreSQL Adapter が要求する標準テーブル (`users`, `accounts`, `sessions`, `verification_tokens`) を Flyway V1 で定義する。

### アプリケーションテーブル (V2 マイグレーション)

#### `contributions`

1行 = 1ユーザー × 1日 × 1タイプの集計値。

| カラム | 型 | 制約 |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `user_id` | UUID | FK → users.id, NOT NULL |
| `contribution_date` | DATE | NOT NULL |
| `contribution_type` | VARCHAR(16) | NOT NULL — `COMMIT` / `PR` / `ISSUE` / `REVIEW` |
| `count` | INT | NOT NULL, DEFAULT 0 |
| `synced_at` | TIMESTAMPTZ | NOT NULL |

UNIQUE: `(user_id, contribution_date, contribution_type)`

#### `goals`

| カラム | 型 | 制約 |
|---|---|---|
| `id` | BIGSERIAL | PK |
| `user_id` | UUID | FK → users.id, NOT NULL |
| `title` | VARCHAR(255) | NOT NULL |
| `contribution_type` | VARCHAR(16) | NOT NULL — `COMMIT` / `PR` / `ISSUE` / `REVIEW` |
| `target_count` | INT | NOT NULL |
| `start_date` | DATE | NOT NULL |
| `end_date` | DATE | NOT NULL |
| `created_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |

達成判定は `SELECT SUM(count) FROM contributions WHERE user_id = ? AND contribution_type = ? AND contribution_date BETWEEN start_date AND end_date` で都度算出する (永続化しない)。

#### `app_config`

| カラム | 型 | 制約 |
|---|---|---|
| `key` | VARCHAR(64) | PK |
| `value` | TEXT | |
| `updated_at` | TIMESTAMPTZ | NOT NULL, DEFAULT NOW() |

初期レコード:

| key | value |
|---|---|
| `discord_webhook_url` | (空) |
| `digest_send_day` | `MONDAY` |
| `digest_send_hour` | `9` |

### ER 概要

```
users (1) ──< contributions (N)
users (1) ──< goals (N)
app_config (独立)
```

---

## API 設計

バックエンド Spring Boot が `/api/` 以下を提供。Next.js Server Action 経由で呼び出す。

認証: Nginx が外部からの `X-User-Id` ヘッダーを除去。Next.js Server Action が `auth()` でセッションを取得し `X-User-Id` をバックエンドへ付与する。バックエンドはこのヘッダーを信頼する。

### 同期

| メソッド | パス | リクエスト | レスポンス |
|---|---|---|---|
| `POST` | `/api/sync` | なし | `{ syncedAt: string, totalRecords: number }` |
| `GET` | `/api/sync/status` | なし | `{ lastSyncedAt: string \| null }` |

同期範囲: 直近 365 日。GitHub GraphQL `contributionsCollection` を `from`/`to` で呼び出す。

### Contribution

| メソッド | パス | リクエスト | レスポンス |
|---|---|---|---|
| `GET` | `/api/contributions/summary` | Query: `from=YYYY-MM-DD&to=YYYY-MM-DD` | `{ items: [{ date, commitCount, prCount, issueCount, reviewCount }] }` |
| `GET` | `/api/contributions/weekly` | なし | `{ week: { commit, pr, issue, review } }` |

### Goal

| メソッド | パス | リクエスト | レスポンス |
|---|---|---|---|
| `GET` | `/api/goals` | なし | `{ goals: [GoalWithProgress] }` |
| `POST` | `/api/goals` | `{ title, contributionType, targetCount, startDate, endDate }` | 作成した `GoalWithProgress` |
| `DELETE` | `/api/goals/{id}` | なし | 204 No Content |

`GoalWithProgress` レスポンス形式:

```json
{
  "id": 1,
  "title": "月30PR",
  "contributionType": "PR",
  "targetCount": 30,
  "startDate": "2026-05-01",
  "endDate": "2026-05-31",
  "currentCount": 12,
  "achieved": false
}
```

### 設定

| メソッド | パス | リクエスト | レスポンス |
|---|---|---|---|
| `GET` | `/api/config` | なし | `{ discordWebhookUrl: string \| null, digestSendDay: string, digestSendHour: number }` |
| `PUT` | `/api/config` | `{ discordWebhookUrl?: string }` | 更新後の設定値 |
| `POST` | `/api/digest/trigger` | なし | `{ sent: boolean }` (手動テスト送信用) |

---

## 画面構成

| 画面 | パス | 説明 |
|---|---|---|
| ログイン | `/` | GitHub OAuth ログインボタン (未認証時) |
| ダッシュボード | `/dashboard` | アクティブ目標進捗 + 直近7日 contribution サマリ + 同期ボタン |
| 目標管理 | `/goals` | 目標一覧 + 新規作成フォーム |
| 設定 | `/settings` | Discord Webhook URL + テスト送信 |

### 主要コンポーネント

**ダッシュボード (`/dashboard`)**

- `UserHeader`: GitHub アバター + ログイン名 + ログアウトボタン
- `SyncButton`: 「同期」ボタン + 最終同期日時表示。クリックで `POST /api/sync` を実行。同期中は disabled
- `WeeklySummaryCard`: 直近7日の commit / PR / issue / review カウント
- `GoalProgressCard`: タイトル / 種別 / 進捗バー (`currentCount / targetCount`) / 達成バッジ

**目標管理 (`/goals`)**

- `GoalCreateForm`: タイトル / 種別セレクト / 目標数 / 開始日・終了日 の入力フォーム (Server Action → `POST /api/goals`)
- `GoalList`: 全目標一覧 (アクティブ + 期間終了済み)
- `GoalRow`: 目標行 + 削除ボタン

**設定 (`/settings`)**

- `WebhookUrlForm`: Discord Webhook URL 入力フォーム (Server Action → `PUT /api/config`)
- `DigestTestButton`: 「テスト送信」ボタン (`POST /api/digest/trigger`)
- `DigestScheduleInfo`: 「毎週月曜 09:00 JST」を読み取り専用表示

---

## 非機能設計

### GitHub API レート制限

- 1回の同期 = GitHub GraphQL 最大4リクエスト (4タイプ分)。5000 req/h 制限に対して問題なし。
- 同期中は `SyncButton` を disabled にして多重実行を防止する。

### セキュリティ

- Nginx で外部からの `X-User-Id` ヘッダーを除去し、バックエンドが信頼するヘッダーは Next.js Server Action 経由のみとする。
- Discord Webhook 送信時は貢献数値のみ送信。GitHub `access_token` は外部に出さない。
- `GITHUB_CLIENT_SECRET` / `AUTH_SECRET` は環境変数で管理する。

### 自己ホスト

- `docker-compose.yml` に `db` / `backend` / `frontend` / `nginx` の4サービスを定義。
- 必須環境変数: `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`, `AUTH_SECRET`, `DATABASE_URL`
- オプション環境変数: `DISCORD_WEBHOOK_URL` (起動時に `app_config` の初期値として投入)

---

## 実装上の注意点

### 未決事項の決定

**1. データ同期トリガー: 手動 + スケジュール (両方)**

手動ボタンは「今すぐ確認したい」ユースケース。スケジュールは **毎時 0 分** に自動実行。  
理由: モチベーション維持ツールとして「今日出した PR がその日のうちに進捗に反映される」ことが重要。GitHub GraphQL rate limit (5000 req/h) に対して1回4リクエストなので毎時でも問題なし。  
実装: `@Scheduled(cron = "0 0 * * * *", zone = "Asia/Tokyo")`

**2. 週次 Digest 送信日時: 毎週月曜 09:00 JST 固定 (UI 変更不可)**

`app_config` には `digest_send_day` / `digest_send_hour` を格納するが、今フェーズでは変更 API・UI を設けない (将来の拡張余地として DB に保持のみ)。  
実装: `@Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Tokyo")`

### Auth.js v5 実装注意

- PostgreSQL Adapter を使用。V1 マイグレーションに Auth.js 標準スキーマを含めること。
- `callbacks.session` で `account.access_token` を session に付与し、GitHubSyncService から参照できるようにする。
- フロントエンドから Spring Boot API を呼び出す際、Next.js Server Action 内で `auth()` を使ってセッションを取得し、`user.id` を `X-User-Id` ヘッダーに付与すること。

### GitHub GraphQL API

- `contributionsCollection` クエリを使用する。`from`/`to` で最大1年を1クエリで取得可能。
- タイプ別フィールド: `commitContributionsByRepository` / `pullRequestContributions` / `issueContributions` / `pullRequestReviewContributions`
- commit の日別データは `commitContributionsByRepository` の各リポジトリ内 `contributions { nodes { occurredAt } }` で取得し、日付ごとに集計する。

### Flyway マイグレーション順序

1. `V1__auth_js_schema.sql` — Auth.js Adapter 標準テーブル (users, accounts, sessions, verification_tokens)
2. `V2__app_tables.sql` — contributions, goals, app_config

### スコープ外 (実装しないこと)

- 複数ユーザー対応 (単一ユーザー前提)
- GitHub 以外の contribution ソース
- 目標の編集機能 (削除・再作成で対応)
- Digest 送信時刻の UI 変更機能
