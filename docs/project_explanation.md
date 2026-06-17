# chat-app プロジェクト完全解説

> 復習用メモ — 2026-05-28 作成

---

## 目次

1. [プロジェクト概要](#1-プロジェクト概要)
2. [技術スタック](#2-技術スタック)
3. [ディレクトリ・ファイル構成](#3-ディレクトリファイル構成)
4. [アーキテクチャ（設計思想）](#4-アーキテクチャ設計思想)
5. [データベース設計](#5-データベース設計)
6. [各レイヤーの役割と関係](#6-各レイヤーの役割と関係)
7. [処理フロー（機能別）](#7-処理フローの詳細)
8. [重要な設計パターン・仕組み](#8-重要な設計パターン仕組み)
9. [テスト設計](#9-テスト設計)
10. [全体の繋がり図](#10-全体の繋がり図)

---

## 1. プロジェクト概要

**chat-app** は Spring Boot で作られた Web チャットアプリ。

主な機能:
- ユーザー登録・ログイン・プロフィール編集
- チャットルームの作成（メンバー指定）
- チャットルーム内でのメッセージ投稿（テキスト + 画像）

---

## 2. 技術スタック

| 分類 | 技術 | 役割 |
|---|---|---|
| 言語 | Java 21 | — |
| フレームワーク | Spring Boot 3.5.14 | Web アプリ基盤 |
| テンプレートエンジン | Thymeleaf | HTML 生成（サーバーサイドレンダリング） |
| ORM | MyBatis | Java オブジェクト ↔ SQL マッピング |
| DB | PostgreSQL | 本番用データベース |
| DB マイグレーション | Flyway | スキーマのバージョン管理・自動適用 |
| セキュリティ | Spring Security | 認証・認可・パスワード暗号化 |
| コード補助 | Lombok | ボイラープレート（Getter/Setter等）自動生成 |
| ビルド | Gradle | 依存管理・ビルド自動化 |
| テスト DB | H2 | テスト時のインメモリ DB |
| テストデータ | JavaFaker | ランダムなダミーデータ生成 |

---

## 3. ディレクトリ・ファイル構成

```
chat-app/
├── build.gradle                        ← 依存ライブラリの設定ファイル
├── settings.gradle                     ← プロジェクト名の設定
└── src/
    ├── main/
    │   ├── java/in/tech_camp/chat_app/
    │   │   ├── ChatAppApplication.java         ← アプリの起動エントリポイント
    │   │   ├── ImageUrl.java                   ← 画像保存先パスを設定から読み込む
    │   │   ├── SecurityConfig.java             ← Spring Security の設定（アクセス制御・ログイン）
    │   │   ├── WebConfig.java                  ← 静的リソースのURL マッピング設定
    │   │   │
    │   │   ├── controller/                     ← HTTP リクエストの受付・レスポンス指示
    │   │   │   ├── UserController.java         ← 会員登録・ログイン・編集・ホーム
    │   │   │   ├── RoomController.java         ← チャットルーム作成
    │   │   │   └── MessageController.java      ← メッセージ一覧・投稿
    │   │   │
    │   │   ├── custom_user/                    ← Spring Security カスタム認証ユーザー
    │   │   │   └── CustomUserDetail.java       ← UserDetails 実装（認証情報ラッパー）
    │   │   │
    │   │   ├── entity/                         ← DB テーブルと1対1に対応するデータ構造
    │   │   │   ├── UserEntity.java             ← users テーブルに対応
    │   │   │   ├── RoomEntity.java             ← rooms テーブルに対応
    │   │   │   ├── RoomUserEntity.java         ← room_users テーブルに対応（中間テーブル）
    │   │   │   └── MessageEntity.java          ← messages テーブルに対応
    │   │   │
    │   │   ├── form/                           ← 画面からの入力データを受け取る器 + バリデーション
    │   │   │   ├── UserForm.java               ← 会員登録フォーム
    │   │   │   ├── UserEditForm.java           ← プロフィール編集フォーム
    │   │   │   ├── LoginForm.java              ← ログインフォーム
    │   │   │   ├── RoomForm.java               ← ルーム作成フォーム
    │   │   │   └── MessageForm.java            ← メッセージ投稿フォーム
    │   │   │
    │   │   ├── repository/                     ← DB との直接やり取り（SQL を書く場所）
    │   │   │   ├── UserRepository.java         ← users テーブルの CRUD
    │   │   │   ├── RoomRepository.java         ← rooms テーブルの CRUD
    │   │   │   ├── RoomUserRepository.java     ← room_users テーブルの CRUD
    │   │   │   └── MessageRepository.java      ← messages テーブルの CRUD
    │   │   │
    │   │   ├── service/                        ← ビジネスロジック（複数レイヤーをまたぐ処理）
    │   │   │   ├── UserService.java            ← パスワード暗号化 + ユーザー保存
    │   │   │   └── UserAuthenticationService.java ← Spring Security 用ユーザー検索
    │   │   │
    │   │   └── validation/                     ← バリデーションのグループ定義
    │   │       ├── ValidationOrder.java        ← P1 → P2 の実行順序を定義
    │   │       ├── ValidationPriority1.java    ← 空白チェック用グループ
    │   │       └── ValidationPriority2.java    ← フォーマットチェック用グループ
    │   │
    │   └── resources/
    │       ├── application.properties          ← DB接続・ファイルアップロード設定
    │       ├── db/migration/                   ← Flyway SQL マイグレーションファイル
    │       │   ├── V1__create_users_table.sql
    │       │   ├── V2__create_rooms_table.sql
    │       │   ├── V3__create_room_users_table.sql
    │       │   └── V4__create_messages_table.sql
    │       ├── static/
    │       │   ├── css/
    │       │   │   ├── style.css               ← 全体共通スタイル
    │       │   │   ├── user.css                ← ユーザーページ用スタイル
    │       │   │   └── room.css                ← ルーム作成ページ用スタイル
    │       │   └── uploads/                    ← アップロード画像の保存先
    │       └── templates/                      ← Thymeleaf テンプレート（画面HTML）
    │           ├── messages/
    │           │   ├── index.html              ← チャットページの外枠（レイアウト）
    │           │   ├── side_bar.html           ← サイドバー（フラグメント部品）
    │           │   └── main_chat.html          ← メインチャットエリア（フラグメント部品）
    │           ├── rooms/
    │           │   └── new.html                ← ルーム作成フォーム画面
    │           └── users/
    │               ├── signUp.html             ← 会員登録画面
    │               ├── login.html              ← ログイン画面
    │               └── edit.html               ← プロフィール編集画面
    │
    └── test/
        ├── java/in/tech_camp/chat_app/
        │   ├── factories/                      ← テスト用ダミーデータ生成クラス
        │   │   ├── UserFormFactory.java
        │   │   ├── UserEditFormFactory.java
        │   │   ├── RoomFormFactory.java
        │   │   └── MessageFormFactory.java
        │   └── form/                           ← フォームクラスの単体テスト
        │       ├── UserFormUnitTest.java
        │       ├── UserEditFormUnitTest.java
        │       ├── RoomFormUnitTest.java
        │       └── MessageFormUnitTest.java
        └── resources/
            └── application-test.properties    ← テスト用 H2 DB 設定
```

---

## 4. アーキテクチャ（設計思想）

### 4-1. MVC パターン

このアプリは **MVC（Model-View-Controller）パターン** + **Service レイヤー** で設計されている。

```
ブラウザ
  ↓ HTTP リクエスト
Controller（受付・振り分け）
  ↓ データ操作依頼
Service（ビジネスロジック）
  ↓ DB 操作依頼
Repository（SQL 実行）
  ↓ データ取得
Entity（データ構造）
  ↑ Model として View へ渡す
Thymeleaf（View = HTML 生成）
  ↓ HTTP レスポンス
ブラウザ
```

| レイヤー | クラス群 | 責任 |
|---|---|---|
| View | templates/*.html | 画面表示 |
| Controller | controller/*.java | URL ルーティング・画面遷移制御 |
| Form | form/*.java | 入力データの受け取り・バリデーション |
| Service | service/*.java | 複数処理をまたぐビジネスロジック |
| Repository | repository/*.java | SQL 実行・DB アクセス |
| Entity | entity/*.java | DB テーブルのデータ構造 |

### 4-2. 依存性注入（DI）

Spring Boot の DI コンテナが各クラスのインスタンスを自動管理する。

```java
// @AllArgsConstructor + final フィールドで自動的に DI される
@Controller
@AllArgsConstructor
public class UserController {
    private final UserRepository userRepository;  // Spring が自動で注入
    private final UserService userService;         // Spring が自動で注入
}
```

---

## 5. データベース設計

### ER 図（概念）

```
users ─────────────── room_users ─────────────── rooms
  id (PK)               id (PK)                    id (PK)
  username              user_id (FK → users.id)    room_name
  user_email            room_id (FK → rooms.id)
  password

messages
  id (PK)
  content
  image
  user_id (FK → users.id)
  room_id (FK → rooms.id)
  created_at
```

### 関係性

- **users ↔ rooms**: 多対多（1ユーザーは複数ルームに参加可、1ルームに複数ユーザー）
- **中間テーブル** `room_users`: 多対多を解消するための繋ぎテーブル
- **messages**: 1ユーザー・1ルームに紐づく（多対1の関係を2つ持つ）

### Flyway マイグレーション

`db/migration/` の SQL ファイルは `V{バージョン番号}__{説明}.sql` の命名規則。
アプリ起動時に Flyway が自動実行し、未適用のファイルだけをDBに反映する。
→ チームで DB スキーマの変更履歴を管理できる。

---

## 6. 各レイヤーの役割と関係

### 6-1. Entity（エンティティ）

DB のテーブル1行分を表す Java オブジェクト。`@Data`（Lombok）でゲッター/セッター自動生成。

```java
// RoomUserEntity がユーザーとルームを「繋ぐ」役割
@Data
public class RoomUserEntity {
    private long id;
    private UserEntity user;   // users テーブルの1行
    private RoomEntity room;   // rooms テーブルの1行
}
```

### 6-2. Form（フォーム）

**Entity と Form は別物** — 重要な設計判断。

| Entity | Form |
|---|---|
| DB の形に合わせた構造 | 画面（ユーザー入力）の形に合わせた構造 |
| バリデーションなし | バリデーションアノテーション付き |
| `@Data` | `@Data` |

**なぜ分けるか**: 画面の入力項目とDB の列が完全一致しないケースが多い（例: パスワード確認欄はDBに不要）。

```java
@Data
public class UserForm {
    @NotBlank(groups = ValidationPriority1.class)
    private String username;
    
    @NotBlank(groups = ValidationPriority1.class)
    @Email(groups = ValidationPriority2.class)
    private String userEmail;
    
    @NotBlank(groups = ValidationPriority1.class)
    @Length(min = 6, max = 128, groups = ValidationPriority2.class)
    private String password;
    
    private String passwordConfirmation;  // ← DB には不要な項目
}
```

### 6-3. Repository（リポジトリ）

MyBatis の `@Mapper` インターフェース。アノテーションで SQL を直書きする。

```java
@Mapper
public interface UserRepository {
    @Insert("INSERT INTO users (username, user_email, password) VALUES ...")
    @Options(useGeneratedKeys = true, keyProperty = "id")  // 自動採番 id を返す
    void insert(UserEntity user);
    
    @Select("SELECT * FROM users WHERE user_email = #{userEmail}")
    UserEntity findByEmail(String userEmail);
}
```

**リレーション取得（`@One`）**: MyBatis で1対1の関係を解決する仕組み。

```java
// room_users テーブルの room_id を使って rooms テーブルから RoomEntity を取得
@Results(value = {
    @Result(property = "room", column = "room_id",
            one = @One(select = "RoomRepository.findById"))  // 別クエリを呼ぶ
})
List<RoomUserEntity> findByUserId(Integer userId);
```

### 6-4. Service（サービス）

複数レイヤーをまたぐビジネスロジックを担う。Controller を薄く保つための分離。

```
UserService:
  createUserWithEncryptedPassword()
    → BCrypt でパスワードをハッシュ化
    → UserRepository.insert() でDB保存
    
UserAuthenticationService（implements UserDetailsService）:
  loadUserByUsername(email)
    → UserRepository.findByEmail() でDB検索
    → CustomUserDetail にラップして Spring Security に返す
```

### 6-5. Controller（コントローラー）

HTTP リクエストを受け取り、Model にデータを詰めて View を返す（または リダイレクト）。

```java
@GetMapping("/")
public String index(@AuthenticationPrincipal CustomUserDetail currentUser, Model model) {
    // 1. 現在ログイン中のユーザー情報を取得
    UserEntity user = userRepository.findById(currentUser.getId());
    // 2. そのユーザーが参加しているルーム一覧を取得
    List<RoomUserEntity> roomUserEntities = roomUserRepository.findByUserId(currentUser.getId());
    List<RoomEntity> roomList = roomUserEntities.stream()
        .map(RoomUserEntity::getRoom)
        .collect(Collectors.toList());
    // 3. Thymeleaf テンプレートに渡すデータを Model に詰める
    model.addAttribute("user", user);
    model.addAttribute("rooms", roomList);
    // 4. templates/messages/index.html を返す
    return "messages/index";
}
```

### 6-6. View（Thymeleaf テンプレート）

`th:` 属性で Java のデータを HTML に埋め込む。

```html
<!-- th:each でリスト繰り返し -->
<div th:each="room : ${rooms}" class="room">
    <a th:href="@{/rooms/{roomId}/messages(roomId=${room.id})}"
       th:text="${room.name}"></a>
</div>
```

**フラグメント機能**: 部品化した HTML を別ファイルに分けて再利用できる。

```html
<!-- side_bar.html: フラグメントの定義 -->
<div th:fragment="side_bar"> ... </div>

<!-- index.html: フラグメントの挿入 -->
<div th:insert="~{messages/side_bar :: side_bar}"></div>
```

---

## 7. 処理フローの詳細

### 7-1. 会員登録フロー

```
① GET /users/signUp
   → UserController.showSignUp()
   → model に空の UserForm を詰める
   → users/signUp.html を表示

② フォーム入力 → POST /user
   → UserController.createUser()
   
   バリデーションチェック（ValidationOrder の順序で実行）:
   ┌─ Priority1: @NotBlank（空白チェック）
   └─ Priority2: @Email, @Length（形式チェック）※P1 が通った場合のみ実行
   
   カスタムチェック:
   ├─ userForm.validatePasswordConfirmation() → パスワード確認一致チェック
   └─ userRepository.existsByEmail() → メール重複チェック
   
   エラーあり → users/signUp.html に戻る（エラーメッセージ表示）
   エラーなし:
   → UserEntity に Form の値をコピー
   → UserService.createUserWithEncryptedPassword()
       └─ BCrypt でパスワードをハッシュ化
       └─ UserRepository.insert() で DB 保存
   → redirect:/ （ホームへリダイレクト）
```

### 7-2. ログインフロー

```
① GET /users/login
   → UserController.showLogin()
   → users/login.html を表示

② フォーム入力 → POST /login
   （Spring Security が自動処理）
   
   → UserAuthenticationService.loadUserByUsername(email)
       └─ UserRepository.findByEmail() で DB 検索
       └─ CustomUserDetail にラップして返す
   
   → Spring Security が BCrypt パスワード照合を自動実行
   
   成功 → redirect:/ （defaultSuccessUrl）
   失敗 → redirect:/login?error
          → UserController.login() でエラーメッセージを Model に詰める
          → users/login.html に戻る
```

### 7-3. チャットルーム作成フロー

```
① GET /rooms/new
   → RoomController.showRoomNew()
   → 自分以外のユーザー一覧を取得（UserRepository.findAllExcept()）
   → rooms/new.html を表示（メンバー選択ドロップダウン）

② フォーム入力（ルーム名 + メンバー選択）→ POST /rooms
   → RoomController.createRoom()
   
   → RoomEntity を作成・DB に保存（RoomRepository.insert()）
   → 選択されたメンバーID ＋ 自分の ID でループ:
       └─ UserEntity を取得
       └─ RoomUserEntity（ルーム-ユーザー関連）を作成・DB に保存
   
   → redirect:/ （ホームへ）
```

**ポイント**: `rooms/new.html` では `th:value="${#authentication?.principal.id}"` で
ログインユーザー自身の ID を隠しフィールドに含め、自動的に自分もメンバーに追加する。

### 7-4. メッセージ表示フロー

```
① GET /rooms/{roomId}/messages
   → MessageController.showMessages()
   
   ① 現在のユーザー情報取得
   ② 自分が参加しているルーム一覧取得（サイドバー用）
   ③ 指定された roomId のルーム情報取得
   ④ そのルームのメッセージ一覧取得（投稿ユーザー情報込み）
   
   → messages/index.html を表示
       ├─ side_bar フラグメント: ルーム一覧
       └─ main_chat フラグメント（room != null の場合）: メッセージ一覧 + 投稿フォーム
```

### 7-5. メッセージ投稿フロー

```
② フォーム入力 → POST /rooms/{roomId}/messages
   → MessageController.saveMessage()
   
   カスタムバリデーション:
   └─ messageForm.validateMessage() → content か image どちらかが必須
   
   → MessageEntity にコンテンツ・ユーザー・ルームをセット
   → MessageRepository.insert() で DB 保存
   → redirect:/rooms/{roomId}/messages
   
   ※ 注意: 画像アップロード処理は現在コメントアウト中（未実装）
```

### 7-6. ユーザー編集フロー

```
① GET /users/{userId}/edit
   → UserController.showEdit()
   → DB からユーザー情報取得 → UserEditForm に詰める
   → users/edit.html 表示

② 編集 → POST /users/{userId}
   → UserController.updateUser()
   
   → メール重複チェック（自分以外が同じメールを使っていないか）
   → バリデーション
   → エラーあり → edit.html に戻る
   → 問題なし → UserRepository.update() で DB 更新
   → redirect:/
```

---

## 8. 重要な設計パターン・仕組み

### 8-1. Spring Security の認証フロー

```
CustomUserDetail（UserDetails 実装）
  └─ UserEntity をラップする
  └─ getUsername() → userEmail を返す（メールでログインするため）
  └─ getPassword() → 暗号化済みパスワードを返す

UserAuthenticationService（UserDetailsService 実装）
  └─ loadUserByUsername(email) → DB からユーザー検索 → CustomUserDetail を返す

SecurityConfig
  └─ .usernameParameter("userEmail") → フォームのどのフィールドをメールとして使うか
  └─ BCryptPasswordEncoder を Bean 登録
  └─ アクセス制御ルール定義
```

`@AuthenticationPrincipal CustomUserDetail currentUser` で、
Controller のメソッドの引数に直接ログインユーザーを受け取れる。

### 8-2. バリデーショングループ（優先順位付きバリデーション）

なぜグループ分けするか？ → **空白の状態でフォーマットチェックをすると、エラーメッセージが2重に出てしまう**問題を防ぐ。

```
ValidationOrder = @GroupSequence({Priority1, Priority2})

Priority1: @NotBlank → 「空白ではいけない」チェック
Priority2: @Email, @Length → 「形式が正しい」チェック

実行順: P1 が全部通ってから P2 が実行される
→ 「空白」エラーが出たら「形式」チェックはスキップ
```

### 8-3. MyBatis のネストされたオブジェクト取得

`@One` を使って関連テーブルのデータを1つのオブジェクトに自動マッピングする。

```java
// room_users テーブルから取得しつつ、room_id を使って rooms テーブルも JOIN 的に取得
@Select("SELECT * FROM room_users WHERE user_id = #{userId}")
@Results(value = {
    @Result(property = "room", column = "room_id",
            one = @One(select = "RoomRepository.findById"))
    // → 取得した room_id で RoomRepository.findById を呼び出し、
    //   結果を RoomUserEntity.room フィールドに詰める
})
List<RoomUserEntity> findByUserId(Integer userId);
```

### 8-4. Flyway によるスキーマ管理

```
V1__create_users_table.sql   ← 最初に実行
V2__create_rooms_table.sql   ← 次に実行
V3__create_room_users_table.sql
V4__create_messages_table.sql
```

- バージョン番号の順に実行される
- 一度実行されたファイルは再実行しない（チェックサムで管理）
- DB の変更時は新しい番号のファイルを追加する（既存ファイルを変更しない）

### 8-5. Thymeleaf フラグメント（画面部品の再利用）

```
index.html（外枠）
  ├─ th:insert → side_bar.html の side_bar フラグメントを挿入
  └─ th:insert → main_chat.html の main_chat フラグメントを挿入（room が null でなければ）
```

`th:if="${room != null}"` により、ルームが選択されていないトップページ（`/`）では
メインチャットエリアが表示されない。

### 8-6. 画像アップロード（WebConfig + ImageUrl）

```
ImageUrl.java
  └─ application.properties の image.url を読み込む
  └─ 保存先: src/main/resources/static/uploads/

WebConfig.java
  └─ /uploads/** → file:src/main/resources/static/uploads/ にマッピング
  └─ ブラウザから /uploads/ファイル名 でアクセス可能にする
```

---

## 9. テスト設計

### 9-1. テストの構成

```
単体テスト（Form バリデーション）
  ├─ UserFormUnitTest
  ├─ UserEditFormUnitTest
  ├─ RoomFormUnitTest
  └─ MessageFormUnitTest
```

### 9-2. Factory パターン（テストデータ生成）

JavaFaker を使ってランダムなダミーデータを生成する。

```java
// UserFormFactory.java
public static UserForm createUser() {
    UserForm userForm = new UserForm();
    userForm.setUsername(faker.name().username());       // ランダムなユーザー名
    userForm.setUserEmail(faker.internet().emailAddress()); // ランダムなメール
    userForm.setPassword(faker.internet().password(6, 12)); // 6〜12文字のパスワード
    userForm.setPasswordConfirmation(userForm.getPassword());
    return userForm;
}
```

**Factory パターンのメリット**: 各テストで `createUser()` を呼ぶだけで「正常データ」が得られる。
あとはテストしたい項目だけを崩せばよい（`userForm.setUsername("")`）。

### 9-3. @Nested でテストをグループ化

```java
@Nested
class ユーザーを作成できる場合 {
    @Test
    public void nameとemailとpasswordが存在すれば登録できる() { ... }
}

@Nested
class ユーザーを作成できない場合 {
    @Test
    public void nameが空では登録できない() { ... }
    @Test
    public void emailが空では登録できない() { ... }
    // ...
}
```

### 9-4. テスト環境の設定分離

`application-test.properties` でテスト時は H2（インメモリDB）を使用する。
`@ActiveProfiles("test")` でテストクラスにこの設定を適用。

---

## 10. 全体の繋がり図

```
[ブラウザ]
    │
    │ HTTP リクエスト
    ↓
[Spring Security]
    │ 認証チェック（SecurityConfig のルールに従う）
    │ ログイン処理 → UserAuthenticationService → UserRepository
    ↓
[Controller]
    UserController      RoomController      MessageController
    ├── / (GET)          ├── /rooms/new      ├── /rooms/{id}/messages (GET)
    ├── /users/signUp    └── /rooms (POST)   └── /rooms/{id}/messages (POST)
    ├── /user (POST)
    ├── /users/login
    └── /users/{id}/edit
    │
    │ Form でバリデーション
    │ (ValidationOrder → Priority1 → Priority2)
    ↓
[Service]
    UserService                  UserAuthenticationService
    └── createUserWithEncrypted  └── loadUserByUsername
         └── PasswordEncoder
    │
    ↓
[Repository]（MyBatis @Mapper）
    UserRepository   RoomRepository   RoomUserRepository   MessageRepository
    │
    │ SQL 実行
    ↓
[PostgreSQL]
    users    rooms    room_users    messages
    │
    │ Flyway でスキーマ管理
    ↓
[V1〜V4 SQL ファイル]

[View]（Thymeleaf）
    templates/users/      templates/rooms/     templates/messages/
    signUp.html           new.html             index.html
    login.html                                 ├─ side_bar.html（フラグメント）
    edit.html                                  └─ main_chat.html（フラグメント）
```

---

## まとめ：このアプリから学べる重要概念

| 概念 | どこで使われているか |
|---|---|
| MVC パターン | アーキテクチャ全体 |
| DI（依存性注入） | `@AllArgsConstructor` + `final` フィールド |
| Spring Security | `SecurityConfig`, `CustomUserDetail`, `UserAuthenticationService` |
| MyBatis アノテーション | `repository/*.java` |
| Flyway マイグレーション | `db/migration/V*.sql` |
| Lombok | 全 Entity, Form, Controller に `@Data`, `@AllArgsConstructor` |
| バリデーショングループ | `validation/`, `form/*.java` |
| Thymeleaf フラグメント | `messages/index.html` と `side_bar.html`, `main_chat.html` |
| Factory パターン | `test/factories/*.java` |
| 多対多リレーション | `room_users` 中間テーブル、`RoomUserEntity` |
