# CLAUDE.md — chat-app

Spring Boot チャットアプリ。Java 21 / Spring Boot 3.5.14 / PostgreSQL / MyBatis / Thymeleaf / Spring Security。

## セットアップ

```bash
# DB: PostgreSQL に chatapp データベースと keisuke ユーザーが必要
# application.properties の接続情報を確認すること

# ビルド・起動
./gradlew bootRun

# テスト実行（H2 インメモリ DB を使用）
./gradlew test
```

## アーキテクチャ

```
Controller → Form（バリデーション）→ Service → Repository（MyBatis）→ PostgreSQL
                                                       ↑
                                                    Entity
View: Thymeleaf テンプレート（templates/）
Security: Spring Security（SecurityConfig + CustomUserDetail）
```

## ディレクトリ構成

| パス            | 役割                                   |
| --------------- | -------------------------------------- |
| `controller/`   | HTTP リクエスト受付・画面遷移制御      |
| `entity/`       | DB テーブルに対応するデータ構造        |
| `form/`         | 画面入力の受け取り器 + バリデーション  |
| `repository/`   | MyBatis @Mapper — SQL を書く場所       |
| `service/`      | 複数レイヤーをまたぐビジネスロジック   |
| `custom_user/`  | Spring Security 用ユーザー情報ラッパー |
| `validation/`   | バリデーショングループ定義             |
| `templates/`    | Thymeleaf HTML テンプレート            |
| `db/migration/` | Flyway SQL マイグレーションファイル    |

## 重要なルール・注意点

### バリデーション

- `ValidationPriority1`（空白チェック）→ `ValidationPriority2`（形式チェック）の順序で実行
- `@Validated(ValidationOrder.class)` でグループ順序を指定する
- カスタムバリデーションは Form クラスのメソッドで実装（例: `validatePasswordConfirmation()`）

### MyBatis

- SQL はアノテーションで書く（XML なし）
- カラム名とフィールド名が異なる場合は `@Results/@Result` でマッピングする
- 関連オブジェクトの取得は `@One(select = "...")` で別クエリを呼ぶ
- `@Options(useGeneratedKeys = true, keyProperty = "id")` で自動採番 ID を取得

### Spring Security

- ログインは `userEmail` フィールドを使用（`usernameParameter("userEmail")` 設定済み）
- `@AuthenticationPrincipal CustomUserDetail currentUser` でログインユーザーを取得
- CSRF は無効化済み（`AbstractHttpConfigurer::disable`）

### Flyway マイグレーション

- `V{番号}__{説明}.sql` の命名規則
- 既存ファイルは変更しない — DB 変更は新しいバージョンのファイルを追加する
- 番号は連番で増やす

### 画像アップロード

- 保存先: `src/main/resources/static/uploads/`
- URL マッピング: `/uploads/**` → ファイルシステムのパス（WebConfig で設定）
- `ImageUrl` コンポーネントが `application.properties` から保存パスを読み込む

### Entity と Form の使い分け

- **Entity**: DB の形に合わせた構造（バリデーションなし）
- **Form**: 画面入力の形に合わせた構造（バリデーションあり）
- Controller で Form の値を Entity にコピーしてから Repository に渡す

## DB スキーマ概要

- `users`: id, username, user_email, password
- `rooms`: id, room_name
- `room_users`: id, user_id(FK), room_id(FK) — 多対多の中間テーブル
- `messages`: id, content, image, user_id(FK), room_id(FK), created_at

## テスト

- `test/factories/` にテストデータ生成クラス（JavaFaker 使用）
- `@ActiveProfiles("test")` で H2 DB 使用
- フォームの単体テストは `test/form/` に配置
- バリデーショングループを指定してテスト: `validator.validate(form, ValidationPriority1.class)`

## 詳細解説

`docs/project_explanation.md` に全ファイル・全処理フローの詳細解説あり。解説の追加・修正も全て反映。

`docs/how_to_build_from_scratch.md` に「白紙からアプリを作る思考プロセス・ファイル作成順序・なぜその順番か」の解説あり。
