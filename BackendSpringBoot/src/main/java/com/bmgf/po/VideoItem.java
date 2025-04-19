package com.bmgf.po;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "VideItem") // 指定 MongoDB 集合名称
public class VideoItem {
    @Id
    private String id; // MongoDB 使用 String 类型主键
    private String title;
    private String url;
    private String imageUrl;
    private String description;
    private String categories;
    @Indexed(name = "createTime_index", direction = IndexDirection.DESCENDING)
    private Date createTime;
}