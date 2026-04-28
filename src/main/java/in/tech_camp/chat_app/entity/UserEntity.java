package in.tech_camp.chat_app.entity;

import lombok.Data;

@Data
public class UserEntity {
    private Long id;
    private String username;
    private String userEmail;
    private String password;
    
}
