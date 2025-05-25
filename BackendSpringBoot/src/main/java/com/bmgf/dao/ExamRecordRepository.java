package com.bmgf.dao;

import com.bmgf.controller.QuestionController;
import com.bmgf.po.ExamRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExamRecordRepository extends MongoRepository<ExamRecord, String> {
        List<ExamRecord> findByUserId(String userId);
        List<ExamRecord> findByExamId(String examId);
        List<ExamRecord> findByUserIdOrderBySubmitTimeDesc(String userId);


        // 可以添加更多查询方法
        List<ExamRecord> findByUserIdAndExamId(String userId, String examId);

        // 查询用户最近N次考试记录
        List<ExamRecord> findTop10ByUserIdOrderBySubmitTimeDesc(String userId);

        // 查询用户某个时间段的考试记录
        List<ExamRecord> findByUserIdAndSubmitTimeBetween(
                String userId,
                LocalDateTime startTime,
                LocalDateTime endTime
        );
}
