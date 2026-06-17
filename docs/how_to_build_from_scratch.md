# Spring Boot アプリを白紙から作る思考プロセス

このドキュメントは「chat-app を最初から自分で作るとしたらどう考え、どの順番でファイルを作るか」をまとめた復習用ガイド。
コードを読んで理解するだけでなく、「なぜその順番か」という設計の論理を身につけることが目的。

---

## 大原則：「データから画面に向かって作る」

Spring Boot アプリを作るときの根本的な考え方は **下から上へ** 。

```
画面（View） ← 何を表示するかは、データがないと決まらない
  ↑
Controller  ← どこへ遷移するかは、Serviceがないと決まらない
  ↑
Service     ← ビジネスロジックは、Repositoryがないと書けない
  ↑
Repository  ← SQLは、Entityの形がないと書けない
  ↑
Entity      ← DBの構造を写したもの
  ↑
DB設計      ← まず「何のデータを持つか」を決める ← ここから始める
```

上位レイヤーは必ず下位レイヤーに **依存** している。
だから「上から作ろうとすると必ず詰まる」。

---

## STEP 1: 機能リストとデータ設計（コードを書く前に考えること）

### まず「何ができるアプリか」を箇条書きにする

このアプリの場合：
- ユーザーが新規登録・ログイン・プロフィール編集できる
- チャットルームを作れる（複数ユーザーを招待できる）
- ルーム内でメッセージ（テキスト or 画像）を送受信できる

### 機能リストからDBのテーブルを逆算する

機能を実現するために「どんなデータを保存する必要があるか」を考える。

| 機能 | 必要なデータ |
|---|---|
| ユーザー登録・ログイン | ユーザー情報（名前・メール・パスワード） |
| チャットルーム | ルーム情報（ルーム名） |
| ルームへの招待 | 「誰がどのルームに属しているか」の対応表 |
| メッセージ送信 | メッセージ（本文・画像・誰が・どのルームに・いつ） |

→ 必要なテーブルが見えてきた：`users`, `rooms`, `room_users`, `messages`

### テーブル間の関係（リレーション）を整理する

- User と Room は **多対多**（1人が複数ルームに入れる、1ルームに複数人）
  → 多対多は **中間テーブル** で表現する → `room_users`テーブル
- Message は User と Room に **多対1**（1メッセージは1人・1ルームに紐づく）
  → `messages`テーブルに `user_id`, `room_id` の外部キーを持たせる

```
users ──< room_users >── rooms
users ──< messages  >── rooms
```

---

## STEP 2: プロジェクト生成と設定ファイル

### 2-1. Spring Initializr でプロジェクト生成

`https://start.spring.io` で以下を選択してダウンロード：
- Spring Web（Controllerを作るために必要）
- Thymeleaf（HTML テンプレートエンジン）
- Spring Security（ログイン認証）
- MyBatis Framework（DB操作）
- Flyway Migration（DBスキーマ管理）
- PostgreSQL Driver（DB接続）
- Lombok（@Data等でコードを省略）
- Validation（バリデーション）

### 2-2. `application.properties` に DB 接続情報を書く

```properties
# DB接続
spring.datasource.url=jdbc:postgresql://localhost:5432/chatapp
spring.datasource.username=keisuke
spring.datasource.password=wafuka0806

# Flyway設定（初回マイグレーション時に必要）
spring.flyway.baselineOnMigrate=true

# ファイルアップロードサイズ上限
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# 画像保存先のパス（@Valueで読み込む用）
image.url=src/main/resources/static/uploads
```

**なぜここで書くか：** 後で Repository や設定クラスが DB に接続しようとするので、接続情報が先に必要。

---

## STEP 3: DB マイグレーション（Flyway SQL）

`src/main/resources/db/migration/` 以下に **V1, V2, V3...** の順に作る。

### 命名規則
`V{番号}__{説明}.sql`（番号の後はアンダースコア2つ）

### なぜ Flyway を使うか

Flyway はアプリ起動時に「まだ実行されていないSQLファイル」を自動で実行する仕組み。
これにより「どのSQL が適用済みか」を自動で管理でき、チームで開発するときも DB の状態を統一できる。

### 作成順序とその理由

外部キーは「参照先のテーブルが先に存在する」必要がある。

```
V1__create_users_table.sql    ← 最初（他から参照される）
V2__create_rooms_table.sql    ← 次（room_usersから参照される）
V3__create_room_users_table.sql ← users と rooms が揃ってから
V4__create_messages_table.sql ← users と rooms が揃ってから
```

### V1 の中身の考え方

```sql
CREATE TABLE IF NOT EXISTS users (
    id SERIAL NOT NULL,          -- 自動採番の主キー（SERIAL = auto increment）
    username VARCHAR(255) NOT NULL UNIQUE,
    user_email VARCHAR(255) NOT NULL UNIQUE,  -- ログインIDになるのでUNIQUE
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);
```

ポイント：
- `SERIAL` = 自動で1,2,3...と番号を振る（MyBatisの`@Options(useGeneratedKeys=true)`と対応）
- `UNIQUE` = 重複禁止（メールアドレスは1人1つ）
- `VARCHAR(255)` = 最大255文字の文字列

### V4（外部キーあり）の考え方

```sql
CREATE TABLE IF NOT EXISTS messages (
    id SERIAL NOT NULL,
    content VARCHAR(512),        -- NULLを許可（画像だけのメッセージがあるから）
    user_id INTEGER NOT NULL,    -- どのユーザーのメッセージか
    room_id INTEGER NOT NULL,    -- どのルームのメッセージか
    image VARCHAR(512),          -- NULLを許可（テキストだけのメッセージがあるから）
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 投稿日時（自動セット）
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    PRIMARY KEY (id)
);
```

`ON DELETE CASCADE` = 参照先（ユーザー・ルーム）が削除されたら、メッセージも一緒に削除する。

---

## STEP 4: Entity クラス（DBテーブルの鏡写し）

**場所：** `entity/`パッケージ

DB の各テーブルに対応するクラスを作る。「DB のカラム = Javaのフィールド」という対応。

### Entity の作り方の考え方

```java
@Data              // Lombokが getter/setter/toString を自動生成
public class UserEntity {
    private Integer id;
    private String username;
    private String userEmail;   // カラム名は user_email だがJavaではキャメルケース
    private String password;
    
    // ★関連するEntityも持たせる（MyBatisの@Oneで後から埋める）
    private List<RoomUserEntity> roomUsers;
    private List<MessageEntity> messages;
}
```

### カラム名とフィールド名の対応

| DB カラム名 | Java フィールド名 |
|---|---|
| `user_email` | `userEmail` |
| `room_name` | `name`（このアプリでは`room_name`→`name`） |
| `created_at` | `createdAt` |

名前が異なる場合、Repository の `@Results/@Result` で明示的にマッピングが必要。

### 多対多の中間テーブル（RoomUserEntity）

```java
@Data
public class RoomUserEntity {
    private long id;
    private UserEntity user;   // user_id ではなく UserEntity オブジェクトを持つ
    private RoomEntity room;   // room_id ではなく RoomEntity オブジェクトを持つ
}
```

DB には `user_id`（数値）しか入っていないが、Javaのオブジェクトとしては `UserEntity` を持たせる。
この「ID→オブジェクト」への変換は Repository の `@One` アノテーションが担当する。

---

## STEP 5: Repository（DB とのやり取り）

**場所：** `repository/`パッケージ

`@Mapper` インターフェースを作り、メソッドに SQL を書く。
**クラスではなくインターフェース** であることが重要（実装はMyBatisが自動生成する）。

### 基本パターン

```java
@Mapper
public interface UserRepository {
    
    // INSERT: @Optionsで自動採番したIDをEntityに書き戻す
    @Insert("INSERT INTO users (username, user_email, password) VALUES (#{username}, #{userEmail}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UserEntity user);
    
    // SELECT（1件）: #{変数名}でメソッド引数を参照
    @Select("SELECT * FROM users WHERE id = #{id}")
    UserEntity findById(Integer id);
    
    // UPDATE
    @Update("UPDATE users SET username = #{username} WHERE id = #{id}")
    void update(UserEntity user);
}
```

### カラム名とフィールド名が異なる場合の @Results

```java
@Select("SELECT * FROM rooms WHERE id = #{id}")
@Results(value = {
    @Result(property = "name", column = "room_name")  // DBのroom_name → Javaのname
})
RoomEntity findById(Integer id);
```

### 関連オブジェクトを別クエリで取得する @One

```java
@Select("SELECT * FROM messages WHERE room_id = #{roomId}")
@Results(value = {
    @Result(property = "createdAt", column = "created_at"),
    @Result(
        property = "user",           // MessageEntityの userフィールドに
        column = "user_id",          // messages.user_id の値を使って
        one = @One(select = "in.tech_camp.chat_app.repository.UserRepository.findById")
        // ↑ このメソッドを呼んで取得したUserEntityをセットする
    )
})
List<MessageEntity> findByRoomId(Integer roomId);
```

`@One` は「1対1の関連をサブクエリで取得する」仕組み。
`user_id`（数値）を渡して `findById` を呼び、返ってきた `UserEntity` を `user` フィールドに入れてくれる。

---

## STEP 6: Spring Security の設定

**場所：** ルートパッケージ（`SecurityConfig.java`、`custom_user/CustomUserDetail.java`、`service/UserAuthenticationService.java`）

ログイン認証は Spring Security が担当するが、「どのユーザー情報を使って認証するか」は自分で教える必要がある。

### 3つのクラスの役割

| クラス | 役割 |
|---|---|
| `SecurityConfig` | 「どのURLを誰がアクセスできるか」「ログイン設定」を定義 |
| `CustomUserDetail` | Spring Securityが扱うユーザー情報のラッパー |
| `UserAuthenticationService` | メールアドレスからユーザーを検索する処理 |

### なぜ CustomUserDetail が必要か

Spring Security は `UserDetails` インターフェースのオブジェクトとしてユーザーを扱う。
自分の `UserEntity` はこのインターフェースを実装していないので、「`UserEntity` を包んで `UserDetails` に見せる」ラッパークラスが必要。

```java
public class CustomUserDetail implements UserDetails {
    private final UserEntity user;  // 本物のEntityを中に持つ
    
    @Override
    public String getUsername() {
        return user.getUserEmail();  // ★ログインIDにemailを使うのでここを変更
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    // 独自メソッド：ControllerでIDや名前が取り出せるようにしておく
    public Integer getId() { return user.getId(); }
}
```

### UserAuthenticationService の役割

```java
@Service
public class UserAuthenticationService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String email) {
        // ログイン時、Spring SecurityがこのメソッドにメールをくれるD
        // → DBからユーザーを探して CustomUserDetail を返す
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) throw new UsernameNotFoundException("Not found");
        return new CustomUserDetail(user);
    }
}
```

### SecurityConfig の設定ポイント

```java
http
  // ①どのURLを誰がアクセスできるか
  .authorizeHttpRequests(auth -> auth
    .requestMatchers("/users/signUp", "/users/login").permitAll()  // ログインなしOK
    .requestMatchers(HttpMethod.POST, "/user").permitAll()          // 新規登録POST もOK
    .anyRequest().authenticated()                                    // それ以外はログイン必須
  )
  // ②ログイン設定
  .formLogin(login -> login
    .loginPage("/users/login")           // ログイン画面のURL
    .loginProcessingUrl("/login")        // フォームのPOST先
    .defaultSuccessUrl("/", true)        // 成功後のリダイレクト先
    .usernameParameter("userEmail")      // ★フォームのname属性（デフォルトは"username"）
  )
  // ③ログアウト設定
  .logout(logout -> logout
    .logoutUrl("/logout")
    .logoutSuccessUrl("/users/login")
  );
```

---

## STEP 7: バリデーション（入力チェック）の設計

**場所：** `validation/`パッケージ

### なぜバリデーショングループが必要か

「空欄チェック」→「形式チェック」の **順番** で実行したいから。

例：メールアドレスが空欄のとき
- `@NotBlank` だけがエラーを出すべき
- `@Email`（形式チェック）も同時にエラーを出してほしくない

### グループの作り方

```java
// グループを表すだけの空のインターフェース
public interface ValidationPriority1 {}  // 第1優先（空白チェック）
public interface ValidationPriority2 {}  // 第2優先（形式チェック）

// 実行順序を定義
@GroupSequence({ValidationPriority1.class, ValidationPriority2.class})
public interface ValidationOrder {}
```

### Form クラスへの適用

```java
@Data
public class UserForm {
    @NotBlank(groups = ValidationPriority1.class)      // 第1優先
    @Email(groups = ValidationPriority2.class)          // 第2優先（空欄でなければ実行）
    private String userEmail;
}
```

---

## STEP 8: Form クラス（画面入力の受け取り）

**場所：** `form/`パッケージ

Entity は「DBの形」、Form は「画面の形」。別々に作る理由：

| Entity | Form |
|---|---|
| DB のカラムと対応 | HTML フォームの項目と対応 |
| バリデーションなし | バリデーションあり |
| 他から参照されて使いまわされる | 特定の画面専用 |

### Form にしか入らない項目の例

- `passwordConfirmation`（パスワード確認欄）→ DB に保存する必要がない
- `memberIds`（ルーム作成時に選ぶユーザーIDリスト）→ room_users テーブルへの変換が必要

### カスタムバリデーションはメソッドで実装

```java
public class UserForm {
    private String password;
    private String passwordConfirmation;
    
    // @アノテーションでは「2つのフィールドの比較」ができないので、メソッドで実装
    public void validatePasswordConfirmation(BindingResult result) {
        if (!password.equals(passwordConfirmation)) {
            result.rejectValue("passwordConfirmation", "error", "パスワードが一致しません");
        }
    }
}
```

---

## STEP 9: Service（ビジネスロジック）

**場所：** `service/`パッケージ

「複数のレイヤーをまたぐ処理」や「Controllerに書くには複雑すぎる処理」を切り出す場所。

### なぜ Service が必要か

パスワードの暗号化を例に取ると：
- Controller でやる？ → Controller は HTTP リクエストの受け取りに集中すべき
- Repository でやる？ → Repository は SQL の実行に集中すべき

→ 「Entity を受け取り、パスワードを暗号化してから Repository に渡す」という **複数レイヤーにまたがる処理** は Service の仕事。

```java
@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // SecurityConfigで@Beanとして登録済み
    
    public void createUserWithEncryptedPassword(UserEntity user) {
        // 1. パスワードを暗号化
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // 2. DBに保存
        userRepository.insert(user);
    }
}
```

---

## STEP 10: Controller（画面遷移の制御）

**場所：** `controller/`パッケージ

Controller は「受付係」。HTTP リクエストを受け取り、Service/Repository に仕事を頼み、Viewに渡すデータを Model に詰めてテンプレートへ返す。

### GET（表示）と POST（送信）のメソッドを対にして考える

ページには大抵「表示」と「処理」がセットで存在する：

```java
// ① サインアップ画面を表示する（GET）
@GetMapping("/users/signUp")
public String showSignUp(Model model) {
    model.addAttribute("userForm", new UserForm());  // 空のFormをViewに渡す
    return "users/signUp";  // テンプレートのパス
}

// ② サインアップを処理する（POST）
@PostMapping("/user")
public String createUser(
    @ModelAttribute("userForm") @Validated(ValidationOrder.class) UserForm userForm,
    BindingResult result,  // バリデーション結果（必ずModelAttributeの直後に書く）
    Model model
) {
    // カスタムバリデーション
    userForm.validatePasswordConfirmation(result);
    
    // メール重複チェック
    if (userRepository.existsByEmail(userForm.getUserEmail())) {
        result.rejectValue("userEmail", "null", "Email already exists");
    }
    
    // エラーがあれば画面を再表示
    if (result.hasErrors()) {
        model.addAttribute("userForm", userForm);
        return "users/signUp";
    }
    
    // Form → Entity への変換
    UserEntity user = new UserEntity();
    user.setUsername(userForm.getUsername());
    user.setUserEmail(userForm.getUserEmail());
    user.setPassword(userForm.getPassword());
    
    // Serviceに処理を委譲
    userService.createUserWithEncryptedPassword(user);
    
    return "redirect:/";  // 処理成功後はリダイレクト
}
```

### Controller の典型的な処理フロー

```
リクエスト受け取り
→ バリデーション（@Validated）
→ カスタムバリデーション（Form のメソッド呼び出し）
→ 追加チェック（DB重複確認など）
→ エラーあり → 画面再表示（return テンプレートパス）
→ エラーなし → Form から Entity に値をコピー
→ Repository/Service に処理を委譲
→ 成功 → リダイレクト（return "redirect:/..."）
```

### ログイン済みユーザーの取得

```java
@GetMapping("/")
public String index(@AuthenticationPrincipal CustomUserDetail currentUser, Model model) {
    // currentUser から ID を取り出して DB からユーザー情報を再取得
    UserEntity user = userRepository.findById(currentUser.getId());
    model.addAttribute("user", user);
    ...
}
```

---

## STEP 11: View（Thymeleaf テンプレート）

**場所：** `src/main/resources/templates/`

テンプレートは `Controller が return した文字列 + .html` のファイルを探す。
例：`return "users/signUp"` → `templates/users/signUp.html`

### Thymeleaf の基本構文

```html
<!-- Controller から渡されたデータを表示 -->
<p th:text="${user.username}">仮テキスト</p>

<!-- Form のフィールドと HTML を紐づける（th:object と th:field を対にして使う） -->
<form th:action="@{/user}" method="post" th:object="${userForm}">
    <input type="text" th:field="*{username}" />
    <!-- th:field は name="username" id="username" value="..." を自動でセット -->
</form>

<!-- 繰り返し -->
<div th:each="room : ${rooms}">
    <p th:text="${room.name}"></p>
</div>

<!-- 条件分岐 -->
<div th:if="${room != null}">...</div>

<!-- フラグメント（部品）の読み込み -->
<div th:insert="~{messages/side_bar :: side_bar}"></div>

<!-- URLの生成 -->
<a th:href="@{/rooms/{id}/messages(id=${room.id})}">入室</a>
```

---

## STEP 12: 補助設定クラス

### WebConfig（静的ファイルのURLマッピング）

画像は `src/main/resources/static/uploads/` に保存するが、Spring Boot はデフォルトで `static/` の中しか自動でURLに公開しない。`uploads/` はその下にあるが、外部からパス指定でアクセスするための明示的なマッピングが必要。

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")          // URLのパターン
                .addResourceLocations("file:src/main/resources/static/uploads/");  // 実ファイルの場所
    }
}
```

### ImageUrl（設定値を Java クラスで使う）

```java
@Component
public class ImageUrl {
    @Value("${image.url}")  // application.properties の image.url の値を注入
    private String url;
    
    public String getImageUrl() { return url; }
}
```

→ Controller で `@Autowired`（または `@AllArgsConstructor` で DI）して使う。

---

## まとめ：ファイル作成の順番表

| ステップ | 作るもの | なぜこの順番か |
|---|---|---|
| 1 | 機能リスト・DB設計（紙orメモ） | コードを書く前に「何を作るか」を固める |
| 2 | `application.properties` | DB 接続情報を先に書かないと起動できない |
| 3 | Flyway SQL（V1〜V4） | テーブルを先に作らないと Entity が作れない |
| 4 | Entity クラス（4つ） | DB の構造を Java で表現する（Repository の引数/戻り値になる） |
| 5 | Repository インターフェース（4つ） | SQL を書く。Entity の形が決まっていないと書けない |
| 6 | `CustomUserDetail` | SecurityConfig より先に作る（SecurityConfigが参照するから） |
| 7 | `UserAuthenticationService` | SecurityConfig に登録する前に作る |
| 8 | `SecurityConfig` | Repository ができた後（`UserAuthenticationService` を使うため） |
| 9 | `ValidationPriority1/2/Order` | Form クラスがアノテーションで参照するため先に作る |
| 10 | Form クラス（5つ） | Controller が引数で受け取るため先に作る |
| 11 | Service クラス | Controller から呼ばれるため先に作る |
| 12 | `ImageUrl`, `WebConfig` | Controller から使われる補助クラス |
| 13 | Controller クラス（3つ） | 上流のすべてが揃ってから書く |
| 14 | Thymeleaf テンプレート（HTML） | Controller が `return` するパスに合わせて作る |

---

## よくある「詰まりポイント」と対処法

### 「どこから書き始めればいいかわからない」
→ **DB設計から始める**。「どんなデータを保存するか」が決まれば、あとは機械的に下から上へ積み上げられる。

### 「Controller に何を書けばいいかわからない」
→ **処理の流れを日本語で書く**。「①フォームを受け取る→②バリデーション→③DBに保存→④リダイレクト」と書いてからコードに変換する。

### 「MyBatisのSQLがうまく動かない」
→ カラム名とフィールド名の対応を確認する。名前が違う場合は `@Results/@Result` でマッピング必須。

### 「Spring Security でリダイレクトループになる」
→ SecurityConfig の `permitAll()` に漏れがないか確認。ログインページ自体とサインアップページは必ず `permitAll()`。

### 「@AuthenticationPrincipal が null になる」
→ そのURLが SecurityConfig で `authenticated()` の対象になっているか確認。また、ログインしていない状態でテストしていないか確認。

---

## 開発の「コツ」

1. **1機能ずつ動かしながら作る**：全部作ってから動かすより、「ユーザー登録だけ完成させてから次の機能へ」の方がデバッグが楽。

2. **エラーメッセージを読む**：Spring Boot のエラーは丁寧で、どのクラスの何行目でどんな問題があるかを教えてくれる。

3. **依存関係の矢印を意識する**：「このクラスは何を必要としているか」を意識すると、どのファイルを先に作ればよいかがわかる。

4. **Form と Entity を明確に使い分ける**：Controller の中でいつ Form から Entity に変換しているかを意識する。「入力を受け取る Form → 変換 → DB 操作用の Entity」。
