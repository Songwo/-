package com.itheima.csstudent.dao;

import com.itheima.csstudent.po.AnswerRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface AnswerRecordRepository extends MongoRepository<AnswerRecord,String> {
}
