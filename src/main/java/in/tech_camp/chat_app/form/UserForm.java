package in.tech_camp.chat_app.form;

import lombok.Data;

@Data
public class UserForm {
    private String username;
    private String userEmail;
    private String password;
    private String passwordConfirmation;
}
