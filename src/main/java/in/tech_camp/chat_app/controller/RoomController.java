package in.tech_camp.chat_app.controller;

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
import org.springframework.web.bind.annotation.PostMapping;

import in.tech_camp.chat_app.custom_user.CustomUserDetail;
import in.tech_camp.chat_app.entity.RoomEntity;
import in.tech_camp.chat_app.entity.RoomUserEntity;
import in.tech_camp.chat_app.entity.UserEntity;
import in.tech_camp.chat_app.form.RoomForm;
import in.tech_camp.chat_app.repository.RoomRepository;
import in.tech_camp.chat_app.repository.RoomUserRepository;
import in.tech_camp.chat_app.repository.UserRepository;
import in.tech_camp.chat_app.validation.ValidationOrder;
import lombok.AllArgsConstructor;
@Controller
@AllArgsConstructor
public class RoomController {
  private final UserRepository userRepository;
  private final RoomRepository roomRepository;

  private final RoomUserRepository roomUserRepository;
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

  @GetMapping("/rooms/new")
  public String showRoomNew(@AuthenticationPrincipal CustomUserDetail currentUser, Model model){
    List<UserEntity> users = userRepository.findAllExcept(currentUser.getId());
    model.addAttribute("users", users);
    model.addAttribute("roomForm", new RoomForm());
    return "rooms/new";
  }
    @PostMapping("/rooms")
    public String createRoom(@ModelAttribute("RoomForm") @Validated(ValidationOrder.class) RoomForm roomForm, BindingResult bindingResult, @AuthenticationPrincipal CustomUserDetail currentUser, Model model){
      if (bindingResult.hasErrors()) {
        List<String> errorMessages = bindingResult.getAllErrors().stream()
                                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                .collect(Collectors.toList());
        List<UserEntity> users = userRepository.findAllExcept(currentUser.getId());
        model.addAttribute("users", users);
        model.addAttribute("roomForm", roomForm);
        model.addAttribute("errorMessages", errorMessages);
        return "rooms/new";
      }
  
      RoomEntity roomEntity = new RoomEntity();
      roomEntity.setName(roomForm.getName());
      try {
        roomRepository.insert(roomEntity);
      } catch (Exception e) {
        System.out.println("エラー：" + e);
        List<UserEntity> users = userRepository.findAllExcept(currentUser.getId());
        model.addAttribute("users", users);
        model.addAttribute("roomForm", new RoomForm());
        return "rooms/new";
      }
    return "redirect:/";
  }
}
