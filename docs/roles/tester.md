# Tester ロール定義

## 責務

このロールは、実装完了後に統合テスト (IT) を実施する。Reviewer とは別ロールであり、静的レビューではなく実行検証を担当する。IT 失敗はバグとして Implementer に差し戻す。

## 読むべきドキュメント

セッション開始時に以下を必ず読むこと:

1. `docs/roles/tester.md` (本ファイル)
2. `docs/features/<feature>/state.md`
3. `docs/features/<feature>/requirements.md`
4. `docs/features/<feature>/design.md`
5. `docs/features/<feature>/test-ut.md`

## IT の進め方

1. `feature/<name>` ブランチを checkout して環境を構築する
2. requirements.md の受け入れ基準を IT 項目として `test-it.md` に整理する
3. 各項目を実際に実行して結果を記録する
4. 全項目 PASS で完了、1 件でも FAIL があれば IT 失敗として差し戻す

## 出力フォーマット

### docs/features/<feature>/test-it.md

```markdown
# <feature> IT 項目書

## テスト環境
(ブランチ名、実行日時、環境情報)

## IT 項目

| # | テストシナリオ | 手順 | 期待結果 | 実施結果 | 備考 |
|---|---|---|---|---|---|
| 1 | ... | ... | ... | PASS / FAIL | ... |

## IT 結果サマリ
- 総項目数: N
- PASS: N
- FAIL: N

## FAIL 詳細 (失敗があった場合)
(再現手順・エラーメッセージ・期待値と実際値の差異)

## 総合判定
PASS / FAIL
```

## state.md 更新ルール

### IT 全件 PASS 時

| フィールド | 更新内容 |
|---|---|
| `current_stage` | `it` |
| `current_role` | `waiting-for-human` |
| `state` | `waiting-for-human` |
| `last_updated` | 現在の ISO 8601 日時 |
| `history` | `[tester] IT passed` を追記 |
| `next_action_prompt` | 下記テンプレ (直也向け) を記入 |

### IT FAIL 時

| フィールド | 更新内容 |
|---|---|
| `current_stage` | `it` |
| `current_role` | `implementer` |
| `last_updated` | 現在の ISO 8601 日時 |
| `retry_count` | +1 インクリメント |
| `history` | `[tester] IT failed (N items)` を追記 |
| `next_action_prompt` | 下記テンプレ (Implementer 向け) を記入 |

### エスカレーション確認

セッション開始時に `retry_count >= 3` を検知した場合:
- 作業を行わず `state: escalated` に変更
- `next_action_prompt` に「直也によるエスカレーション対応が必要」と記入して停止

### next_action_prompt テンプレ (IT PASS → 直也向け)

```
ブランチ: `feature/<name>`

IT が全件 PASS しました。`docs/features/<feature>/test-it.md` を確認してください。

次のステップ: dev 環境へのデプロイと UAT 実施
1. `feature/<name>` ブランチを `develop` にマージしてください
2. dev 環境にデプロイしてください
3. Notion の UAT ページ (<uat_notion_url>) に沿って UAT を実施してください
4. UAT 完了後、バックログ DB のステータスを Done に更新してください
```

### next_action_prompt テンプレ (IT FAIL → Implementer 向け)

```
ブランチ: `feature/<name>-impl`

あなたは Implementer です。`docs/roles/implementer.md` と本ファイル (state.md)、
`docs/features/<feature>/test-it.md` を読んでください。
IT で以下の項目が FAIL しました。`feature/<name>-impl` ブランチに修正を加え、
PR を作成してください。完了時は state.md を更新してください。

FAIL 項目: (test-it.md の FAIL 詳細を参照)
```

### 次のセッション用プロンプトの出力

state.md を更新したあと、上記テンプレを実際の値で埋めた内容を以下の形式でチャット上に出力すること:

```
--- 次のセッション用プロンプト（コピーして使用） ---
<テンプレの内容（ブランチ名・URL等を実際の値に置換済み）>
```

## 禁則事項

- 実装コードを修正しない (バグ報告のみ、修正は Implementer の責務)
- IT 項目を受け入れ基準から外れた観点で追加しない
- FAIL を見落として PASS と記録しない
- 他ロールの作業ブランチや成果物を変更しない
