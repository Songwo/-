package com.bmgf.po;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
@Data
@Document(collection = "answer_records")
@AllArgsConstructor // 显式生成全参构造函数
@NoArgsConstructor  // 生成无参构造函数
public class AnswerRecord {
    @Id
    private String id;
    private String userId;
    private String questionId;
    private String examId;
    private String userAnswer;
    private boolean correct;
    private int scoreDelta;// 本次得分变化
    private LocalDateTime answerTime =LocalDateTime.now();
}
