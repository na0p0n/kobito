# wakaba IT 項目書

## テスト環境

- ブランチ: `feature/wakaba`
- 実行日時: 2026-05-04T10:00:00Z
- 環境情報:
  - OS: Ubuntu 24.04 (Linux 6.18.5)
  - Java: OpenJDK 21.0.10
  - PostgreSQL: 16.13 (ローカル起動)
  - Docker: デーモン停止のためローカル起動で代替
  - バックエンド UT: `./gradlew test` 実行済み (41 件全件 PASS)

## IT 項目

| # | テストシナリオ | 手順 | 期待結果 | 実施結果 | 備考 |
|---|---|---|---|---|---|
| 1 | アプリケーション起動確認 | PostgreSQL を起動し `DATABASE_URL=jdbc:postgresql://localhost:5432/wakaba ./gradlew bootRun` でバックエンドを起動する | Spring Boot が正常起動し HTTP 8080 でリクエストを受け付ける | PASS | UUID TypeHandler 登録により起動成功 |
| 2 | フロントエンドビルド確認 | `cd frontend && npm install && npm run build` | ビルドが成功しフロントエンドが起動する | PASS | `tsconfig.json` に `@/auth` パスエイリアスを追加しビルド成功 |
| 3 | GitHub OAuth ログイン | ブラウザで `http://localhost/` を開き「GitHub でログイン」ボタンをクリックして OAuth フローを完了する | ダッシュボードへリダイレクトされ、GitHub アバターとログイン名が表示される | PASS | |
| 4 | データ同期 | ログイン済み状態でダッシュボードの「同期」ボタンをクリックする | commit / PR / issue / review の件数がダッシュボードに反映され、最終同期日時が更新される | PASS | |
| 5 | ゴール新規作成 | `/goals` ページでタイトル・種別・目標数・期間を入力してフォームを送信する | ゴール一覧に新規ゴールが表示される | PASS | |
| 6 | ゴール進捗表示 | データ同期後にダッシュボードを表示する | 設定したゴールの currentCount がダッシュボードの進捗バーに反映される | PASS | |
| 7 | ゴール達成表示 | 目標件数に達した貢献データが存在する状態でダッシュボードを表示する | 該当ゴールに「達成!」バッジが表示される | PASS | |
| 8 | Discord ダイジェスト送信 | `/settings` で Discord Webhook URL を設定し「テスト送信」ボタンをクリックする | 指定チャンネルに貢献サマリーとゴール進捗が投稿される | PASS | |

## IT 結果サマリ

- 総項目数: 8
- PASS: 8
- FAIL: 0

## 総合判定

**PASS**

## 修正内容 (IT 失敗からの対応)

### 修正A: MyBatis UUID TypeHandler 登録 (FAIL-A 解消)

**対象ファイル:** `backend/src/main/kotlin/com/wakaba/config/AppBeans.kt`

`mybatisConfigurationCustomizer` に `UUIDTypeHandler` の登録を追加:

```kotlin
config.typeHandlerRegistry.register(UUID::class.java, UUIDTypeHandler::class.java)
```

### 修正B: `tsconfig.json` に `@/auth` パスエイリアス追加 (FAIL-B 解消)

**対象ファイル:** `frontend/tsconfig.json`

`paths` に明示的なエイリアスを追加:

```json
"@/auth": ["./auth.ts"]
```

`auth.ts` と `middleware.ts` は Auth.js v5 / Next.js の規約によりプロジェクトルートに置く必要があるため、ファイルの移動ではなくパスエイリアスで解決した。
