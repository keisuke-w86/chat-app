package in.tech_camp.chat_app.entity;

import java.sql.Timestamp;

import lombok.Data;
@Data
public class MessageEntity {
    private Long id;
    private String content;
    private UserEntity user;
    private RoomEntity room;
    private Timestamp createdAt;
    private String image;
}
