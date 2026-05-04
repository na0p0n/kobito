---
feature: wakaba
current_branch: feature/wakaba
created_at: 2026-05-01T00:00:00Z
last_updated: 2026-05-04T09:45:00Z
current_stage: it
current_role: implementer
current_pr: 6
retry_count: 1
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

あなたは Implementer です。`docs/roles/implementer.md` と本ファイル (state.md)、
`docs/features/wakaba/test-it.md` を読んでください。
IT で以下の項目が FAIL しました。`feature/wakaba` ブランチに修正を加え、
PR を作成してください。完了時は state.md を更新してください。

FAIL 項目: (test-it.md の FAIL 詳細を参照)

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
- 2026-05-02T05:00:00Z [reviewer] PR #6 approved, merged
- 2026-05-04T09:45:00Z [tester] IT failed (2 items): FAIL-A MyBatis UUID TypeHandler, FAIL-B auth.ts path resolution
