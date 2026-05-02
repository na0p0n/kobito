---
feature: wakaba
current_branch: feature/wakaba
created_at: 2026-05-01T00:00:00Z
last_updated: 2026-05-02T04:00:00Z
current_stage: implementation
current_role: reviewer
current_pr: 6
retry_count: 0
state: in-progress
uat_notion_url: https://app.notion.com/p/3535213213e781a5b7edfaf7bb1a5fac
uat_backlog_id: 35352132-13e7-8189-96f4-df6d7c163335
---

## idea_summary

**Wakaba (若葉)** — 個人向け OSS 貢献ゴール管理ツール (セルフホスト)

### 背景

個人開発者が OSS へ貢献しようとしても、活動の可視化やゴール設定の仕組みがなく、モチベーションが続きにくい。既存ツールは組織向け (Amazon oss-contribution-tracker / Netflix osstracker) か、GitHub の内蔵統計 (ゴール設定なし) に留まる。

### 概要

- GitHub API で個人の貢献データ (commit / PR / issue / review) を取得・蓄積
- ユーザーが期間とゴールを設定 (例: 「今期 3 つの新しいリポジトリに PR を出す」)
- ゴール進捗ダッシュボード
- 週次 Discord ダイジェスト (webhook)

### 技術スタック

- バックエンド: Spring Boot / Kotlin + MyBatis + Flyway + PostgreSQL
- フロントエンド: Next.js 15 + Tailwind CSS
- 認証: Auth.js v5 (GitHub OAuth)
- インフラ: Docker Compose + Nginx + Cloudflare Tunnel

### 新規性判定

| 判定軸 | 内容 |
|---|---|
| ターゲットユーザー | 個人開発者 (既存は組織向け) |
| 解決課題 | ゴール設定とモチベーション維持 (既存はデータ可視化のみ) |
| UX・提供形式 | Discord 週次ダイジェスト + 個人ゴール進捗 |
| 判定 | **採用** |

## next_action_prompt

ブランチ: `feature/wakaba`

あなたは Reviewer です。`docs/roles/reviewer.md` と本ファイル (state.md)、
`docs/features/wakaba/requirements.md`、`docs/features/wakaba/design.md`、
`docs/features/wakaba/test-ut.md` を読んでください。
Implementer が PR #5 レビュー指摘事項 (3点) を修正した実装 (PR #6) をレビューし、
approve または request changes を行ってください。

修正内容:
1. `DiscordDigestService.sendDigestToAllUsers` — `users` テーブルからユーザーを取得し、ユーザーごとに `buildDigestPayload` を呼び出して Discord に送信するよう修正
2. `SyncService.scheduledSync` — `accounts` テーブルからユーザーID + アクセストークンを取得し、`syncForUser` を呼び出すよう実装
3. `auth.ts callbacks.session` — 2回目以降のセッション (account=undefined) でも `accounts` テーブルから `access_token` を読み込むよう修正

完了時は state.md を更新し、next_action_prompt を次のロール向けに書き換えてください。

## history

- 2026-05-01T00:00:00Z [ideation] feature created, idea: Wakaba (OSS 貢献ゴール管理ツール)
- 2026-05-01T12:00:00Z [pdm] requirements drafted (PR #2)
- 2026-05-01T22:00:00Z [reviewer] PR #2 request changes
- 2026-05-02T00:00:00Z [reviewer] PR #2 approved, merged
- 2026-05-02T12:00:00Z [architect] design drafted (PR #4)
- 2026-05-02T13:00:00Z [reviewer] PR #4 approved, merged
- 2026-05-02T02:40:00Z [implementer] implementation done (PR #5)
- 2026-05-02T03:10:00Z [implementer] fix for IT failure (PR #5): auth.ts accessToken, nginx X-User-Id scope
- 2026-05-02T04:00:00Z [implementer] fix for reviewer request changes (PR #6): DiscordDigestService.sendDigestToAllUsers, SyncService.scheduledSync, auth.ts session callback
