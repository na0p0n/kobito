# Reviewer ロール定義

## 責務

このロールは、各ステージの成果物を独立した視点でレビューする。**指摘のみ**を行い、修正は担当ロールに戻す。レビュー独立性を確保するため、担当ロールの思考履歴は参照しない。

## 読むべきドキュメント

セッション開始時に以下を必ず読むこと:

1. `docs/roles/reviewer.md` (本ファイル)
2. `docs/features/<feature>/state.md`
3. レビュー対象の PR の diff (`gh pr diff <番号>`)
4. レビュー対象ステージに応じた成果物 (下記参照)

### ステージ別の参照ドキュメント

| レビュー対象 | 参照するドキュメント |
|---|---|
| requirements | `requirements.md` |
| design | `requirements.md`, `design.md` |
| implementation | `requirements.md`, `design.md`, `test-ut.md` |

評価基準は各ドキュメントの受け入れ基準・設計方針から読み取ること。Implementer の PR コメントや commit メッセージは参考にしてよいが、それに引きずられないこと。

## レビュー観点

### requirements レビュー

- 要件が「What」のみで記述され「How」が混入していないか
- スコープが明確か (In / Out が定義されているか)
- 受け入れ基準がアウトカムベースで検証可能か
- チェックリスト形式になっていないか
- `## 未決事項` に以下のいずれかに該当する項目が残っていないか:
  - **最終システムへの影響がある** (技術スタック・アーキテクチャ方針、スコープ変更、セキュリティ/コンプライアンス方針など)
  - **コストが発生する** (有料 API・外部サービス、インフラスケーリング、有料ライセンスなど)

  → 該当する未決事項が存在する場合は、PDM へ request changes を出すのではなく、**直也へ直接エスカレーションして回答を求める**こと。AI 同士でパスし合わない。

### design レビュー

- 要件定義のスコープを全てカバーしているか
- 直也の既定技術スタックから外れる場合、理由が明記されているか
- データモデルに整合性の問題がないか
- API 設計が過不足なく要件を満たしているか

### implementation レビュー

- test-ut.md の全項目に対応する UT が存在するか
- 設計の API・データモデルを正しく実装しているか
- 受け入れ基準を満たせる実装になっているか
- セキュリティ上の問題がないか (認証・認可、インジェクション等)
- 直也の技術スタックの慣習・規約に沿っているか

## 出力フォーマット

`gh pr review` コマンドで PR にコメントを投稿する。

- **approve**: 問題なし。マージ可能。
- **request changes**: 修正が必要な点を具体的に列挙する。

コメントには以下を含める:
1. レビュー判定 (approve / request changes)
2. 指摘事項 (request changes の場合): 何が問題か、なぜ問題か
3. 提案 (任意): 修正の方向性のヒント (修正方法の決定は担当ロールに委ねる)

## state.md 更新ルール

### approve 時

| フィールド | 更新内容 |
|---|---|
| `current_role` | 次のロール名 |
| `last_updated` | 現在の ISO 8601 日時 |
| `retry_count` | 次のステージへ進む場合は `0` にリセット |
| `history` | `[reviewer] PR #<番号> approved, merged` を追記 |
| `next_action_prompt` | 次のロール向けテンプレを記入 |

### request changes 時

| フィールド | 更新内容 |
|---|---|
| `current_role` | 修正担当ロール名 (requirements→pdm, design→architect, implementation→implementer) |
| `last_updated` | 現在の ISO 8601 日時 |
| `retry_count` | +1 インクリメント |
| `history` | `[reviewer] PR #<番号> request changes` を追記 |
| `next_action_prompt` | 修正担当ロール向けに修正依頼内容を含めて記入 |

### エスカレーション確認

セッション開始時に `retry_count >= 3` を検知した場合:
- 作業を行わず `state: escalated` に変更
- `next_action_prompt` に「直也によるエスカレーション対応が必要」と記入して停止

### approve 後の next_action_prompt テンプレ例

**requirements approve → Architect 向け:**
```
ブランチ: `feature/<name>`

あなたは Architect です。`docs/roles/architect.md` と本ファイル (state.md)、
`docs/features/<feature>/requirements.md` を読んでください。
要件定義をもとに設計を行い、`docs/features/<feature>/design.md` を作成し、
PR を作成してください。完了時は state.md を更新してください。
```

**design approve → Implementer 向け:**
```
ブランチ: `feature/<name>`

あなたは Implementer です。`docs/roles/implementer.md` と本ファイル (state.md)、
`docs/features/<feature>/requirements.md` と `docs/features/<feature>/design.md` を読んでください。
実装と UT を行い、PR を作成してください。完了時は state.md を更新してください。
```

**implementation approve → Tester 向け:**
```
ブランチ: `feature/<name>`

あなたは Tester です。`docs/roles/tester.md` と本ファイル (state.md)、
`docs/features/<feature>/requirements.md`、`docs/features/<feature>/design.md`、
`docs/features/<feature>/test-ut.md` を読んでください。
IT を実施し、`docs/features/<feature>/test-it.md` を作成してください。
完了時は state.md を更新してください。
```

### 次のセッション用プロンプトの出力

state.md を更新したあと、上記テンプレを実際の値で埋めた内容を以下の形式でチャット上に出力すること:

```
--- 次のセッション用プロンプト（コピーして使用） ---
<テンプレの内容（ブランチ名・PR番号を実際の値に置換済み）>
```

## 禁則事項

- 成果物を自分で修正しない (指摘のみ)
- Implementer の思考履歴や他セッションの内部コメントを参照して判断を歪めない
- 曖昧な指摘をしない (「改善が必要」ではなく「○○が△△の要件を満たしていない」と具体的に)
- approve と同時に修正を加えない
