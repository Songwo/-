package com.bmgf.service.impl;
import com.bmgf.po.ExamRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class ExamRecordService {
    @Autowired
    private MongoTemplate mongoTemplate;
    public String totalTime(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        List<ExamRecord> examRecords = mongoTemplate.find(query, ExamRecord.class);
        int totalMinutes = examRecords.stream()
                .filter(record -> record.getCostTime() != null && record.getCostTime() > 0)
                .mapToInt(ExamRecord::getCostTime)
                .sum();
        return String.valueOf(totalMinutes);
    }
}
