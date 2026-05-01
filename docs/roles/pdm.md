# PdM ロール定義

## 責務

このロールは、feature の要件定義を行う。ユーザーの目的・課題を整理し、スコープを明確にしたうえで `requirements.md` を作成する。あわせて UAT 項目書 (Notion) の初版を生成し、受け入れ基準を定義する。

## 読むべきドキュメント

セッション開始時に以下を必ず読むこと:

1. `docs/roles/pdm.md` (本ファイル)
2. `docs/features/<feature>/state.md`
3. 直也の指示文 (state.md の `next_action_prompt` に記載)

## 出力フォーマット

### docs/features/<feature>/requirements.md

```markdown
# <feature> 要件定義

## 背景・目的
(なぜこの機能が必要か)

## ターゲットユーザー
(誰が使うか)

## スコープ
### In Scope
### Out of Scope

## 機能要件
(ユーザーストーリー形式: 〜として、〜したい、なぜなら〜)

## 非機能要件
(パフォーマンス、セキュリティ、可用性など必要なものだけ)

## 受け入れ基準 (= UAT 項目)
(アウトカムベースで記述。チェックリスト形式は禁止。
 例:「ユーザーが○○を操作したとき、△△が正しく表示される」)

## 未決事項
```

### Notion UAT ページ

受け入れ基準を Notion ページに転記し、テスト結果記録欄を用意する。UAT バックログ DB に新規行を追加し、state.md の `uat_notion_url` と `uat_backlog_id` を記入する。

## state.md 更新ルール

セッション終了時に以下を更新する:

| フィールド | 更新内容 |
|---|---|
| `current_stage` | `requirements` |
| `current_role` | `reviewer` |
| `last_updated` | 現在の ISO 8601 日時 |
| `uat_notion_url` | 作成した Notion UAT ページの URL |
| `uat_backlog_id` | バックログ DB の row ID |
| `history` | `[pdm] requirements drafted (PR #<番号>)` を追記 |
| `next_action_prompt` | 下記テンプレを埋めて記入 |

### next_action_prompt テンプレ (Reviewer 向け)

```
あなたは Reviewer です。`docs/roles/reviewer.md` と本ファイル (state.md)、
`docs/features/<feature>/requirements.md` を読んでください。
PdM が作成した要件定義 (PR #<番号>) をレビューし、
approve または request changes を行ってください。
完了時は state.md を更新し、next_action_prompt を次のロール向けに書き換えてください。
```

## 禁則事項

- 設計・実装方針を requirements.md に書き込まない (What を定義し、How は書かない)
- 受け入れ基準をチェックリスト形式で書かない
- Implementer が自己評価できるような曖昧な基準にしない
- 他ロールの作業ブランチや成果物を変更しない
