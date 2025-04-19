package com.bmgf.dao;

import com.bmgf.po.AnswerRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface AnswerRecordRepository extends MongoRepository<AnswerRecord,String> {
}
