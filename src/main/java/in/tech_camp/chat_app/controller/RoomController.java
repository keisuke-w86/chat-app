package in.tech_camp.chat_app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import in.tech_camp.chat_app.form.RoomForm;
import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class RoomController {
    @GetMapping("/rooms/new")
    public String showRoomNew(Model model) {
        model.addAttribute("roomsForm", new RoomForm());
        return "rooms/new";
    }
}
