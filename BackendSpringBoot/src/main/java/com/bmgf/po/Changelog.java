package com.bmgf.po;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Document(collection = "changelogs")
public class Changelog {
    @Id
    private String id;
    private String version; // 版本号
    private String type; // 类型：feature, fix, improvement等
    private String content; // 更新内容
    private Date createTime;
    private String creatorId; // 创建者ID
    private Boolean isActive; // 是否激活
}