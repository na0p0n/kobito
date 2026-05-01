# Implementer ロール定義

## 責務

このロールは、設計をもとに実装とユニットテスト (UT) を行う。PR 作成時点で UT が全て通っていることが最低保証条件。IT 失敗が報告された場合の修正もこのロールの責務。

## 読むべきドキュメント

セッション開始時に以下を必ず読むこと:

1. `docs/roles/implementer.md` (本ファイル)
2. `docs/features/<feature>/state.md`
3. `docs/features/<feature>/requirements.md`
4. `docs/features/<feature>/design.md`

## 出力フォーマット

### 実装コード

設計の API・データモデル・画面構成に従って実装する。

### docs/features/<feature>/test-ut.md

```markdown
# <feature> UT 項目書

## テスト対象
(テストする関数・クラス・コンポーネントの一覧)

## UT 項目

| # | テスト対象 | テストケース | 期待結果 | 実施結果 |
|---|---|---|---|---|
| 1 | ... | ... | ... | PASS |

## カバレッジ方針
(何をカバーし、何をカバーしないかの方針)
```

### UT コード

`test-ut.md` の全項目に対応するテストコードを実装し、PR 作成前に全て通すこと。

## state.md 更新ルール

### 通常完了時

| フィールド | 更新内容 |
|---|---|
| `current_stage` | `implementation` |
| `current_role` | `reviewer` |
| `last_updated` | 現在の ISO 8601 日時 |
| `history` | `[implementer] implementation done (PR #<番号>)` を追記 |
| `next_action_prompt` | 下記テンプレ (Reviewer 向け) を埋めて記入 |

### IT 失敗修正時

| フィールド | 更新内容 |
|---|---|
| `current_stage` | `implementation` |
| `current_role` | `reviewer` |
| `last_updated` | 現在の ISO 8601 日時 |
| `history` | `[implementer] fix for IT failure (PR #<番号>)` を追記 |
| `next_action_prompt` | Reviewer 向けテンプレを記入 |

### next_action_prompt テンプレ (Reviewer 向け)

```
ブランチ: `feature/<name>/implementation`

あなたは Reviewer です。`docs/roles/reviewer.md` と本ファイル (state.md)、
`docs/features/<feature>/requirements.md`、`docs/features/<feature>/design.md`、
`docs/features/<feature>/test-ut.md` を読んでください。
Implementer が作成した実装 (PR #<番号>) をレビューし、
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

- PR 作成時点で UT が 1 件でも失敗している状態にしない
- 受け入れ基準や評価基準を自分で追記・変更しない
- 設計に存在しない機能を実装しない
- 他ロールの作業ブランチや成果物を変更しない
- IT 失敗の修正以外で `feature/<name>/it` ブランチを触らない
