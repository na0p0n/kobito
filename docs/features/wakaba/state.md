---
feature: wakaba
current_branch: feature/wakaba
created_at: 2026-05-01T00:00:00Z
last_updated: 2026-05-01T22:00:00Z
current_stage: requirements
current_role: pdm
current_pr: 2
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
|---|—|
| ターゲットユーザー | 個人開発者 (既存は組織向け) |
| 解決課題 | ゴール設定とモチベーション維持 (既存はデータ可視化のみ) |
| UX・提供形式 | Discord 週次ダイジェスト + 個人ゴール進捗 |
| 判定 | **採用** |

## next_action_prompt

あなたは PdM です。`docs/roles/pdm.md` と本ファイル (state.md)、
`docs/features/wakaba/requirements.md` を読んでください。
PR #2 に対するレビューで request changes が出ています。
GitHub PR #2 のコメントを確認し、以下の指摘事項を修正した上で PR を更新してください。

### 修正が必要な指摘事項

1. **スコープ「In Scope」の How 混入**: 「GitHub API を用いた」「Docker Compose による」を除去し、What のみで記述してください。
2. **非機能要件の How 混入**: 「Docker Compose 一式で構成し」「PostgreSQL」等の実装技術を除去し、品質特性のみを記述してください。
3. **受け入れ基準 7 の実装詳細混入**: 「Docker Compose で起動したとき」「DB / バックエンド / フロントエンド / Nginx」を除去し、アウトカムベースの記述にしてください。
4. **受け入れ基準 5 と未決事項の不整合**: 「ゴール達成条件の粒度（PR のマージ必須か、オープンで可か）」を先に解消した上で受け入れ基準 5 を確定させるか、達成条件の種別を明記した形に修正してください。

修正完了後、PR を更新し state.md の `current_role` を `reviewer`、`retry_count` を維持したまま更新してください。

## history

- 2026-05-01T00:00:00Z [ideation] feature created, idea: Wakaba (OSS 貢献ゴール管理ツール)
- 2026-05-01T12:00:00Z [pdm] requirements drafted (PR #2)
- 2026-05-01T22:00:00Z [reviewer] PR #2 request changes
