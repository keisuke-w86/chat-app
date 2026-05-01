package in.tech_camp.chat_app.entity;

import lombok.Data;

@Data
public class MessageEntity {
    private Long id;
    private String content;
    private UserEntity user;
    private RoomEntity room;
    private String createdAt;
    private String image;
}
