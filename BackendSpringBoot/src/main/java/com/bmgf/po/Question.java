package com.bmgf.po;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "questions")
public class Question {
    @Id
    private String id;//唯一标识
    private String title;
    private String category;
    private List<String> options;
    private String answer;//储存答案
    private String explanation;
    private String createdAt;
    private String updatedAt;
    private int score = 10;//本题分值
    private int difficultyLevel;
}
