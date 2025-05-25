package com.bmgf.po;

import com.bmgf.controller.QuestionController;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

// 新增考试记录实体
@Data
@Document(collection = "exam_records")
public class ExamRecord {
    @Id
    private String id;
    private String userId;
    private String examId;
    private LocalDateTime submitTime;
    //花费时间
    private Integer CostTime;
    private int totalScore;
    private int correctCount;
    private List<QuestionController.QuestionResult> questionResults;
}

