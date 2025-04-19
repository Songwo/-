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
@Document(collection = "comments")
public class Comment {
    @Id
    private String id;
    @Indexed
    @Pattern(regexp = "^[a-zA-Z0-9_]+$") // MongoDB 自动生成的 ObjectId
    private String postId; // 关联 Post.id (ObjectId 格式)
    private String authorId;
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{3,30}$", message = "用户名需为3-30位中文、字母、数字或下划线")
    private String username;
    private String content;
    @CreatedDate
    private Date timestamp; // 自动生成创建时间
    private String avatar;
}
