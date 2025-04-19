package com.bmgf.po;

import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Map;
@Data
@Document(collection = "challenges")
public class Challenge {
    @Id
    private String id;
    private String title;
    private String description;
    private int difficulty;
    private String task;
    private String flag;
    private int score;
    private Map<String, ImageConfig> images; // 关键点
    private int durationMinutes;
}
