package com.itheima.csstudent.po;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Video_Category")
@Data
public class Video_Category {
    @Id
    private String id;
    private String challengel1;
    private String challengel2;
    private String challengel3;
    private String challengel4;
    private String challengel5;
    private String challengel6;
}
