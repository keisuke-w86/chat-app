package in.tech_camp.chat_app.controller; // このファイルがどのフォルダ（パッケージ）に属しているかを宣言します

// 必要なクラスやSpring Bootの便利機能をインポート（取り込み）します
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.tech_camp.chat_app.entity.UserEntity;
import in.tech_camp.chat_app.form.LoginForm;
import in.tech_camp.chat_app.form.UserForm;
import in.tech_camp.chat_app.repository.UserRepository;
import in.tech_camp.chat_app.service.UserService;
import lombok.AllArgsConstructor;


@Controller // このクラスがブラウザからのリクエストを受け付ける「受付窓口（コントローラー）」であることを宣言します
@AllArgsConstructor // Lombokの機能で、下にある「final」がついた変数の初期設定（コンストラクタ）を全自動で作ってくれます
public class UserController {

    // データベースとやり取りするための専門の作業員（リポジトリ）を用意します
    private final UserRepository userRepository;
// ★新しく作った専門職人（UserService）を呼び出せるように準備します
    private final UserService userService;
    
    @GetMapping("/users/sign_up") // ブラウザから「/users/sign_up」にアクセス（GETリクエスト）が来た時に動くメソッドです
    public String showSignUp(Model model){ // Modelは、画面（HTML）へデータを運ぶための「段ボール箱」です
        
        // 段ボール箱に、新しく作った空っぽの「受付用紙（UserForm）」を「userForm」という名札をつけて入れます
        model.addAttribute("userForm", new UserForm());
        
        // templatesフォルダの中にある「users/signUp.html」を表示するように指示します
        return "users/signUp";
    }

    @PostMapping("/user") // 画面から「/user」宛てにデータが送信（POSTリクエスト）されてきた時に動くメソッドです
    public String createUser(@ModelAttribute("userForm") UserForm userForm, Model model) { 
        // 飛んできたデータを「受付用紙（userForm）」に書き込まれた状態で受け取り、同時に「段ボール箱（model）」も用意します

        // データベースの金庫に入れるための箱（UserEntity）を新しく作ります
        UserEntity userEntity = new UserEntity();
        
        // 【⚠️注意】あなたのコードに合わせて getUsername() / getUserEmail() などに書き換えてください！
        // 受付用紙（Form）に書かれた名前を取り出して、金庫の箱（Entity）にセットします
        userEntity.setUsername(userForm.getUsername());
        
        // 受付用紙に書かれたメールアドレスを取り出して、金庫の箱にセットします
        userEntity.setUserEmail(userForm.getUserEmail());
        
        // 受付用紙に書かれたパスワードを取り出して、金庫の箱にセットします
        userEntity.setPassword(userForm.getPassword());

        try { // エラーが起きるかもしれない「データベースへの保存」の処理を try { } で囲んで監視します
            
            // 専門の作業員（userService）に、金庫の箱（userEntity）をデータベースへ保存（insert）するようにお願いします
            userService.createUserWithEncryptedPassword(userEntity);
            
        } catch (Exception e) { // もし保存中に何らかのエラー（Exception）が起きたら、ここでキャッチします
            
            // 開発者が原因を特定できるように、ターミナル（黒い画面）にエラーの内容を表示します
            System.out.println("エラー：" + e);
            
            // エラーになってしまったので、ユーザーが入力した内容が残ったままの受付用紙を、もう一度段ボール箱に入れます
            model.addAttribute("userForm", userForm);
            
            // 再度、新規登録画面（signUp.html）を表示して、入力をやり直してもらいます
            return "users/signUp";
        }

        // 無事にデータベースへの保存が終わったら、トップページ（/）へ強制移動（リダイレクト）するように指示します
        return "redirect:/";
    }

    @GetMapping("/users/login")
    public String showLogin(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "users/login";
    }
    
    @GetMapping("/login")
    public String login(@RequestParam(value= "error", required=false) String error, @ModelAttribute("loginForm") LoginForm loginForm, Model model) {
        if (error != null) {
            model.addAttribute("loginError", "メールアドレスかパスワードが違います");
        }
        return "users/login";
    }
}

