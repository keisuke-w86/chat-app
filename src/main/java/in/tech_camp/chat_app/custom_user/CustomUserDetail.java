package in.tech_camp.chat_app.custom_user; // custom_userというパッケージ（フォルダ）に配置します

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import in.tech_camp.chat_app.entity.UserEntity;
import lombok.Data;

@Data // Getter/Setterなどを自動生成します
public class CustomUserDetail implements UserDetails { // Spring Security専用の「身分証（UserDetails）」のルールに従うことを宣言します
    
    // この身分証の中に、実際のユーザー情報（金庫の箱）を保持しておきます
    private final UserEntity user;

    // コンストラクタ：この身分証を作る時に、必ずUserEntityを渡してもらうようにします
    public CustomUserDetail(UserEntity user) {
        this.user = user;
    }

    @Override // UserDetailsのルールで「権限（管理者か一般人か等）」を答える必須メソッドです
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 今回は権限の区別がないので、誰もが「空っぽの権限リスト」を持つことにします
        return Collections.emptyList();
    }

    @Override // UserDetailsのルールで「パスワード」を答える必須メソッドです
    public String getPassword() {
        // 持っているUserEntityからパスワードを取り出して答えます
        return user.getPassword();
    }

    @Override // UserDetailsのルールで「ログインに使うID（ユーザー名）」を答える必須メソッドです
    public String getUsername() {
        // ★重要：今回は「メールアドレス」でログインさせたいので、名前ではなくメールアドレスを答えるようにします！
        // （あなたのEntityに合わせて getUserEmail() に修正しています）
        return user.getUserEmail();
    }

    // --- ここから下は独自のメソッド（必須ではないが、後で名前やIDを取り出したい時に便利） ---

    // ユーザーのIDを取り出すメソッド（あなたのEntityに合わせてLong型に修正しています）
    public Long getId() {
        return user.getId();
    }

    // ユーザーの名前を取り出すメソッド（あなたのEntityに合わせてgetUsername()に修正しています）
    public String getName() {
        return user.getUsername();
    }

    // --- ここから下は、アカウントの状態（凍結されていないか等）を答える必須メソッドです ---
    // 今回は細かい制御をしないので、すべて「問題なし（true）」と答えるようにしておきます

    @Override
    public boolean isAccountNonExpired() {
        return true; // アカウントの有効期限切れではないか？ → はい（true）
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // アカウントがロックされていないか？ → はい（true）
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // パスワードの有効期限切れではないか？ → はい（true）
    }

    @Override
    public boolean isEnabled() {
        return true; // アカウントが有効か？ → はい（true）
    }
}