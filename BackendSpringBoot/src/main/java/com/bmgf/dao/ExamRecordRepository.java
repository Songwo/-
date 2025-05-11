package com.bmgf.dao;

import com.bmgf.controller.QuestionController;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExamRecordRepository extends MongoRepository<QuestionController.ExamRecord, String> {
        List<QuestionController.ExamRecord> findByUserId(String userId);
        List<QuestionController.ExamRecord> findByExamId(String examId);
        List<QuestionController.ExamRecord> findByUserIdOrderBySubmitTimeDesc(String userId);

        // 可以添加更多查询方法
        List<QuestionController.ExamRecord> findByUserIdAndExamId(String userId, String examId);

        // 查询用户最近N次考试记录
        List<QuestionController.ExamRecord> findTop10ByUserIdOrderBySubmitTimeDesc(String userId);

        // 查询用户某个时间段的考试记录
        List<QuestionController.ExamRecord> findByUserIdAndSubmitTimeBetween(
                String userId,
                LocalDateTime startTime,
                LocalDateTime endTime
        );
}
