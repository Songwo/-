package com.itheima.csstudent.po;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection = "labFlag")
@Data
public class LabFlag {
    @Id
    private String id;
    private String imageName;
    private String flag;
}