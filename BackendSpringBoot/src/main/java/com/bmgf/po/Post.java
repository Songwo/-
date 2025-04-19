package com.bmgf.po;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "posts")
public class Post {
    @Id
    private String id;
    @Indexed
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_]+$") // MongoDB 自动生成的 ObjectId
    private String authorId; // 关联 User.id
    private String username;
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{3,30}$", message = "用户名需为3-30位中文、字母、数字或下划线")
    private String title;
    private String section;
    private String content;
    private int replyCount = 0;
    private String avatar;
    @CreatedDate
    private Date timestamp; // 自动生成创建时间
}
