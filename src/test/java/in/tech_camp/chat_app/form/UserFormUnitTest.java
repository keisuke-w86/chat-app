// このクラスが属しているパッケージ（フォルダ階層）を宣言します
package in.tech_camp.chat_app.form;
  
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.BindingResult;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import in.tech_camp.chat_app.factories.UserFormFactory;
import in.tech_camp.chat_app.validation.ValidationPriority1;
import in.tech_camp.chat_app.validation.ValidationPriority2;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
  
// 「application-test.yml」など、テスト環境用の設定ファイルを使うように指定します
@ActiveProfiles("test")
// Spring Bootの機能を丸ごと立ち上げて、実際のアプリに近い状態でテストを行うための宣言です
@SpringBootTest
public class UserFormUnitTest {
    // テスト対象となるフォームのデータを格納する箱（変数）を用意します
    private UserForm userForm;
  
    // バリデーション（入力チェック）を強制的に実行するためのツールを用意します
    private Validator validator;
  
    // @BeforeEachは、このクラスの中にある各テスト（@Test）が実行される「直前」に毎回必ず呼ばれるメソッドです
    // これにより、前のテストの結果が次のテストに影響を与えないようにします
    @BeforeEach
    public void setUp() {
        // テスト用のダミーデータ（正しい状態のデータ）を生成してuserFormにセットします
        userForm = UserFormFactory.createUser();
  
        // バリデーションツールを生成するための工場（Factory）を作ります
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        // 工場から実際にチェックを行う担当者（validator）を生成します
        validator = factory.getValidator();
    }
  
    // @Nestedを使うことで、テスト結果の画面で「ユーザーを作成できる場合」というグループにまとめて見やすくします
    @Nested
    class ユーザーを作成できる場合 {
        
        // このメソッドが1つの「テストケース」であることを示します
        @Test
        public void nameとemailとpasswordとpasswordconfirmationが存在すれば登録できる () {
            // validatorを使って、userFormの中身をチェックします（ValidationPriority1という優先度のルールだけを適用します）
            // チェックに引っかかったエラーのリストが violations という箱に入ります
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            
            // violations（エラーの数）が「0」であることを確認（assertEquals）します。
            // 0であれば、正しいデータとして扱われているためテスト成功となります。
            assertEquals(0, violations.size());
        }
    }
  
    // エラーが起きる（作成できない）パターンのグループを作ります
    @Nested
    class ユーザーを作成できない場合 {
        
        @Test
        public void nameが空では登録できない () {
            // 正しい状態のダミーデータから、意図的に名前（Username）だけを空っぽにします
            userForm.setUsername("");
            
            // 空っぽの状態でバリデーションチェックを実行します
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            
            // 名前が空なので、エラーが「1つ」発生しているはずだと確認します
            assertEquals(1, violations.size());
            // その発生した1つのエラーメッセージが「Name can't be blank」と完全に一致しているかを確認します
            assertEquals("Name can't be blank", violations.iterator().next().getMessage());
        }
  
        @Test
        public void emailが空では登録できない () {
            // 意図的にメールアドレスだけを空っぽにしてテストします
            userForm.setUserEmail("");
            
            // バリデーションチェックを実行します
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            
            // エラーが「1つ」発生していることを確認します
            assertEquals(1, violations.size());
            // エラーメッセージが「Email can't be blank」であることを確認します
            assertEquals("Email can't be blank", violations.iterator().next().getMessage());
        }
  
        @Test
        public void passwordが空では登録できない() {
            // 意図的にパスワードだけを空っぽにしてテストします
            userForm.setPassword("");
            
            // バリデーションチェックを実行します
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority1.class);
            
            // エラーが「1つ」発生していることを確認します
            assertEquals(1, violations.size());
            // エラーメッセージが「Password can't be blank」であることを確認します
            assertEquals("Password can't be blank", violations.iterator().next().getMessage());
        }

        @Test
        public void emailはアットマークを含まないと登録できない() {
            userForm.setUserEmail("invalidEmail");
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority2.class);
            assertEquals(1,violations.size());
            assertEquals("Email should be valid", violations.iterator().next().getMessage());
        }

        @Test
        public void passwordが5文字以下では登録できない() {
            userForm.setPassword("a".repeat(5));
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority2.class);
            assertEquals(1,violations.size());
            assertEquals("Password should be between 6 and 128 characters", violations.iterator().next().getMessage());

    }

        @Test
        public void passwordが129文字以上では登録できない() {
            userForm.setPassword("a".repeat(129));
            Set<ConstraintViolation<UserForm>> violations = validator.validate(userForm, ValidationPriority2.class);
            assertEquals(1,violations.size());
            assertEquals("Password should be between 6 and 128 characters", violations.iterator().next().getMessage());
    }

      @Test
    public void passwordとpasswordConfirmationが不一致では登録できない() {
        // ① テスト用のダミーの BindingResult を作成する
        BindingResult bindingResult = mock(BindingResult.class);

        // ② パスワードの不一致を起こす
        userForm.setPasswordConfirmation("differentPassword");
        
        // ③ ダミーの箱を渡してバリデーションを実行する
        userForm.validatePasswordConfirmation(bindingResult);

        // ④ ダミーの箱の中に、想定通りのエラーが記録されたかを確認（verify）する
        verify(bindingResult).rejectValue("passwordConfirmation", "error.user", "Password confirmation does not match");
    }
    }
}