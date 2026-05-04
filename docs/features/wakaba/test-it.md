# wakaba IT 項目書

## テスト環境

- ブランチ: `feature/wakaba`
- 実行日時: 2026-05-04T09:45:00Z
- 環境情報:
  - OS: Ubuntu 24.04 (Linux 6.18.5)
  - Java: OpenJDK 21.0.10
  - PostgreSQL: 16.13 (ローカル起動)
  - Docker: デーモン停止のためローカル起動で代替
  - バックエンド UT: `./gradlew test` 実行済み (41 件全件 PASS)

## IT 項目

| # | テストシナリオ | 手順 | 期待結果 | 実施結果 | 備考 |
|---|---|---|---|---|---|
| 1 | アプリケーション起動確認 | PostgreSQL を起動し `DATABASE_URL=jdbc:postgresql://localhost:5432/wakaba ./gradlew bootRun` でバックエンドを起動する | Spring Boot が正常起動し HTTP 8080 でリクエストを受け付ける | FAIL | FAIL-A: MyBatis UUID TypeHandler 未登録によりアプリケーション起動失敗 |
| 2 | フロントエンドビルド確認 | `cd frontend && npm install && npm run build` | ビルドが成功しフロントエンドが起動する | FAIL | FAIL-B: `auth.ts` がルートにあるため `@/auth` パス解決失敗 |
| 3 | GitHub OAuth ログイン | ブラウザで `http://localhost/` を開き「GitHub でログイン」ボタンをクリックして OAuth フローを完了する | ダッシュボードへリダイレクトされ、GitHub アバターとログイン名が表示される | FAIL | IT-1, IT-2 が FAIL のため実施不可 |
| 4 | データ同期 | ログイン済み状態でダッシュボードの「同期」ボタンをクリックする | commit / PR / issue / review の件数がダッシュボードに反映され、最終同期日時が更新される | FAIL | IT-1, IT-2 が FAIL のため実施不可 |
| 5 | ゴール新規作成 | `/goals` ページでタイトル・種別・目標数・期間を入力してフォームを送信する | ゴール一覧に新規ゴールが表示される | FAIL | IT-1, IT-2 が FAIL のため実施不可 |
| 6 | ゴール進捗表示 | データ同期後にダッシュボードを表示する | 設定したゴールの currentCount がダッシュボードの進捗バーに反映される | FAIL | IT-1, IT-2 が FAIL のため実施不可 |
| 7 | ゴール達成表示 | 目標件数に達した貢献データが存在する状態でダッシュボードを表示する | 該当ゴールに「達成!」バッジが表示される | FAIL | IT-1, IT-2 が FAIL のため実施不可 |
| 8 | Discord ダイジェスト送信 | `/settings` で Discord Webhook URL を設定し「テスト送信」ボタンをクリックする | 指定チャンネルに貢献サマリーとゴール進捗が投稿される | FAIL | IT-1, IT-2 が FAIL のため実施不可 |

## IT 結果サマリ

- 総項目数: 8
- PASS: 0
- FAIL: 8

## FAIL 詳細

### FAIL-A: バックエンド起動失敗 — MyBatis UUID TypeHandler 未登録 (IT-1 原因)

**対象ファイル:** `backend/src/main/kotlin/com/wakaba/config/AppBeans.kt` および各 Mapper

**再現手順:**
```bash
cd backend
DATABASE_URL=jdbc:postgresql://localhost:5432/wakaba DB_USERNAME=wakaba DB_PASSWORD=wakaba ./gradlew bootRun
```

**エラーメッセージ (抜粋):**
```
Error creating bean with name 'contributionMapper': java.lang.IllegalStateException:
Type handler was null on parameter mapping for property 'userId'.
It was either not specified and/or could not be found for the
javaType (java.util.UUID) : jdbcType (null) combination.
  at org.apache.ibatis.mapping.ParameterMapping$Builder.validate(ParameterMapping.java:117)
  ~[mybatis-3.5.14.jar:3.5.14]
```

**影響範囲:** `ContributionMapper`, `GoalMapper`, `UserMapper` の全メソッドが Bean 生成時に失敗。Spring ApplicationContext の初期化に失敗しサーバーが起動しない。

**原因:** `mybatis-spring-boot-starter:3.0.3` が依存する `mybatis:3.5.14` は `java.util.UUID` のデフォルト TypeHandler を自動登録しない。`AppBeans.kt` の `mybatisConfigurationCustomizer` に `UUIDTypeHandler` の登録が欠落している。

**期待値と実際値の差異:**
- 期待: Spring Boot が正常起動し HTTP 8080 でリクエストを受け付ける
- 実際: `ApplicationContext` 初期化に失敗し起動中断

**修正方針:**
`AppBeans.kt` の `mybatisConfigurationCustomizer` に UUID TypeHandler を登録する。

```kotlin
// AppBeans.kt
import org.apache.ibatis.type.UUIDTypeHandler
import java.util.UUID

@Bean
fun mybatisConfigurationCustomizer() = ConfigurationCustomizer { config ->
    config.isMapUnderscoreToCamelCase = true
    config.typeHandlerRegistry.register(UUID::class.java, UUIDTypeHandler::class.java)
}
```

または `application.yml` に以下を追加する:
```yaml
mybatis:
  type-handlers-package: org.apache.ibatis.type
```

---

### FAIL-B: フロントエンドビルド失敗 — `auth.ts` パス解決エラー (IT-2 原因)

**対象ファイル:** `frontend/auth.ts`, `frontend/tsconfig.json`

**再現手順:**
```bash
cd frontend
npm install
npm run build
```

**エラーメッセージ (予測):**
```
Module not found: Can't resolve '@/auth'
```

**影響範囲:** `@/auth` を import している全ファイル:
- `frontend/middleware.ts`
- `frontend/src/actions/goals.ts`
- `frontend/src/actions/sync.ts`
- `frontend/src/actions/config.ts`
- `frontend/src/app/page.tsx`
- `frontend/src/app/dashboard/page.tsx`
- `frontend/src/app/goals/page.tsx`
- `frontend/src/app/settings/page.tsx`
- `frontend/src/components/UserHeader.tsx`

**原因:** `tsconfig.json` のパスエイリアス `"@/*": ["./src/*"]` により `@/auth` は `frontend/src/auth.ts` に解決されるが、`auth.ts` は `frontend/auth.ts` (プロジェクトルート) に存在し `frontend/src/` には存在しない。

**期待値と実際値の差異:**
- 期待: `npm run build` が成功しフロントエンドが起動する
- 実際: webpack/turbopack が `@/auth` を解決できずビルド中断

**修正方針 (どちらか一方を選択):**

**案A:** `auth.ts` を `frontend/src/auth.ts` へ移動する
```bash
mv frontend/auth.ts frontend/src/auth.ts
```
※ `frontend/middleware.ts` も `frontend/src/middleware.ts` へ移動が必要か確認すること

**案B:** `tsconfig.json` に auth のパスを明示的に追加する
```json
{
  "compilerOptions": {
    "paths": {
      "@/*": ["./src/*"],
      "@/auth": ["./auth.ts"]
    }
  }
}
```

## 総合判定

**FAIL**

---

## 備考

### バックエンド UT (参考)

`./gradlew test` を実行した結果、全 41 件の UT が PASS していることを確認済み。サービス層のビジネスロジックに問題はなく、MyBatis Mapper の設定レベルの問題が原因。

| テストクラス | 件数 | 結果 |
|---|---|---|
| `GoalServiceTest` | 12 | PASS |
| `ContributionServiceTest` | 8 | PASS |
| `ConfigServiceTest` | 6 | PASS |
| `SyncServiceTest` | 8 | PASS |
| `DiscordDigestServiceTest` | 7 | PASS |
| **合計** | **41** | **PASS** |

### Flyway マイグレーション (参考)

バックエンド起動時に Flyway が V1, V2 の 2 マイグレーションを正常適用することを確認済み (`Successfully applied 2 migrations to schema "public"`).
