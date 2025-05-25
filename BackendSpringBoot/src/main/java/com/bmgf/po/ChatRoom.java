package com.bmgf.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
@CompoundIndexes({
        @CompoundIndex(name = "name_creator_idx", def = "{'name': 1, 'creatorId': 1}", unique = true),
        @CompoundIndex(name = "expire_status_idx", def = "{'expireAt': 1, 'status': 1}")
})
@Document(collection = "chat_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    @Id
    private String id;

    private String name;       // 房间名称
    private String category;   // 房间类别
    private String password;   // BCrypt加密后的密码
    private String creatorId;  // 创建者ID

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime expireAt; // 自动销毁时间

    @Builder.Default
    private Set<String> members = new HashSet<>(); // 当前成员

    @Transient
    private boolean isOwner;   // 非持久化字段（用于前端展示）
}