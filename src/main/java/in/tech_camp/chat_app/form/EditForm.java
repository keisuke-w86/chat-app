package in.tech_camp.chat_app.form;

import in.tech_camp.chat_app.validation.ValidationPriority1;
import in.tech_camp.chat_app.validation.ValidationPriority2;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EditForm {
    @NotBlank(message = "Id can't be blank", groups = ValidationPriority1.class)
    private Integer id;

    @NotBlank(message = "Name can't be blank", groups = ValidationPriority1.class)
    private String username;
    
    @NotBlank(message = "Email can't be blank", groups = ValidationPriority1.class)
    @Email(message = "Email should be valid", groups = ValidationPriority2.class)
    private String userEmail;


}
 

