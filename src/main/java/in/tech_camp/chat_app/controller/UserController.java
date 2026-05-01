package in.tech_camp.chat_app.controller; // このファイルがどのフォルダ（パッケージ）に属しているかを宣言します

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.tech_camp.chat_app.custom_user.CustomUserDetail;
import in.tech_camp.chat_app.entity.RoomEntity;
import in.tech_camp.chat_app.entity.RoomUserEntity;
import in.tech_camp.chat_app.entity.UserEntity;
import in.tech_camp.chat_app.form.EditForm;
import in.tech_camp.chat_app.form.LoginForm;
import in.tech_camp.chat_app.form.UserForm;
import in.tech_camp.chat_app.repository.RoomUserRepository;
import in.tech_camp.chat_app.repository.UserRepository;
import in.tech_camp.chat_app.service.UserService;
import in.tech_camp.chat_app.validation.ValidationOrder;
import lombok.AllArgsConstructor;
@Controller // このクラスがブラウザからのリクエストを受け付ける「受付窓口（コントローラー）」であることを宣言します
@AllArgsConstructor // Lombokの機能で、下にある「final」がついた変数の初期設定（コンストラクタ）を全自動で作ってくれます
public class UserController {

    // データベースとやり取りするための専門の作業員（リポジトリ）を用意します
    private final UserRepository userRepository;
// ★新しく作った専門職人（UserService）を呼び出せるように準備します
    private final UserService userService;
     private final RoomUserRepository roomUserRepository;
    
    @GetMapping("/users/sign_up") // ブラウザから「/users/sign_up」にアクセス（GETリクエスト）が来た時に動くメソッドです
    public String showSignUp(Model model){ // Modelは、画面（HTML）へデータを運ぶための「段ボール箱」です
        
        // 段ボール箱に、新しく作った空っぽの「受付用紙（UserForm）」を「userForm」という名札をつけて入れます
        model.addAttribute("userForm", new UserForm());
        
        // templatesフォルダの中にある「users/signUp.html」を表示するように指示します
        return "users/signUp";
    }

        @PostMapping("/user")
    public String createUser(@ModelAttribute("userForm") @Validated(ValidationOrder.class) UserForm userForm, BindingResult result, Model model) {
      userForm.validatePasswordConfirmation(result);
      if (userRepository.existsByEmail(userForm.getUserEmail())) {
        result.rejectValue("userEmail", "null", "Email already exists");
      }
  
      if (result.hasErrors()) {
        List<String> errorMessages = result.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
  
        model.addAttribute("errorMessages", errorMessages);
        model.addAttribute("userForm", userForm);
        return "users/signUp";
      }
  
      UserEntity userEntity = new UserEntity();
      userEntity.setUsername(userForm.getUsername());
      userEntity.setUserEmail(userForm.getUserEmail());
      userEntity.setPassword(userForm.getPassword());
  
      try {
        userService.createUserWithEncryptedPassword(userEntity);
      } catch (Exception e) {
        System.out.println("エラー：" + e);
        model.addAttribute("userForm", userForm);
        return "users/signUp";
      }
  
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
    @GetMapping("/users/{userId}/edit")
    public String showEdit(@PathVariable("userId") Integer userId, Model model) {
        UserEntity user = userRepository.findById(userId);
        EditForm editForm = new EditForm();
        editForm.setId(user.getId());
        editForm.setUsername(user.getUsername());
        editForm.setUserEmail(user.getUserEmail());
        model.addAttribute("user", editForm);
        return "users/edit";
    } 
         @PostMapping("/users/{userId}")
    public String updateUser(@PathVariable("userId") Integer userId, @ModelAttribute("user") @Validated(ValidationOrder.class) EditForm editForm, BindingResult result, Model model) {
      String newEmail = editForm.getUserEmail();
      if (userRepository.existsByEmailExcludingCurrent(newEmail, userId)) {
        result.rejectValue("userEmail", "error.user", "Email already exists");
      }
      if (result.hasErrors()) {
        List<String> errorMessages = result.getAllErrors().stream()
                                      .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                      .collect(Collectors.toList());
        model.addAttribute("errorMessages", errorMessages);
        model.addAttribute("user", editForm);
        return "users/edit";
      }
      
      UserEntity user = userRepository.findById(userId);
      user.setUsername(editForm.getUsername());
      user.setUserEmail(editForm.getUserEmail());
  
      try {
        userRepository.update(user);
      } catch (Exception e) {
        System.out.println("エラー：" + e);
        model.addAttribute("user", editForm);
        return "users/edit";
      }
  
      return "redirect:/";
    }


    @GetMapping("/")
        public String index(@AuthenticationPrincipal CustomUserDetail currentUser, Model model) {
        UserEntity user = userRepository.findById(currentUser.getId());
        model.addAttribute("user", user);
        List<RoomUserEntity> roomUserEntities = roomUserRepository.findByUserId(currentUser.getId());
        List<RoomEntity> roomList = roomUserEntities.stream()
            .map(RoomUserEntity::getRoom)
            .collect(Collectors.toList());
        model.addAttribute("rooms", roomList);
        return "rooms/index";
        }
    
}
