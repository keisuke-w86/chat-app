package in.tech_camp.chat_app.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class MessageController {
    @GetMapping("/messages")
    public String getMessages() {
        return "messages/index";
    }
    
}
