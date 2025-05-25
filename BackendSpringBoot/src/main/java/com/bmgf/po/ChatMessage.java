package com.bmgf.po;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.awt.*;
import java.time.LocalDateTime;
@Builder
@Data
@Document(collection = "chat_messages")
public class ChatMessage {
    @Id
    private String id;
    @Field("room_id")
    private String roomId;
    @Field("sender_id")
    private String senderId;
    private String content;
    private MessageType type; // TEXT/IMAGE/SYSTEM
    private LocalDateTime timestamp;
}
