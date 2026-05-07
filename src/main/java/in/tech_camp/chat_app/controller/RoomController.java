package in.tech_camp.chat_app.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class RoomController {
  private final UserRepository userRepository;
  private final RoomRepository roomRepository;
  private final RoomUserRepository roomUserRepository;

 

  @GetMapping("/rooms/new")
  public String showRoomNew(@AuthenticationPrincipal CustomUserDetail currentUser, Model model){
    List<UserEntity> users = userRepository.findAllExcept(currentUser.getId());
    model.addAttribute("users", users);
    model.addAttribute("roomForm", new RoomForm());
    return "rooms/new";
  }
    @PostMapping("/rooms")
      public String createRoom(@ModelAttribute("RoomForm") RoomForm roomForm, @AuthenticationPrincipal CustomUserDetail currentUser, Model model){
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

    List<Integer> memberIds = roomForm.getMemberIds();
    for (Integer userId : memberIds) {
      UserEntity userEntity = userRepository.findById(userId);
      RoomUserEntity roomUserEntity = new RoomUserEntity();
      roomUserEntity.setRoom(roomEntity);
      roomUserEntity.setUser(userEntity);
      try {
        roomUserRepository.insert(roomUserEntity);
      } catch (Exception e) {
        System.out.println("エラー：" + e);
        List<UserEntity> users = userRepository.findAllExcept(currentUser.getId());
        model.addAttribute("users", users);
        model.addAttribute("roomForm", new RoomForm());
        return "rooms/new";
      }
    }
    return "redirect:/";
  }
}
