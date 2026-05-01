package in.tech_camp.chat_app.service; // serviceという新しいパッケージ（部署）に所属します

// 必要なクラスをインポートします（SecurityConfigで作ったPasswordEncoderもここで使います！）
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import in.tech_camp.chat_app.entity.UserEntity;
import in.tech_camp.chat_app.repository.UserRepository;
import lombok.AllArgsConstructor;

@Service // このクラスが「複雑な処理を行う専門職人（サービス）」であることをSpring Bootに宣言します
@AllArgsConstructor // 下にある2つの「final」変数の初期設定を全自動で作ってくれます
public class UserService {
    
    // データベース保存専門の作業員（リポジトリ）を呼び出せるようにしておきます
    private final UserRepository userRepository;
    
    // SecurityConfigで「@Bean」として登録しておいた、パスワード暗号化の道具を呼び出します
    private final PasswordEncoder passwordEncoder;

    // Controllerから「このユーザーを暗号化して保存して！」と依頼される窓口となるメソッドです
    public void createUserWithEncryptedPassword(UserEntity userEntity) {
        
        // ① まず、渡されたEntityの中から、ユーザーが入力した「生のパスワード」を取り出し、すぐ下のメソッドを使って暗号化します
        String encodedPassword = encodePassword(userEntity.getPassword());
        
        // ② 暗号化された解読不能なパスワードを、再びEntity（金庫の箱）に上書きセットします（これで安心！）
        userEntity.setPassword(encodedPassword);
        
        // ③ 安全になったEntityを、Repositoryに渡してデータベースへ保存（insert）してもらいます
        userRepository.insert(userEntity);
    }

    // パスワードを暗号化するための専用の小さなメソッドです（privateなので、このクラスの中でしか使えません）
    private String encodePassword(String password) {
        // パスワード暗号化の道具（passwordEncoder）を使って、生の文字列をハッシュ化して返します
        return passwordEncoder.encode(password);
    }
    
}