package in.tech_camp.chat_app.form;
  
  import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import in.tech_camp.chat_app.factories.UserEditFormFactory;
import in.tech_camp.chat_app.validation.ValidationPriority1;
import in.tech_camp.chat_app.validation.ValidationPriority2;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
  
  @ActiveProfiles("test")
  @SpringBootTest
  public class UserEditFormUnitTest {
    private UserEditForm userEditForm;
  
    private Validator validator;
  
    @BeforeEach
    public void setUp() {
      userEditForm = UserEditFormFactory.createEditUser();
      userEditForm.setId(1);
  
      ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
      validator = factory.getValidator();
    }
  
    @Nested
    class ユーザー情報を編集できる場合 {
      @Test
      public void 全てデータが存在すれば保存できること () {
        Set<ConstraintViolation<UserEditForm>> violations = validator.validate(userEditForm, ValidationPriority1.class);
        assertEquals(0, violations.size());
      }
    }
  
    @Nested
    class ユーザー情報を編集できない場合 {
      @Test
      public void nameが空では登録できない () {
        userEditForm.setUsername("");
        Set<ConstraintViolation<UserEditForm>> violations = validator.validate(userEditForm, ValidationPriority1.class);
        assertEquals(1, violations.size());
        assertEquals("Name can't be blank", violations.iterator().next().getMessage());
      }
  
      @Test
      public void emailが空では登録できない () {
        userEditForm.setUserEmail("");
        Set<ConstraintViolation<UserEditForm>> violations = validator.validate(userEditForm, ValidationPriority1.class);
        assertEquals(1, violations.size());
        assertEquals("Email can't be blank", violations.iterator().next().getMessage());
      }
  
      @Test
      public void emailは無効なメールでは登録できない() {
        userEditForm.setUserEmail("invalidEmail");
        Set<ConstraintViolation<UserEditForm>> violations = validator.validate(userEditForm, ValidationPriority2.class);
        assertEquals(1, violations.size());
        assertEquals("Email should be valid", violations.iterator().next().getMessage());
      }
    }
  }