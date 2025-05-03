package com.bmgf.po;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Document(collection = "announcements")
public class Announcement {
    @Id
    private String id;
    private String title;
    private String content;
    private String type; // 公告类型：warning, info, danger等
    private String tag; // 标签：重要、普通等
    private Date createTime;
    private String creatorId; // 创建者ID
    private Boolean isActive; // 是否激活
}
