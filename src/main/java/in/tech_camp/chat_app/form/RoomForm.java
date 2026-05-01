package in.tech_camp.chat_app.form;
import java.util.List;

import in.tech_camp.chat_app.validation.ValidationPriority1;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoomForm {
    @NotBlank(message="Name can't be blank", groups = ValidationPriority1.class)
    public String name;
    private List<Integer> memberIds;
}
