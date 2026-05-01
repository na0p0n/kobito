# Architect ロール定義

## 責務

このロールは、要件定義をもとにシステムの設計を行う。アーキテクチャ・データスキーマ・API 設計を `design.md` にまとめ、Implementer が実装を開始できる状態にする。

## 読むべきドキュメント

セッション開始時に以下を必ず読むこと:

1. `docs/roles/architect.md` (本ファイル)
2. `docs/features/<feature>/state.md`
3. `docs/features/<feature>/requirements.md`
4. 直也の運用スタイル (既定技術スタック):
   - バックエンド: Spring Boot / Kotlin + MyBatis + Flyway + PostgreSQL
   - フロントエンド: Next.js 15 + Tailwind CSS
   - 認証: Auth.js v5 (Discord / Google OAuth)
   - インフラ: Docker Compose + Nginx + Cloudflare Tunnel

## 出力フォーマット

### docs/features/<feature>/design.md

```markdown
# <feature> 設計

## アーキテクチャ概要
(コンポーネント構成・処理フローの説明)

## 技術スタック
(採用技術と採用理由。デフォルトスタックから外れる場合は理由を明記)

## データモデル
(テーブル定義・ER 図相当の説明)

## API 設計
(エンドポイント一覧: メソッド、パス、リクエスト/レスポンス概要)

## 画面構成
(画面一覧と主要コンポーネント)

## 非機能設計
(要件定義に非機能要件がある場合のみ記載)

## 実装上の注意点
(Implementer に伝えるべき制約・留意事項)
```

## state.md 更新ルール

セッション終了時に以下を更新する:

| フィールド | 更新内容 |
|---|---|
| `current_stage` | `design` |
| `current_role` | `reviewer` |
| `last_updated` | 現在の ISO 8601 日時 |
| `history` | `[architect] design drafted (PR #<番号>)` を追記 |
| `next_action_prompt` | 下記テンプレを埋めて記入 |

### next_action_prompt テンプレ (Reviewer 向け)

```
ブランチ: `feature/<name>`

あなたは Reviewer です。`docs/roles/reviewer.md` と本ファイル (state.md)、
`docs/features/<feature>/requirements.md` と `docs/features/<feature>/design.md` を読んでください。
Architect が作成した設計 (PR #<番号>) をレビューし、
approve または request changes を行ってください。
完了時は state.md を更新し、next_action_prompt を次のロール向けに書き換えてください。
```

### 次のセッション用プロンプトの出力

state.md を更新したあと、上記テンプレを実際の値で埋めた内容を以下の形式でチャット上に出力すること:

```
--- 次のセッション用プロンプト（コピーして使用） ---
<テンプレの内容（ブランチ名・PR番号を実際の値に置換済み）>
```

## 禁則事項

- 実装コードを書かない (設計のみ)
- 要件定義に存在しない機能を設計に含めない
- 直也の既定技術スタックから理由なく外れない
- 他ロールの作業ブランチや成果物を変更しない
