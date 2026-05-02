# wakaba UT 項目書

## テスト対象

| クラス | 責務 |
|---|---|
| `GoalService` | ゴール一覧取得・作成・削除、進捗計算 |
| `ContributionService` | 日別/週次貢献集計、最終同期日時取得 |
| `ConfigService` | アプリ設定取得・Discord Webhook URL 更新 |
| `SyncService#parseContributions` | GitHub GraphQL レスポンスから Contribution への変換 |
| `DiscordDigestService#buildDigestPayload` | Discord 送信ペイロード生成、手動送信トリガー |

## UT 項目

| # | テスト対象 | テストケース | 期待結果 | 実施結果 |
|---|---|---|---|---|
| 1 | `GoalService#getGoalsWithProgress` | ゴールが0件のとき | 空リストを返す | PASS |
| 2 | `GoalService#getGoalsWithProgress` | currentCount < targetCount のとき | achieved = false | PASS |
| 3 | `GoalService#getGoalsWithProgress` | currentCount == targetCount のとき | achieved = true | PASS |
| 4 | `GoalService#getGoalsWithProgress` | currentCount > targetCount のとき | achieved = true | PASS |
| 5 | `GoalService#createGoal` | 正常パラメータで作成 | GoalWithProgress を返し GoalMapper#insert が呼ばれる | PASS |
| 6 | `GoalService#createGoal` | targetCount = 0 のとき | IllegalArgumentException をスロー | PASS |
| 7 | `GoalService#createGoal` | targetCount < 0 のとき | IllegalArgumentException をスロー | PASS |
| 8 | `GoalService#createGoal` | endDate < startDate のとき | IllegalArgumentException をスロー | PASS |
| 9 | `GoalService#createGoal` | startDate == endDate のとき | 正常に作成される | PASS |
| 10 | `GoalService#deleteGoal` | 存在するゴールを削除 | true を返す | PASS |
| 11 | `GoalService#deleteGoal` | 存在しないゴールを削除 | false を返す | PASS |
| 12 | `GoalService#deleteGoal` | 他ユーザーのゴールを削除 | false を返す | PASS |
| 13 | `ContributionService#getSummary` | データなし | 全日付に0カウントのエントリを返す | PASS |
| 14 | `ContributionService#getSummary` | 各タイプのデータあり | commit/pr/issue/review が正しく集計される | PASS |
| 15 | `ContributionService#getSummary` | from == to の単一日付 | 1件のエントリを返す | PASS |
| 16 | `ContributionService#getSummary` | 複数日付 | 日付ごとにデータが分割される | PASS |
| 17 | `ContributionService#getWeeklyTotal` | データなし | 全項目0を返す | PASS |
| 18 | `ContributionService#getWeeklyTotal` | 複数タイプ・複数日のデータあり | タイプ別の合計が正しく集計される | PASS |
| 19 | `ContributionService#getLastSyncedAt` | 同期履歴なし | null を返す | PASS |
| 20 | `ContributionService#getLastSyncedAt` | 同期履歴あり | 日時文字列を返す | PASS |
| 21 | `ConfigService#getConfig` | webhook URL が設定済み | 正しい URL と送信設定を返す | PASS |
| 22 | `ConfigService#getConfig` | webhook URL が null | discordWebhookUrl = null を返す | PASS |
| 23 | `ConfigService#getConfig` | digest_send_hour が数値でない | digestSendHour = 9 (デフォルト) を返す | PASS |
| 24 | `ConfigService#getConfig` | レコードなし | デフォルト値を返す | PASS |
| 25 | `ConfigService#updateDiscordWebhookUrl` | URL を更新 | AppConfigMapper#update が呼ばれ更新後の設定を返す | PASS |
| 26 | `ConfigService#updateDiscordWebhookUrl` | null を渡す | webhook URL が null になる | PASS |
| 27 | `SyncService#parseContributions` | data なし | 空リストを返す | PASS |
| 28 | `SyncService#parseContributions` | data キーなし | 空リストを返す | PASS |
| 29 | `SyncService#parseContributions` | PR データあり | 日付ごとに集計された PR の Contribution リストを返す | PASS |
| 30 | `SyncService#parseContributions` | Issue データあり | 日付ごとに集計された Issue の Contribution リストを返す | PASS |
| 31 | `SyncService#parseContributions` | Review データあり | Review の Contribution リストを返す | PASS |
| 32 | `SyncService#parseContributions` | Commit が複数リポジトリ | 同日の Commit が合算される | PASS |
| 33 | `SyncService#parseContributions` | userId の設定確認 | 全エントリの userId が引数と一致する | PASS |
| 34 | `DiscordDigestService#buildDigestPayload` | contribution なし | 0カウントのペイロードを返す | PASS |
| 35 | `DiscordDigestService#buildDigestPayload` | contribution あり | 各タイプの正しいカウントを含む | PASS |
| 36 | `DiscordDigestService#buildDigestPayload` | ゴールあり | 達成数 / 総数の進捗情報を含む | PASS |
| 37 | `DiscordDigestService#buildDigestPayload` | ゴールなし | ゴール進捗情報を含まない | PASS |
| 38 | `DiscordDigestService#triggerManual` | webhook URL = null | false を返す | PASS |
| 39 | `DiscordDigestService#triggerManual` | webhook URL = 空文字 | false を返す | PASS |

## カバレッジ方針

### カバーするもの

- サービス層のビジネスロジック全体 (5クラス、全パブリックメソッド)
- 境界値・異常系 (targetCount = 0、日付逆転、レコードなし等)
- 型変換・集計ロジック (GraphQL レスポンスのパース、日別集計)

### カバーしないもの

- データベースとの結合 (MyBatis Mapper は全件 Mock)
- GitHub API / Discord Webhook への外部 HTTP 呼び出し (RestClient は Mock)
- Next.js フロントエンドコンポーネント (UT フレームワーク未整備、IT で確認)
- Spring Boot の起動テスト・統合テスト (`@SpringBootTest` は IT フェーズで実施)
- Flyway マイグレーション (DB が必要なため IT フェーズで確認)
