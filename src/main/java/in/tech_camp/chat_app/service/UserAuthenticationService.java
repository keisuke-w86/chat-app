package in.tech_camp.chat_app.service; // serviceパッケージに配置します

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import in.tech_camp.chat_app.custom_user.CustomUserDetail;
import in.tech_camp.chat_app.entity.UserEntity;
import in.tech_camp.chat_app.repository.UserRepository;
import lombok.AllArgsConstructor;

@Service // このクラスが専門の職人（サービス）であることを宣言します
@AllArgsConstructor // userRepositoryを自動で初期化します
public class UserAuthenticationService implements UserDetailsService { 
    // Spring Security専用の「ユーザー検索窓口（UserDetailsService）」のルールに従うことを宣言します

    // データベースからユーザーを探してくる作業員（リポジトリ）を用意します
    private final UserRepository userRepository;

    @Override // UserDetailsServiceのルールで「名前（今回はメール）からユーザーを探す」必須メソッドです
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // ※引数名は「email」ですが、Spring Security側からはログイン画面で入力された文字列が渡されてきます

        // リポジトリにお願いして、データベースからそのメールアドレスを持つ人（Entity）を探してきます
        UserEntity userEntity = userRepository.findByEmail(email);

        // もし、誰も見つからなかったら（nullだったら）
        if (userEntity == null) {
            // 「そんな人はいません！」という専用のエラーを警備員（Spring Security）に投げ返します
            // これにより、ログイン画面で「パスワードかメールアドレスが違います」と表示させることができます
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // 無事に見つかったら、先ほど作った「身分証（CustomUserDetail）」の中にEntityを入れて、
        // 警備員（Spring Security）に渡してあげます（その後、警備員がパスワードの一致チェックを自動でやってくれます！）
        return new CustomUserDetail(userEntity);
    }
}