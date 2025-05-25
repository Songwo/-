package com.bmgf.po;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "feedback")
public class Feedback {
    @Id
    private String id;

    private String type;        // 反馈类型：feature, bug, question, other
    private String title;       // 反馈标题
    private String content;     // 详细描述
    private String contact;     // 联系方式
    private List<String> imageUrls;  // 图片URL列表
    private String status;      // 状态：pending, processing, resolved
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}
