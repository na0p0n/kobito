# wakaba IT 項目書

## テスト環境

- ブランチ: `feature/wakaba`
- 実行日時: 2026-05-02T02:50:00Z
- 環境情報:
  - OS: Ubuntu 24.04 (Linux 6.18.5)
  - Java: OpenJDK 21.0.10
  - Node.js: v22.22.2
  - PostgreSQL: 16.13
  - Docker: 利用不可 (daemon なし) → ローカル起動で代替

## IT 項目

| # | テストシナリオ | 手順 | 期待結果 | 実施結果 | 備考 |
|---|---|---|---|---|---|
| 1 | アプリケーション起動確認 | `docker compose up` でバックエンド・フロントエンド・DB・Nginx を起動し、`http://localhost/` へアクセス | トップページが表示され HTTP 200 を返す | FAIL | バックエンド起動失敗・フロントエンドビルドエラー (詳細は FAIL 詳細参照) |
| 2 | GitHub OAuth ログイン | ブラウザで `http://localhost/` を開き、「GitHub でログイン」ボタンをクリックして OAuth フローを完了する | ダッシュボードへリダイレクトされ、GitHub アバターとログイン名が表示される | FAIL | IT-1 (アプリ起動) が FAIL のため実施不可 |
| 3 | データ同期 | ログイン済み状態でダッシュボードの「同期」ボタンをクリックする | commit / PR / issue / review の件数がダッシュボードに反映され、最終同期日時が更新される | FAIL | IT-1 (アプリ起動) が FAIL のため実施不可 |
| 4 | ゴール新規作成 | `/goals` ページでタイトル・種別・目標数・期間を入力してフォームを送信する | ゴール一覧に新規ゴールが表示される | FAIL | IT-1 (アプリ起動) が FAIL のため実施不可 |
| 5 | ゴール進捗表示 | データ同期後にダッシュボードを表示する | 設定したゴールの currentCount がダッシュボードの進捗バーに反映される | FAIL | IT-1 (アプリ起動) が FAIL のため実施不可 |
| 6 | ゴール達成表示 | 目標件数に達した貢献データが存在する状態でダッシュボードを表示する | 該当ゴールに「達成済み」バッジが表示される | FAIL | IT-1 (アプリ起動) が FAIL のため実施不可 |
| 7 | Discord ダイジェスト送信 | `/settings` で Discord Webhook URL を設定し、「テスト送信」ボタンをクリックする | 指定チャンネルに貢献サマリーとゴール進捗が投稿される | FAIL | IT-1 (アプリ起動) が FAIL のため実施不可 |

## IT 結果サマリ

- 総項目数: 7
- PASS: 0
- FAIL: 7

## FAIL 詳細

### FAIL-A: バックエンド起動エラー (IT-1 原因)

**対象ファイル:** `backend/src/main/kotlin/com/wakaba/mapper/ContributionMapper.kt`

**再現手順:**
```
cd backend
DATABASE_URL=jdbc:postgresql://localhost:5432/wakaba DB_USERNAME=wakaba DB_PASSWORD=wakaba ./gradlew bootRun
```

**エラーメッセージ:**
```
java.lang.IllegalStateException: Type handler was null on parameter mapping for property 'userId'.
It was either not specified and/or could not be found for the javaType (java.util.UUID) : jdbcType (null) combination.
```

**原因:**
`ContributionMapper` と `GoalMapper` の MyBatis アノテーションクエリで `java.util.UUID` 型のパラメータ (`#{userId}`) を使用しているが、MyBatis 3.5.14 は `UUID` 型のデフォルト TypeHandler を持たないため、Spring 起動時に Bean 生成に失敗する。

**期待値と実際値の差異:**
- 期待: Spring Boot が正常起動し HTTP 8080 でリクエストを受付
- 実際: `ApplicationContext` 初期化に失敗し起動中断

**修正方針 (Implementer 向け):**
`AppBeans.kt` に `UUIDTypeHandler` を登録するか、`mybatis-config.xml` または `application.yml` の `mybatis.type-handlers-package` で UUID 用 TypeHandler を提供する。例:
```kotlin
// AppBeans.kt の mybatisConfigurationCustomizer に追加
config.typeHandlerRegistry.register(UUID::class.java, UUIDTypeHandler::class.java)
```
または `application.yml` に:
```yaml
mybatis:
  type-handlers-package: org.apache.ibatis.type
```

---

### FAIL-B: フロントエンドビルドエラー (IT-1 原因)

**対象ファイル:** `frontend/auth.ts`、`frontend/tsconfig.json`

**再現手順:**
```
cd frontend
npm install
npm run build
```

**エラーメッセージ:**
```
Module not found: Can't resolve '@/auth'
```

**発生箇所 (import している全ファイル):**
- `src/actions/goals.ts`
- `src/actions/sync.ts`
- `src/actions/config.ts`
- `src/app/page.tsx`
- `src/app/dashboard/page.tsx`
- `src/app/goals/page.tsx`
- `src/app/settings/page.tsx`
- `src/components/UserHeader.tsx`

**原因:**
`tsconfig.json` の `paths` 設定 (`"@/*": ["./src/*"]`) では `@/auth` が `src/auth.ts` に解決されるが、`auth.ts` はプロジェクトルート (`frontend/auth.ts`) に配置されているため解決できない。

**期待値と実際値の差異:**
- 期待: `npm run build` が成功しフロントエンドが起動
- 実際: webpack エラーでビルド中断

**修正方針 (Implementer 向け):**
`auth.ts` を `frontend/src/auth.ts` へ移動するか、または `tsconfig.json` の `paths` に `"@/auth": ["./auth.ts"]` を追加する。

## 総合判定

**FAIL**
